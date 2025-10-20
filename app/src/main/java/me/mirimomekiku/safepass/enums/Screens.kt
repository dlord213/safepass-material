package me.mirimomekiku.safepass.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import me.mirimomekiku.safepass.R


enum class Screens(
    val iconVector: ImageVector? = null,
    val drawableRes: Int? = null
) {
    Safe(Icons.Outlined.Lock),
    Generator(Icons.Outlined.Build),
    Settings(Icons.Outlined.Settings),
    AddWebsite(drawableRes = R.drawable.add_card_24px),
    AddCard(drawableRes = R.drawable.add_card_24px),
    AddApps(drawableRes = R.drawable.apps_24px),
    ViewWebsite(drawableRes = R.drawable.cloud_24px),
    ViewCard(drawableRes = R.drawable.credit_card_24px),
    ViewApps(drawableRes = R.drawable.apps_24px);

    fun routeWithId(id: Int): String {
        return when (this) {
            ViewWebsite -> "view_website/$id"
            ViewCard -> "view_card/$id"
            ViewApps -> "view_app/$id"
            else -> this.name.lowercase()
        }
    }
}