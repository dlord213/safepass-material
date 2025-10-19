package me.mirimomekiku.safepass.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder

import me.mirimomekiku.safepass.R
import org.silentsoft.simpleicons.SimpleIcons

@Composable
fun getSocialIcon(label: String): Painter? {
    return when (label.lowercase()) {
        "deviantart" -> painterResource(R.drawable.icons8_deviantart)
        "dribble" -> painterResource(R.drawable.icons8_dribbble)
        "facebook" -> painterResource(R.drawable.icons8_facebook)
        "facebook messenger" -> painterResource(R.drawable.icons8_facebook_messenger)
        "flickr" -> painterResource(R.drawable.icons8_flickr)
        "github" -> painterResource(R.drawable.icons8_github)
        "instagram" -> painterResource(R.drawable.icons8_instagram)
        "line" -> painterResource(R.drawable.icons8_line)
        "linkedin" -> painterResource(R.drawable.icons8_linkedin)
        "medium" -> painterResource(R.drawable.icons8_medium)
        "pinterest" -> painterResource(R.drawable.icons8_pinterest)
        "reddit" -> painterResource(R.drawable.icons8_reddit)
        "snapchat" -> painterResource(R.drawable.icons8_snapchat_circled_logo)
        "stack exchange" -> painterResource(R.drawable.icons8_stack_exchange)
        "stack overflow" -> painterResource(R.drawable.icons8_stack_overflow)
        "telegram" -> painterResource(R.drawable.icons8_telegram_app)
        "tiktok" -> painterResource(R.drawable.icons8_tiktok)
        "tumblr" -> painterResource(R.drawable.icons8_tumblr)
        "wechat" -> painterResource(R.drawable.icons8_wechat)
        "whatsapp" -> painterResource(R.drawable.icons8_whatsapp)
        "wordpress" -> painterResource(R.drawable.icons8_wordpress)
        "youtube" -> painterResource(R.drawable.icons8_youtube)
        "twitter" -> painterResource(R.drawable.icons8_x)
        "x" -> painterResource(R.drawable.icons8_x)
        else -> null
    }
}

fun getSocialIconSvg(label: String): String? {
    val icon = SimpleIcons.get(label.lowercase()) ?: return null
    return icon.svg
}

@Composable
fun SocialIcon(label: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val svg = getSocialIconSvg(label) ?: return
    val dataUri = "data:image/svg+xml;utf8,$svg"

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(dataUri)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = label,
        modifier = modifier.size(16.dp)
    )
}
