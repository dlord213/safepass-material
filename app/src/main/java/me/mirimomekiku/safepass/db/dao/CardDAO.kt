package me.mirimomekiku.safepass.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import me.mirimomekiku.safepass.db.entity.CardCredentials

@Dao
interface CardCredentialsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardCredentials): Long

    @Query("SELECT * FROM card_credentials")
    suspend fun getAllCards(): List<CardCredentials>

    @Query("SELECT * FROM card_credentials WHERE label = :label")
    suspend fun getCardsByLabel(label: String): List<CardCredentials>

    @Query("SELECT * FROM card_credentials WHERE id = :id")
    suspend fun getCardById(id: Int): CardCredentials?

    @Update
    suspend fun updateCard(card: CardCredentials)

    @Query(
        """
        UPDATE card_credentials
        SET label = :label,
            cardHolder = :cardHolder,
            cardNumber = :cardNumber,
            lastFour = :lastFour,
            type = :type,
            expiryMonth = :expiryMonth,
            expiryYear = :expiryYear,
            cvv = :cvv,
            notes = :notes
        WHERE id = :id
    """
    )
    suspend fun updateCardById(
        id: Int,
        label: String,
        cardHolder: String,
        cardNumber: String,
        lastFour: String,
        type: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        notes: String?
    )

    @Delete
    suspend fun deleteCard(card: CardCredentials)

    @Query("DELETE FROM card_credentials WHERE id = :id")
    suspend fun deleteCardById(id: Int)

    @Query("SELECT * FROM card_credentials WHERE label LIKE '%' || :query || '%' OR lastFour LIKE '%' || :query || '%'")
    suspend fun searchCredentials(query: String): List<CardCredentials>
}