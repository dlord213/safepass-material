package me.mirimomekiku.safepass.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.CardCredentials
import me.mirimomekiku.safepass.helpers.CardHelper
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.composables.CardPreview
import me.mirimomekiku.safepass.ui.compositions.LocalNavController


@Composable
fun AddCardScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val labelTextFieldState = rememberTextFieldState()
    val cardHolderTextFieldState = rememberTextFieldState()
    val cardNumberTextFieldState = rememberTextFieldState()
    val expiryMonthTextFieldState = rememberTextFieldState()
    val expiryYearTextFieldState = rememberTextFieldState()
    val cvvTextFieldState = rememberTextFieldState()
    val notesTextFieldState = rememberTextFieldState()

    var cardType by remember { mutableStateOf("Unknown") }

    LaunchedEffect(cardNumberTextFieldState.text) {
        cardType = CardHelper.detectCardType(cardNumberTextFieldState.text.toString())
        if (labelTextFieldState.text.isBlank() && cardType != "Unknown") {
            labelTextFieldState.edit { replace(0, length, cardType) }
        }
    }

    fun saveCard() {
        val cardNumber = cardNumberTextFieldState.text.filter { it.isDigit() }
        val lastFour = cardNumber.takeLast(4)

        if (cardNumber.isEmpty()) {
            Toast.makeText(context, "Card number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (cardHolderTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Card holder name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (expiryMonthTextFieldState.text.isEmpty() || expiryYearTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Expiry date cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (cvvTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "CVV cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val aead = TinkManager.getAead()
            val encryptedCardNumber = CryptoUtil.encrypt(
                aead, cardNumber.toString(), cardHolderTextFieldState.text.toString()
            )
            val encryptedCVV = CryptoUtil.encrypt(
                aead, cvvTextFieldState.text.toString(), cardHolderTextFieldState.text.toString()
            )


            val credential = CardCredentials(
                label = labelTextFieldState.text.toString(),
                cardHolder = cardHolderTextFieldState.text.toString(),
                cardNumber = encryptedCardNumber,
                lastFour = lastFour.toString(),
                expiryMonth = expiryMonthTextFieldState.text.toString(),
                expiryYear = expiryYearTextFieldState.text.toString(),
                type = cardType,
                cvv = encryptedCVV,
                notes = notesTextFieldState.text.takeIf { it.isNotBlank() }.toString()
            )

            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(context).cardCredentialsDao().insertCard(credential)
            }

            navController.popBackStack()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text("Add card", style = MaterialTheme.typography.titleLarge)
        }

        CardPreview(
            name = cardHolderTextFieldState.text.toString(),
            cardNumber = cardNumberTextFieldState.text.toString(),
            expiryMonth = expiryMonthTextFieldState.text.toString(),
            expiryYear = expiryYearTextFieldState.text.toString(),
            cardType = cardType,
            cvv = cvvTextFieldState.text.toString(),
        )

        TextField(
            state = labelTextFieldState,
            readOnly = true,
            label = { Text("Label (auto-detected)") },
            textStyle = MaterialTheme.typography.labelLarge,
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            state = cardHolderTextFieldState,
            label = { Text("Card holder name") },
            textStyle = MaterialTheme.typography.labelLarge,
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )

        TextField(
            state = cardNumberTextFieldState,
            label = { Text("Card number") },
            inputTransformation = InputTransformation.maxLength(16),
            textStyle = MaterialTheme.typography.labelLarge,
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number,
            ),
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )

        Text("Detected card type: $cardType", style = MaterialTheme.typography.bodyMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                state = expiryMonthTextFieldState,
                label = { Text("MM") },
                inputTransformation = InputTransformation.maxLength(2),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            TextField(
                state = expiryYearTextFieldState,
                label = { Text("YY") },
                inputTransformation = InputTransformation.maxLength(2),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            SecureTextField(
                state = cvvTextFieldState,
                label = { Text("CVV") },
                inputTransformation = InputTransformation.maxLength(3),
                modifier = Modifier.weight(1f)
            )
        }

        TextField(
            state = notesTextFieldState,
            label = { Text("Notes (optional)") },
            textStyle = MaterialTheme.typography.labelLarge,
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f, true))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            FilledTonalButton(
                onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            FilledTonalButton(
                onClick = {
                    saveCard()
                }, modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}
