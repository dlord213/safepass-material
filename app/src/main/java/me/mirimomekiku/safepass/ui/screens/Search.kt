package me.mirimomekiku.safepass.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import coil3.compose.AsyncImage
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.db.entity.CardCredentials
import me.mirimomekiku.safepass.db.entity.WebsiteCredentials
import me.mirimomekiku.safepass.helpers.BiometricCallback
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.composables.AppIcon
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun SearchScreen(navBackStackEntry: NavBackStackEntry) {
    val query = navBackStackEntry.arguments?.getString("query")?.toString() ?: return
    val context = LocalContext.current
    val activity = LocalActivity.current as FragmentActivity
    val navController = LocalNavController.current
    val executor = ContextCompat.getMainExecutor(context)

    val textFieldState = rememberTextFieldState(initialText = "")

    // credentials
    var websiteCredentials by rememberSaveable { mutableStateOf(emptyList<WebsiteCredentials>()) }
    var cardCredentials by rememberSaveable { mutableStateOf(emptyList<CardCredentials>()) }
    var appCredentials by rememberSaveable { mutableStateOf(emptyList<AppCredentials>()) }

    val groupedWebCredentials = remember(websiteCredentials) {
        websiteCredentials.sortedBy { it.label.lowercase() }.groupBy { it.label }
    }
    val groupedCardCredentials = remember(cardCredentials) {
        cardCredentials.sortedBy { it.type.lowercase() }.groupBy { it.type }
    }
    val groupedAppCredentials = remember(appCredentials) {
        appCredentials.sortedBy { it.appName.lowercase() }.groupBy { it.appName }
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate to access credentials")
            .setSubtitle("To view credentials, authenticate.").setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ).build()
    }

    LaunchedEffect(query) {
        suspend fun loadWebsiteCredentials(): List<WebsiteCredentials> {
            val dao = AppDatabase.getDatabase(context).websiteCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.searchCredentials(query = "%$query%").map { cred ->
                cred.copy(password = CryptoUtil.decrypt(aead, cred.password, cred.domain))
            }
        }

        suspend fun loadCardCredentials(): List<CardCredentials> {
            val dao = AppDatabase.getDatabase(context).cardCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.searchCredentials(query = "%$query%").map { cred ->
                cred.copy(cardNumber = CryptoUtil.decrypt(aead, cred.cardNumber, cred.cardHolder))
            }
        }

        suspend fun loadAppCredentials(): List<AppCredentials> {
            val dao = AppDatabase.getDatabase(context).appCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.searchCredentials(query = "%$query%").map { cred ->
                cred.copy(password = CryptoUtil.decrypt(aead, cred.password, cred.packageName))
            }
        }

        websiteCredentials = loadWebsiteCredentials()
        cardCredentials = loadCardCredentials()
        appCredentials = loadAppCredentials()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            TextField(
                state = textFieldState,
                label = { Text("Search safe") },
                textStyle = MaterialTheme.typography.labelLarge,
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                onKeyboardAction = KeyboardActionHandler {
                    navController.navigate("search/${textFieldState.text}")
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedWebCredentials.forEach { (name, creds) ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        AsyncImage(
                            model = "https://www.google.com/s2/favicons?domain=${creds.firstOrNull()?.domain}&sz=48",
                            contentDescription = "${creds.firstOrNull()?.domain} favicon",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = creds.firstOrNull()?.domain.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                items(creds, key = { it.username + it.password }) { cred ->
                    FilledTonalButton(
                        onClick = {
                            val prompt = BiometricPrompt(
                                activity, executor, BiometricCallback(context) {
                                    navController.navigate("view_website/${cred.id}")
                                })

                            prompt.authenticate(promptInfo)
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
                                    activity, executor, BiometricCallback(context) {
                                        navController.navigate("view_website/${cred.id}")
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

            groupedCardCredentials.forEach { (name, creds) ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(creds, key = { it.lastFour + it.cardNumber }) { cred ->
                    FilledTonalButton(
                        onClick = {
                            val prompt = BiometricPrompt(
                                activity,
                                executor,
                                BiometricCallback(context) {
                                    navController.navigate("view_card/${cred.id}")
                                })

                            prompt.authenticate(promptInfo)
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
                                    text = "**** **** **** ${cred.lastFour}",
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
                                        navController.navigate("view_card/${cred.id}")
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

            groupedAppCredentials.forEach { (appName, creds) ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        creds.firstOrNull()?.let {
                            AppIcon(
                                packageName = it.packageName, modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                items(creds, key = { it.packageName + it.id }) { cred ->
                    FilledTonalButton(
                        onClick = {
                            val prompt = BiometricPrompt(
                                activity, executor, BiometricCallback(context) {
                                    navController.navigate("view_app/${cred.id}")
                                })

                            prompt.authenticate(promptInfo)
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
                                    activity, executor, BiometricCallback(context) {
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
}