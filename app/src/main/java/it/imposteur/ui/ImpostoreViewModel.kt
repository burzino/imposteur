package it.imposteur.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.imposteur.data.RepositoryConfigurazione
import it.imposteur.data.RepositoryParole
import it.imposteur.data.RepositorySessione
import it.imposteur.data.RisultatoCaricamento
import it.imposteur.game.Categoria
import it.imposteur.game.Configurazione
import it.imposteur.game.Distribuzione
import it.imposteur.game.ErroreConfigurazione
import it.imposteur.game.GestorePartite
import it.imposteur.game.Modalita
import it.imposteur.game.Partita
import it.imposteur.game.Regole
import it.imposteur.game.RisultatoNuovaPartita
import it.imposteur.game.SessioneSalvata
import it.imposteur.game.StatoDistribuzione
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class UiState(
    val caricamento: Boolean = true,
    val erroreCaricamento: Boolean = false,
    val categorie: List<Categoria> = emptyList(),
    val config: Configurazione = Configurazione(),
    val partita: Partita? = null,
    val distribuzione: StatoDistribuzione = Distribuzione.iniziale(),
    /** Partita salvata valida (con stato), offerta da "Riprendi partita". */
    val ripristinabile: SessioneSalvata? = null,
) {
    val errori: List<ErroreConfigurazione>
        get() = if (categorie.isEmpty()) emptyList() else Regole.valida(config, categorie)
    val puoIniziare: Boolean get() = !caricamento && !erroreCaricamento && errori.isEmpty()
}

class ImpostoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repoParole = RepositoryParole(application)
    private val repoConfig = RepositoryConfigurazione(application)
    private val repoSessione = RepositorySessione(application)
    private var gestore = GestorePartite(Random.Default)
    /** True se la partita in UI fa parte della sessione salvata. */
    private var partitaAttiva = false

    private val _stato = MutableStateFlow(UiState())
    val stato: StateFlow<UiState> = _stato.asStateFlow()

    private var salvataggio: Job? = null

    init {
        viewModelScope.launch {
            when (val r = repoParole.carica()) {
                is RisultatoCaricamento.Ok -> {
                    val config = repoConfig.leggi(r.categorie)
                    val sessione = repoSessione.leggi(r.categorie)
                    gestore = GestorePartite(Random.Default, sessione.usate, sessione.ultima)
                    val ripristinabile = sessione.takeIf { it.partita != null && it.stato != null }
                    _stato.update {
                        it.copy(caricamento = false, categorie = r.categorie, config = config, ripristinabile = ripristinabile)
                    }
                }
                is RisultatoCaricamento.Errore ->
                    _stato.update { it.copy(caricamento = false, erroreCaricamento = true) }
            }
        }
    }

    private fun modificaConfig(f: (Configurazione) -> Configurazione) {
        if (_stato.value.caricamento) return
        _stato.update { it.copy(config = f(it.config)) }
        salvataggio?.cancel()
        salvataggio = viewModelScope.launch {
            delay(500)
            repoConfig.salva(_stato.value.config)
        }
    }

    /** Salva subito la configurazione corrente (Inizia, tasto indietro). */
    fun salvaOra() {
        if (_stato.value.caricamento || _stato.value.erroreCaricamento) return
        salvataggio?.cancel()
        val config = _stato.value.config
        viewModelScope.launch { repoConfig.salva(config) }
    }

    fun impostaNumeroGiocatori(n: Int) = modificaConfig { c ->
        Regole.conNumeroGiocatori(c, n.coerceIn(Regole.MIN_GIOCATORI, Regole.MAX_GIOCATORI))
    }

    fun impostaNumeroImpostori(n: Int) = modificaConfig { c ->
        c.copy(numeroImpostori = n.coerceIn(1, Regole.maxImpostori(c.numeroGiocatori)))
    }

    fun impostaNome(indice: Int, testo: String) = modificaConfig { c ->
        val nomi = c.nomi.toMutableList()
        while (nomi.size <= indice) nomi.add("")
        nomi[indice] = testo.take(Regole.MAX_LUNGHEZZA_NOME)
        c.copy(nomi = nomi)
    }

    fun impostaModalita(m: Modalita) = modificaConfig { it.copy(modalita = m) }

    fun impostaMostraCategoria(v: Boolean) = modificaConfig { it.copy(mostraCategoria = v) }

    fun impostaCategoria(id: String, selezionata: Boolean) = modificaConfig { c ->
        c.copy(
            categorieSelezionate = if (selezionata) c.categorieSelezionate + id else c.categorieSelezionate - id,
        )
    }

    fun selezionaTutte(tutte: Boolean) = modificaConfig { c ->
        c.copy(categorieSelezionate = if (tutte) _stato.value.categorie.map { it.id }.toSet() else emptySet())
    }

    /** Crea una nuova partita con la configurazione corrente. True se riuscita. */
    fun iniziaPartita(): Boolean {
        val s = _stato.value
        if (!s.puoIniziare) return false
        salvaOra()
        return when (val r = gestore.nuovaPartita(s.config, s.categorie)) {
            is RisultatoNuovaPartita.Ok -> {
                partitaAttiva = true
                _stato.update {
                    it.copy(partita = r.partita, distribuzione = Distribuzione.iniziale(), ripristinabile = null)
                }
                persisti()
                true
            }
            is RisultatoNuovaPartita.Errore -> false
        }
    }

    /** Avanza solo se lo stato corrente è ancora [da] (ignora i tocchi doppi). */
    fun avanza(da: StatoDistribuzione) {
        val p = _stato.value.partita ?: return
        if (_stato.value.distribuzione != da) return
        _stato.update { it.copy(distribuzione = Distribuzione.avanza(da, p.giocatori.size)) }
        persisti()
    }

    /** App in background o schermata ricreata durante la rivelazione. */
    fun interrompiRivelazione() {
        val prima = _stato.value.distribuzione
        _stato.update { it.copy(distribuzione = Distribuzione.interrompiRivelazione(it.distribuzione)) }
        if (_stato.value.distribuzione != prima) persisti()
    }

    /** Ingresso in Rivela: cancella la partita salvata (le parole usate restano). */
    fun entraInRivela() {
        partitaAttiva = false
        _stato.update { it.copy(ripristinabile = null) }
        persisti()
    }

    fun terminaPartita() {
        partitaAttiva = false
        _stato.update { it.copy(partita = null, distribuzione = Distribuzione.iniziale(), ripristinabile = null) }
        persisti()
    }

    /** Carica la partita salvata nello stato di gioco. True se va aperta la schermata di Gioco. */
    fun riprendiPartita(): Boolean {
        val r = _stato.value.ripristinabile ?: return false
        val stato = r.stato ?: return false
        partitaAttiva = true
        _stato.update { it.copy(partita = r.partita, distribuzione = stato, ripristinabile = null) }
        return stato is StatoDistribuzione.Gioco
    }

    private fun persisti() {
        val s = _stato.value
        val attiva = partitaAttiva && s.partita != null
        val sessione = SessioneSalvata(
            partita = if (attiva) s.partita else null,
            stato = if (attiva) s.distribuzione else null,
            usate = gestore.usate,
            ultima = gestore.ultima,
        )
        viewModelScope.launch { repoSessione.salva(sessione) }
    }
}
