package me.mirimomekiku.safepass.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import me.mirimomekiku.safepass.db.entity.AppCredentials

@Dao
interface AppCredentialsDAO {
    @Query("SELECT * FROM app_credentials WHERE id = :id")
    suspend fun getCredentialsById(id: Int): AppCredentials?

    @Query("SELECT * FROM app_credentials WHERE packageName = :packageName")
    suspend fun getCredentialsForApp(packageName: String): List<AppCredentials>

    @Insert
    suspend fun insertCredentials(credentials: AppCredentials)

    @Update
    suspend fun updateCredentials(credentials: AppCredentials)

    @Query("DELETE FROM app_credentials WHERE packageName = :packageName")
    suspend fun deleteCredentialsForApp(packageName: String)

    @Query("DELETE FROM app_credentials WHERE id = :id")
    suspend fun deleteCredentialsById(id: Int)

    @Query("DELETE FROM app_credentials where appName = :appName")
    suspend fun deleteCredentialsByAppName(appName: String)

    @Query("SELECT * FROM app_credentials")
    suspend fun getAllCredentials(): List<AppCredentials>

    @Query("SELECT * FROM app_credentials WHERE appName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    suspend fun searchCredentials(query: String): List<AppCredentials>
}
