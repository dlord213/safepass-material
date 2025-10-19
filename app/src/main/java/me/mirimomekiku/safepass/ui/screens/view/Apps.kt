package me.mirimomekiku.safepass.ui.screens.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.composables.AppIcon
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun ViewAppCredentialScreen(navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val id = navBackStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return
    val scope = rememberCoroutineScope()

    var credentials by remember { mutableStateOf<AppCredentials?>(null) }
    var isPasswordShown by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val appNameTextFieldState = rememberTextFieldState()
    val usernameTextFieldState = rememberTextFieldState()
    val passwordTextFieldState = rememberTextFieldState()
    val notesTextFieldState = rememberTextFieldState()

    LaunchedEffect(id) {
        val dao = AppDatabase.getDatabase(context).appCredentialsDao()
        val aead = TinkManager.getAead()
        val result = withContext(Dispatchers.IO) { dao.getCredentialsById(id) }

        if (result != null) {
            val decrypted = result.copy(
                password = CryptoUtil.decrypt(aead, result.password, result.packageName)
            )
            credentials = decrypted

            appNameTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.appName)
            usernameTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.username)
            passwordTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.password)
            notesTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.notes ?: "")
        } else {
            navController.popBackStack()
        }
    }

    fun saveChanges() {
        val currentCreds = credentials ?: return

        if (usernameTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (passwordTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val aead = TinkManager.getAead()
            val encryptedPassword = CryptoUtil.encrypt(
                aead,
                passwordTextFieldState.text.toString(),
                currentCreds.packageName
            )

            val updatedCredential = currentCreds.copy(
                username = usernameTextFieldState.text.toString(),
                password = encryptedPassword,
                notes = notesTextFieldState.text.toString().takeIf { it.isNotBlank() }
            )

            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(context).appCredentialsDao()
                    .updateCredentials(updatedCredential)
            }
        }
    }

    fun deleteCredential() {
        credentials?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(context).appCredentialsDao()
                        .deleteCredentialsById(it.id)
                }
                navController.popBackStack()
            }
        }
    }

    credentials?.let { cred ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Row {
                    IconButton(onClick = { deleteCredential() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        if (isEditing) {
                            saveChanges()
                        }
                        isEditing = !isEditing
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIcon(packageName = cred.packageName, modifier = Modifier.size(56.dp))
                Column {
                    Text(
                        text = cred.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isEditing) "Edit" else cred.username,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    state = appNameTextFieldState,
                    readOnly = true,
                    label = { Text("Application (read-only)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    state = usernameTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                )
                SecureTextField(
                    state = passwordTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Password") },
                    textObfuscationMode = if (isPasswordShown) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    trailingIcon = {
                        IconButton(onClick = { isPasswordShown = !isPasswordShown }) {
                            Icon(
                                imageVector = if (isPasswordShown) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    state = notesTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isEditing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            saveChanges()
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                }
            }
        }
    }
}
