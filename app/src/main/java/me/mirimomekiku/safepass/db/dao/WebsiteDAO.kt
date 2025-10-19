package me.mirimomekiku.safepass.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import me.mirimomekiku.safepass.db.entity.WebsiteCredentials

@Dao
interface WebsiteCredentialsDAO {

    @Query("SELECT * FROM website_credentials WHERE id = :id")
    suspend fun getCredentialsById(id: Int): WebsiteCredentials

    @Query("SELECT * FROM website_credentials WHERE url = :url")
    suspend fun getCredentialsForUrl(url: String): List<WebsiteCredentials>

    @Insert
    suspend fun insertCredentials(credentials: WebsiteCredentials)

    @Update
    suspend fun updateCredentials(credentials: WebsiteCredentials)

    @Query("DELETE FROM website_credentials WHERE url = :url")
    suspend fun deleteCredentialsForUrl(url: String)

    @Query("DELETE FROM website_credentials WHERE id = :id")
    suspend fun deleteCredentialsById(id: Int)

    @Query("DELETE FROM website_credentials where label = :label")
    suspend fun deleteCredentialsByLabel(label: String)

    @Query("SELECT * FROM website_credentials")
    suspend fun getAllCredentials(): List<WebsiteCredentials>

    @Query("SELECT * FROM website_credentials WHERE url LIKE :query OR username LIKE :query")
    suspend fun searchCredentials(query: String): List<WebsiteCredentials>

    @Query("SELECT * FROM website_credentials WHERE id = :id")
    suspend fun searchCredentialsById(id: Int): List<WebsiteCredentials>
}