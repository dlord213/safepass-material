package me.mirimomekiku.safepass.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mirimomekiku.safepass.R
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.helpers.Password
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.composables.HalfCircleProgressIndicator
import me.mirimomekiku.safepass.ui.composables.dialogs.AppPickerDialog
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun AddAppScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val appNameTextFieldState = rememberTextFieldState()
    val usernameTextFieldState = rememberTextFieldState()
    val passwordTextFieldState = rememberTextFieldState()
    val notesTextFieldState = rememberTextFieldState()

    var selectedPackageName by remember { mutableStateOf<String?>(null) }
    var showAppPickerDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        pageCount = { 2 }, initialPage = 0
    )
    val pagerScope = rememberCoroutineScope()

    if (showAppPickerDialog) {
        AppPickerDialog(
            onAppSelected = { appInfo ->
                appNameTextFieldState.setTextAndPlaceCursorAtEnd(appInfo.appName)
                selectedPackageName = appInfo.packageName
                showAppPickerDialog = false
            },
            onDismissRequest = {
                showAppPickerDialog = false
            }
        )
    }

    fun saveApp(
        appName: String,
        packageName: String,
        username: String,
        password: String,
        notes: String?,
    ) {
        if (appNameTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Application name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (usernameTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPackageName == null) {
            Toast.makeText(context, "Please select an application", Toast.LENGTH_SHORT).show()
            return
        }

        pagerScope.launch {
            val aead = TinkManager.getAead()
            val encryptedPassword = CryptoUtil.encrypt(aead, password, packageName)

            val credential = AppCredentials(
                appName = appName,
                packageName = packageName,
                username = username,
                password = encryptedPassword,
                notes = notes
            )

            val dao = AppDatabase.getDatabase(context).appCredentialsDao()
            withContext(Dispatchers.IO) {
                dao.insertCredentials(credential)
            }

            navController.popBackStack()
        }
    }

    val passwordStrength = remember(passwordTextFieldState.text) {
        Password.analyze(passwordTextFieldState.text.toString())
    }

    HorizontalPager(state = pagerState, userScrollEnabled = false) { page ->
        when (page) {
            0 -> Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text("Add application", style = MaterialTheme.typography.titleLarge)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextField(
                        state = appNameTextFieldState,
                        label = { Text("Application") },
                        textStyle = MaterialTheme.typography.labelLarge,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showAppPickerDialog = true }) {
                                Image(
                                    painter = painterResource(R.drawable.apps_24px),
                                    contentDescription = "Select an application"
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .clickable { showAppPickerDialog = true }
                            .fillMaxWidth()
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Credentials", style = MaterialTheme.typography.labelLarge)
                    TextField(
                        state = usernameTextFieldState,
                        label = { Text("Username") },
                        textStyle = MaterialTheme.typography.labelLarge,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldDefaults.colors(
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                    )
                    SecureTextField(
                        state = passwordTextFieldState,
                        label = { Text("Password") },
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = TextFieldDefaults.colors(
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                        ),
                    )
                }
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(12.dp),
//                ) {
//                    Text("Extras", style = MaterialTheme.typography.labelLarge)
//                    TextField(
//                        state = notesTextFieldState,
//                        label = { Text("Notes") },
//                        textStyle = MaterialTheme.typography.labelLarge,
//                        colors = TextFieldDefaults.colors(
//                            cursorColor = MaterialTheme.colorScheme.primary,
//                            disabledIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent
//                        ),
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        keyboardOptions = KeyboardOptions(
//                            imeAction = ImeAction.Done,
//                        ),
//                    )
//                }
                Spacer(modifier = Modifier.weight(1f, fill = true))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FilledTonalButton(onClick = {
                        navController.popBackStack()
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Cancel")
                    }
                    FilledTonalButton(onClick = {
                        if (passwordTextFieldState.text.isEmpty()) {
                            Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            pagerScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Next")
                    }
                }
            }

            1 -> Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        pagerScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text("Check", style = MaterialTheme.typography.titleLarge)
                }


                HalfCircleProgressIndicator(
                    progress = passwordStrength.percent / 100f,
                    label = passwordStrength.label,
                    progressColor = when (passwordStrength.score) {
                        4 -> Color(0xFF4CAF50)
                        3 -> Color(0xFF8BC34A)
                        2 -> Color(0xFFFFA000)
                        1 -> Color(0xFFFF5722)
                        else -> Color.Red
                    }
                )


                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Feedback/Suggestions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (passwordStrength.feedback!!.suggestions!!.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                passwordStrength.feedback.suggestions.map { it ->
                                    Text(
                                        it.toString(), style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Text(
                            "This would take ${passwordStrength.crackTimes?.onlineThrottling100perHour} to crack.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f, fill = true))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FilledTonalButton(onClick = {
                        pagerScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Back")
                    }
                    FilledTonalButton(onClick = {
                        val appName = appNameTextFieldState.text.toString()
                        val packageName = selectedPackageName
                        val username = usernameTextFieldState.text.toString()
                        val password = passwordTextFieldState.text.toString()
                        val notes = notesTextFieldState.text.toString().takeIf { it.isNotBlank() }

                        if (packageName != null) {
                            saveApp(
                                appName = appName,
                                packageName = packageName,
                                username = username,
                                password = password,
                                notes = notes,
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Please select an application first.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}