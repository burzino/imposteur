package it.imposteur.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class RepositorySegnalazioni(context: Context) {
    private val file: File = File(context.getExternalFilesDir(null) ?: context.filesDir, "segnalazioni.jsonl")
    private val mutex = Mutex()

    suspend fun aggiungi(s: Segnalazione) {
        val riga = FormatoSegnalazioni.riga(s)
        mutex.withLock {
            withContext(Dispatchers.IO) {
                file.parentFile?.mkdirs()
                file.appendText(riga + "\n")
            }
        }
    }

    suspend fun conta(): Int = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (file.exists()) FormatoSegnalazioni.leggi(file.readText()).size else 0
        }
    }

    suspend fun cancellaTutte() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                if (file.exists()) file.delete()
            }
        }
    }
}
