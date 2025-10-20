package me.mirimomekiku.safepass.ui.screens

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.mirimomekiku.safepass.R
import me.mirimomekiku.safepass.helpers.Password
import me.mirimomekiku.safepass.ui.composables.OptionSwitch
import kotlin.math.roundToInt

@Composable
fun GeneratorScreen() {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var generatedPassword by remember { mutableStateOf("") }
    val generatedPasswordTextFieldState = rememberTextFieldState()

    var passwordLength by remember { mutableFloatStateOf(16f) }
    var useLowercase by remember { mutableStateOf(true) }
    var useUppercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var excludeSimilar by remember { mutableStateOf(true) }
    var excludeSequential by remember { mutableStateOf(true) }
    var excludeRepeated by remember { mutableStateOf(true) }
    var startWithLetter by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        generatedPassword = Password.generate()
        generatedPasswordTextFieldState.setTextAndPlaceCursorAtEnd(generatedPassword)
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Generator",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        TextField(
            state = generatedPasswordTextFieldState,
            enabled = false,
            label = { Text("Generated password") },
            textStyle = MaterialTheme.typography.labelLarge,
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            leadingIcon = {
                IconButton(onClick = {
                    val clipData = ClipData.newPlainText("generatedPassword", generatedPassword)
                    scope.launch {
                        clipboardManager.setClipEntry(clipData.toClipEntry())
                    }
                }
                ) {
                    Image(
                        painter = painterResource(R.drawable.content_copy_24px),
                        contentDescription = "Copy generated password to clipboard"
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    generatedPassword = Password.generate(
                        length = passwordLength.roundToInt(),
                        useLowercase = useLowercase,
                        useUppercase = useUppercase,
                        useNumbers = useNumbers,
                        useSymbols = useSymbols,
                        excludeSimilar = excludeSimilar,
                        excludeSequential = excludeSequential,
                        excludeRepeated = excludeRepeated,
                        startWithLetter = startWithLetter,
                    )
                    generatedPasswordTextFieldState.setTextAndPlaceCursorAtEnd(generatedPassword)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh, contentDescription = "Generate password"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Length", style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = passwordLength.roundToInt().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = passwordLength,
                        onValueChange = { passwordLength = it },
                        valueRange = 8f..64f,
                        steps = (64 / 4) - 1 // Creates a step for each integer value
                    )
                }

            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Options",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Character type options
                    OptionSwitch(
                        label = "Lowercase (a-z)",
                        checked = useLowercase,
                        onCheckedChange = { useLowercase = it })
                    OptionSwitch(
                        label = "Uppercase (A-Z)",
                        checked = useUppercase,
                        onCheckedChange = { useUppercase = it })
                    OptionSwitch(
                        label = "Numbers (0-9)",
                        checked = useNumbers,
                        onCheckedChange = { useNumbers = it })
                    OptionSwitch(
                        label = "Symbols (!@#$)",
                        checked = useSymbols,
                        onCheckedChange = { useSymbols = it })

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    // Rule options
                    OptionSwitch(
                        label = "Exclude similar characters (l, 1, I, O, 0)",
                        checked = excludeSimilar,
                        onCheckedChange = { excludeSimilar = it })
                    OptionSwitch(
                        label = "Exclude sequential characters (abc, 123)",
                        checked = excludeSequential,
                        onCheckedChange = { excludeSequential = it })
                    OptionSwitch(
                        label = "Exclude repeated characters (aa, 11)",
                        checked = excludeRepeated,
                        onCheckedChange = { excludeRepeated = it })
                    OptionSwitch(
                        label = "Must start with a letter",
                        checked = startWithLetter,
                        onCheckedChange = { startWithLetter = it })
                }
            }
        }
    }
}