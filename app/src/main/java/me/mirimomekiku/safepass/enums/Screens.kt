package me.mirimomekiku.safepass.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector


enum class Screens(val icon: ImageVector) {
    Safe(Icons.Outlined.Lock),
    Generator(Icons.Outlined.Build),
    Settings(Icons.Outlined.Settings),
    AddWebsite(Icons.Outlined.Cloud),
    AddCard(Icons.Outlined.AddCard),
    AddApps(Icons.Outlined.Apps),
    ViewWebsite(Icons.Outlined.Cloud),
    ViewCard(Icons.Outlined.CreditCard),
    ViewApps(Icons.Outlined.Apps);

    fun routeWithId(id: Int): String {
        return when (this) {
            ViewWebsite -> "view_website/$id"
            ViewCard -> "view_card/$id"
            ViewApps -> "view_app/$id"
            else -> this.name.lowercase()
        }
    }
}