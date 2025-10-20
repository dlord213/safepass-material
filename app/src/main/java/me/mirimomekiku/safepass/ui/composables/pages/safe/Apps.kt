package me.mirimomekiku.safepass.ui.composables.pages.safe

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.helpers.BiometricCallback
import me.mirimomekiku.safepass.ui.composables.AppIcon
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun AppsPage(appCredentials: List<AppCredentials>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = LocalActivity.current as FragmentActivity
    val navController = LocalNavController.current
    val executor = ContextCompat.getMainExecutor(context)

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate to access credentials")
            .setSubtitle("To view credentials, authenticate.").setAllowedAuthenticators(
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            ).build()
    }

    val groupedAppCredentials = remember(appCredentials) {
        appCredentials.sortedBy { it.appName.lowercase() }.groupBy { it.appName }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        groupedAppCredentials.forEach { (appName, creds) ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    creds.firstOrNull()?.let {
                        AppIcon(
                            packageName = it.packageName,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            items(creds, key = { it.id }) { cred ->
                FilledTonalButton(
                    onClick = {
                        val biometricManager = BiometricManager.from(context)
                        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                val prompt = BiometricPrompt(
                                    activity,
                                    executor,
                                    BiometricCallback(context) {
                                        navController.navigate("view_app/${cred.id}")
                                    })

                                prompt.authenticate(promptInfo)
                            }

                            else -> {
                                navController.navigate("view_app/${cred.id}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cred.username,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = {
                            val prompt = BiometricPrompt(
                                activity,
                                executor,
                                BiometricCallback(context) {
                                    navController.navigate("view_app/${cred.id}")
                                })

                            prompt.authenticate(promptInfo)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View credentials"
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}