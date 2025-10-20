package me.mirimomekiku.safepass.ui.composables

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import me.mirimomekiku.safepass.R

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val pm = context.packageManager

    val icon: Drawable? = remember(packageName) {
        try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    if (icon != null) {
        Image(
            painter = rememberAsyncImagePainter(model = icon),
            contentDescription = "$packageName icon",
            modifier = modifier.size(40.dp)
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(model = R.drawable.question_mark_24px),
            contentDescription = "App not found",
            modifier = modifier.size(40.dp),
        )
    }
}