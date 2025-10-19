package me.mirimomekiku.safepass.ui.composables.pages.safe

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun SafeSearch(pagerState: PagerState, scope: CoroutineScope) {
    val navController = LocalNavController.current

    val textFieldState = rememberTextFieldState(initialText = "")

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(0) } }) {
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
}