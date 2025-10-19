package me.mirimomekiku.safepass.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.mirimomekiku.safepass.db.dao.AppCredentialsDAO
import me.mirimomekiku.safepass.db.dao.CardCredentialsDAO
import me.mirimomekiku.safepass.db.dao.WebsiteCredentialsDAO
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.db.entity.CardCredentials
import me.mirimomekiku.safepass.db.entity.WebsiteCredentials

@Database(
    entities = [WebsiteCredentials::class, CardCredentials::class, AppCredentials::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun websiteCredentialsDao(): WebsiteCredentialsDAO
    abstract fun cardCredentialsDao(): CardCredentialsDAO
    abstract fun appCredentialsDao(): AppCredentialsDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "safepass_db_secure"
                ).fallbackToDestructiveMigration(true).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
