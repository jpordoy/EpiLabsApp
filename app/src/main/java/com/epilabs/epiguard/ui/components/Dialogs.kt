package com.epilabs.epiguard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LoadingDialog(
    isVisible: Boolean,
    message: String = "Loading..."
) {
    if (isVisible) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = message)
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel"
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

@Composable
fun ErrorDialog(
    isVisible: Boolean,
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}