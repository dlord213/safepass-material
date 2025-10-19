package me.mirimomekiku.safepass.ui.screens.view

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
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.navigation.NavBackStackEntry
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
fun ViewCardCredentialScreen(navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val id = navBackStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return
    val scope = rememberCoroutineScope()

    var credentials by remember { mutableStateOf<CardCredentials?>(null) }
    var isCvvShown by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val labelTextFieldState = rememberTextFieldState()
    val cardHolderTextFieldState = rememberTextFieldState()
    val cardNumberTextFieldState = rememberTextFieldState()
    val expiryMonthTextFieldState = rememberTextFieldState()
    val expiryYearTextFieldState = rememberTextFieldState()
    val cvvTextFieldState = rememberTextFieldState()
    val notesTextFieldState = rememberTextFieldState()
    var cardType by remember { mutableStateOf("Unknown") }

    LaunchedEffect(id) {
        val dao = AppDatabase.getDatabase(context).cardCredentialsDao()
        val aead = TinkManager.getAead()
        val result = dao.getCardById(id)

        if (result != null) {
            val decrypted = result.copy(
                cardNumber = CryptoUtil.decrypt(aead, result.cardNumber, result.cardHolder),
                cvv = CryptoUtil.decrypt(aead, result.cvv, result.cardHolder)
            )
            credentials = decrypted

            cardHolderTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.cardHolder)
            labelTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.label)
            cardNumberTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.cardNumber)
            expiryMonthTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.expiryMonth)
            expiryYearTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.expiryYear)
            cvvTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.cvv)
            notesTextFieldState.setTextAndPlaceCursorAtEnd(decrypted.notes ?: "")
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(cardNumberTextFieldState.text) {
        cardType = CardHelper.detectCardType(cardNumberTextFieldState.text.toString())
        labelTextFieldState.edit { replace(0, length, cardType) }
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

        scope.launch {
            val aead = TinkManager.getAead()
            val encryptedCardNumber =
                CryptoUtil.encrypt(
                    aead,
                    cardNumber.toString(),
                    cardHolderTextFieldState.text.toString()
                )
            val encryptedCVV = CryptoUtil.encrypt(
                aead,
                cvvTextFieldState.text.toString(),
                cardHolderTextFieldState.text.toString()
            )


            val credential = CardCredentials(
                id = id,
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
                AppDatabase.getDatabase(context).cardCredentialsDao().updateCard(credential)
            }
        }
    }

    fun deleteCard(id: Int) {
        scope.launch {
            val dao = AppDatabase.getDatabase(context).cardCredentialsDao()
            withContext(Dispatchers.IO) { dao.deleteCardById(id) }
            navController.popBackStack()
        }
    }

    credentials?.let {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { deleteCard(credentials!!.id) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        if (isEditing) {
                            saveCard()
                        }

                        isEditing = !isEditing
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }
            CardPreview(
                name = cardHolderTextFieldState.text.toString(),
                cardNumber = cardNumberTextFieldState.text.toString(),
                expiryMonth = expiryMonthTextFieldState.text.toString(),
                expiryYear = expiryYearTextFieldState.text.toString(),
                cardType = cardType,
                cvv = cvvTextFieldState.text.toString(),
            )

            if (isEditing) {
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
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isEditing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { isEditing = false; navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    FilledTonalButton(
                        onClick = {
                            saveCard()
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                }
            }
        }
    }
}
