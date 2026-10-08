package it.imposteur.game

internal fun parole(vararg coppie: Pair<String, String?>): List<Parola> =
    coppie.map { Parola(it.first, it.second) }

internal fun cat(id: String, vararg coppie: Pair<String, String?>): Categoria =
    Categoria(id, id.replaceFirstChar { it.uppercase() }, parole(*coppie))

internal fun categorieDemo(): List<Categoria> = listOf(
    cat("animali", "Cane" to "Lupo", "Gatto" to "Leone", "Topo" to "Criceto"),
    cat("cibo", "Pizza" to "Focaccia", "Pasta" to "Riso"),
)

internal fun configBase(
    n: Int = 5, k: Int = 1, mod: Modalita = Modalita.SENZA_PAROLA,
    cats: Set<String> = setOf("animali", "cibo"), nomi: List<String> = emptyList(),
    mostra: Boolean = true,
) = Configurazione(n, nomi, k, mod, mostra, cats)
