package it.imposteur.ui.schermate

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.imposteur.R

@Composable
fun DialogoConferma(testo: String, onSi: () -> Unit, onNo: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNo,
        title = { Text(testo) },
        confirmButton = { TextButton(onClick = onSi) { Text(stringResource(R.string.si)) } },
        dismissButton = { TextButton(onClick = onNo) { Text(stringResource(R.string.no)) } },
    )
}
