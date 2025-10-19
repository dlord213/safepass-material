package me.mirimomekiku.safepass.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "website_credentials")
data class WebsiteCredentials(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val domain: String,
    val label: String,
    val username: String,
    val password: String,
    val notes: String
) : Parcelable