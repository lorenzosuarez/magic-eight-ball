package com.lorenzosuarez.magiceightball.phone.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Whose account it is, as a round: the picture Google has of them, and until it has come, or
 * where there is none, the first letter of their name. It says nothing to whoever has the
 * screen read out: the name beside it does.
 *
 * @param small where it stands beside a line of text rather than over a name.
 */
@Composable
internal fun AccountAvatar(name: String, photoUrl: String?, size: Dp, modifier: Modifier = Modifier, small: Boolean = false) {
    val cache = LocalContext.current.applicationContext.cacheDir
    val pixels = with(LocalDensity.current) { size.roundToPx() }
    // Shown before, it is shown at once: no letter in its place for a moment each time the row comes back.
    val picture by produceState(initialValue = photoUrl?.let { Avatars.shown(it, pixels) }, photoUrl, pixels) {
        if (value == null) value = photoUrl?.let { withContext(Dispatchers.IO) { Avatars.load(cache, it, pixels) } }
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA))
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        val shown = picture
        if (shown != null) {
            Image(bitmap = shown, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(
                text = name.trim().take(1).uppercase(),
                // Beside a line of text it is a letter of that line's size.
                style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * The pictures of accounts: fetched once, at the size they are shown, and kept among what the
 * system may clear, so that the next launch shows one without asking for it again.
 *
 * ponytail: one small picture, so a few lines do what an image library would. Take one on
 * (Coil) when the app shows pictures that other people uploaded.
 */
internal object Avatars {

    /** [url] asking for a picture [pixels] across: Google's say how large they are at their end. */
    fun sized(url: String, pixels: Int): String =
        if (GOOGLE_SIZE.containsMatchIn(url)) url.replace(GOOGLE_SIZE, "=s$pixels-c") else url

    /** The picture at [url] if it has been loaded since the app started. */
    fun shown(url: String, pixels: Int): ImageBitmap? = synchronized(loaded) { loaded[sized(url, pixels)] }

    /** The picture at [url], from [cache] when it is there; null when it cannot be had, which is never worth a word. */
    fun load(cache: File, url: String, pixels: Int): ImageBitmap? = runCatching {
        val address = sized(url, pixels)
        // Only over a connection nobody can read or change on the way.
        require(address.startsWith("https://"))
        val kept = File(cache, "avatar-" + address.hashCode().toUInt().toString(16))
        if (!kept.exists()) {
            val connection = URL(address).openConnection() as HttpURLConnection
            val arriving = File(cache, kept.name + ".part")
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.inputStream.use { from ->
                    arriving.outputStream().use { to ->
                        // A picture of a face at this size is a few thousand bytes: what is far more is not one.
                        val buffer = ByteArray(8 * 1024)
                        var total = 0
                        while (true) {
                            val read = from.read(buffer)
                            if (read < 0) break
                            total += read
                            require(total <= MOST_BYTES)
                            to.write(buffer, 0, read)
                        }
                    }
                }
                arriving.renameTo(kept)
            } finally {
                connection.disconnect()
                // Cut short, what had arrived is no use to anyone.
                arriving.delete()
            }
        }
        // Read no larger than it is shown: one served at another size than was asked for is brought down to it.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(kept.path, bounds)
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= pixels) sample *= 2
        val picture = BitmapFactory.decodeFile(kept.path, BitmapFactory.Options().apply { inSampleSize = sample })
        if (picture == null) {
            // Not a picture: not kept, so that it is asked for again another time.
            kept.delete()
            null
        } else {
            picture.asImageBitmap().also { synchronized(loaded) { loaded[address] = it } }
        }
    }.getOrNull()

    /** What has been loaded since the app started: an account or two, so nothing is ever let go of. */
    private val loaded = mutableMapOf<String, ImageBitmap>()

    private val GOOGLE_SIZE = Regex("=s\\d+(-c)?$")
    private const val TIMEOUT_MS = 6_000
    private const val MOST_BYTES = 512 * 1024
}
