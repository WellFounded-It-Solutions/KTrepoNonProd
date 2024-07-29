package se.infomaker.profile.view.items.authentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import se.infomaker.profile.view.items.authentication.data.AuthenticationDialogState

@Composable
fun AuthenticationDialog(dialogState: AuthenticationDialogState) {
    if (dialogState.visible) {
        AlertDialog(
            onDismissRequest = { },
            title = { dialogState.logoutDialogStrings?.confirm?.let { Text(text = it, style= MaterialTheme.typography.h6) } },
            confirmButton = {
                dialogState.logoutDialogStrings?.confirm?.let { message ->
                    Text(
                        modifier = Modifier
                            .padding(12.dp)
                            .runNotNull(dialogState.authAction) { Modifier.clickable(onClick = it) }
                            .padding(12.dp), text = message.uppercase(),
                        style = TextStyle(color = MaterialTheme.colors.primary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    )
                }
            },
            dismissButton = {
                dialogState.logoutDialogStrings?.cancel?.let { message ->
                    Text(
                        modifier = Modifier
                            .padding(12.dp)
                            .runNotNull(dialogState.dismissAction) { Modifier.clickable(onClick = it) }
                            .padding(12.dp), text = message.uppercase(),
                        style = TextStyle(color = MaterialTheme.colors.primary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    )
                }
            },
            text = {
                dialogState.logoutDialogStrings?.message?.let { Text(text = it) }
            },
        )
    }
}

fun <T, R> T.runNotNull(subject: R?, block: (R) -> T): T {
    return when (subject) {
        null -> this
        else -> block(subject)
    }
}