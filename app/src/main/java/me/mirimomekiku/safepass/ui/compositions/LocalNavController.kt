package me.mirimomekiku.safepass.ui.compositions

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
    error("No navigation controller provided.")
}