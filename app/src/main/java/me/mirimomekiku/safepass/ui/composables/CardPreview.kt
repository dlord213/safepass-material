package me.mirimomekiku.safepass.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CardPreview(
    name: String,
    cardNumber: String,
    expiryMonth: String,
    expiryYear: String,
    cvv: String,
    cardType: String = "VISA"
) {

    val brandColors = mapOf(
        "visa" to listOf(Color(0xFF1A1F71), Color(0xFF3A6EA5)),  // Blue gradient
        "mastercard" to listOf(Color(0xFFFF5F00), Color(0xFFFFBD00)),  // Orange → Yellow
        "amex" to listOf(Color(0xFF2E77BC), Color(0xFF00AEEF)),  // Teal-ish blue
        "american express" to listOf(
            Color(0xFF2E77BC),
            Color(0xFF00AEEF)
        ),   // Teal-ish blue
        "discover" to listOf(Color(0xFFFF6000), Color(0xFFFFD200)),   // Orange → Yellow
        "jcb" to listOf(Color(0xFF0076BE), Color(0xFFFF0000)),   // Blue → Red
        "diners club" to listOf(Color(0xFF006BA6), Color(0xFF00AEEF)),   // Blue gradient
        "unionpay" to listOf(Color(0xFF00853E), Color(0xFFD7001C)),   // Green → Red
        "maestro" to listOf(Color(0xFFCC0000), Color(0xFF005BAA)),   // Red → Blue
        "rupay" to listOf(Color(0xFF004BA0), Color(0xFFF79E1B)),   // Blue → Orange
        "mir" to listOf(Color(0xFFD50000), Color(0xFFFFB300)),   // Red → Yellow
    )

    val targetColors = brandColors[cardType.lowercase()] ?: listOf(Color.Gray, Color.DarkGray)

    val animatedColor1 by animateColorAsState(targetColors[0])
    val animatedColor2 by animateColorAsState(targetColors[1])

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(animatedColor1, animatedColor2)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Name", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text(name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    cardType,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text("Card Number", color = Color.White, style = MaterialTheme.typography.bodySmall)
                Text(
                    cardNumber.chunked(4).joinToString(" "),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Valid", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${expiryMonth.padStart(2, '0')}/${expiryYear}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CVV", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text(cvv, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
