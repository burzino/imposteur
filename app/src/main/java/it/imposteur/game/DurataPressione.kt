package it.imposteur.game

import kotlin.math.floor

/** Durata della pressione lunga per scoprire il ruolo (CA-106). Logica pura. */
object DurataPressione {
    const val MIN_MS = 0
    const val MAX_MS = 1000
    const val PASSO_MS = 50
    const val PREDEFINITA_MS = 150

    private val numero = Regex("""[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?""")

    /** Riporta al valore valido più vicino: clamp a 0..1000, poi multiplo di 50 più vicino (metà strada per eccesso). */
    fun normalizza(valore: Int): Int = arrotonda(valore.toDouble())

    /** Testo -> valore valido; null, vuoto o non numerico -> PREDEFINITA_MS. */
    fun daTesto(s: String?): Int {
        val t = s?.trim() ?: return PREDEFINITA_MS
        if (!numero.matches(t)) return PREDEFINITA_MS
        val d = t.toDoubleOrNull() ?: return PREDEFINITA_MS
        if (d.isNaN() || d.isInfinite()) return PREDEFINITA_MS
        return arrotonda(d)
    }

    /** true se ms == 0: nessuna barra, rivelazione al rilascio. */
    fun soloTocco(ms: Int): Boolean = ms == 0

    private fun arrotonda(v: Double): Int {
        val c = v.coerceIn(MIN_MS.toDouble(), MAX_MS.toDouble())
        return (floor(c / PASSO_MS + 0.5) * PASSO_MS).toInt()
    }
}
