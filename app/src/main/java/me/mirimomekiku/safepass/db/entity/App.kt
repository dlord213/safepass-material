package me.mirimomekiku.safepass.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "app_credentials")
data class AppCredentials(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val appName: String,
    val packageName: String,
    val username: String,
    val password: String,
    val notes: String? = null
) : Parcelable
