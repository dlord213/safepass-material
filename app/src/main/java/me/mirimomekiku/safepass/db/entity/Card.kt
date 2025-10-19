package me.mirimomekiku.safepass.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "card_credentials")
data class CardCredentials(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val cardHolder: String,
    val cardNumber: String,
    val lastFour: String,
    val expiryMonth: String,
    val expiryYear: String,
    val type: String,
    val cvv: String,
    val notes: String? = null
) : Parcelable