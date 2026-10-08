package it.imposteur.game

enum class Modalita { SENZA_PAROLA, PAROLA_AFFINE }

data class Parola(val parola: String, val affine: String?)
data class Categoria(val id: String, val nome: String, val parole: List<Parola>)

data class Configurazione(
    val numeroGiocatori: Int = 4,
    val nomi: List<String> = emptyList(),
    val numeroImpostori: Int = 1,
    val modalita: Modalita = Modalita.SENZA_PAROLA,
    val mostraCategoria: Boolean = true,
    val categorieSelezionate: Set<String> = emptySet(),
    val impostoreNonPrimo: Boolean = false,
    val impostoriSorpresa: Boolean = false,
    val ordineCasuale: Boolean = false,
    val partitaTrappola: Boolean = false,
    val promemoriaUltimaPossibilita: Boolean = false,
    val giriIndizi: Int = 1,
)

data class VoceParola(val categoriaId: String, val categoriaNome: String, val parola: String, val affine: String?)

sealed interface ErroreConfigurazione {
    data object TroppoPochiGiocatori : ErroreConfigurazione
    data object TroppiGiocatori : ErroreConfigurazione
    data object TroppoPochiImpostori : ErroreConfigurazione
    data object TroppiImpostori : ErroreConfigurazione
    data object NessunaCategoria : ErroreConfigurazione
    data object PoolVuoto : ErroreConfigurazione
    data class NomeDuplicato(val indici: List<Int>) : ErroreConfigurazione
    data class NomeTroppoLungo(val indice: Int) : ErroreConfigurazione
}

/** Testi generati dalla logica (specifiche 4.3 e 4.5). */
object TestiGioco {
    const val SEI_IMPOSTORE = "Sei l'impostore"
    const val LA_PAROLA_E = "La parola è:"
    const val LA_TUA_PAROLA_E = "La tua parola è:"
    fun categoria(nome: String) = "Categoria: $nome"
    fun impostoriSingolare(nome: String) = "L'impostore era: $nome"
    fun impostoriPlurale(nomi: List<String>) = "Gli impostori erano: ${nomi.joinToString(", ")}"
    fun laParolaEra(parola: String) = "La parola era: $parola"
    const val NESSUN_IMPOSTORE = "Nessun impostore: era una partita trappola!"
    const val PROMEMORIA_ULTIMA_POSSIBILITA =
        "L'impostore scoperto può provare a indovinare la parola: se ci riesce, vince lui!"
    fun laParolaAffineEra(affine: String) = "La parola affine era: $affine"
}
