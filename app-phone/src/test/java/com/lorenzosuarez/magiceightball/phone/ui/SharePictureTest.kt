package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What stands between a picture that came out empty and whoever it would be sent to, and where
 * the pictures are kept. That one is drawn afresh for every share is seen on a device only: the
 * JVM has no layers to lose.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class SharePictureTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun picture(colour: Int) = Bitmap.createBitmap(108, 135, Bitmap.Config.ARGB_8888).apply { eraseColor(colour) }

    @Test
    fun `a picture of nothing is told from a card`() {
        // See-through from edge to edge: what a layer that lost its drawing gives.
        assertTrue(picture(Color.TRANSPARENT).isBlank())
        // Black all over: what a picture that could not be read back gives.
        assertTrue(picture(Color.BLACK).isBlank())
        // A card: one colour that is never black, and ink on it.
        val card = picture(0xFFFFDD33.toInt())
        assertFalse(card.isBlank())
        card.setPixel(54, 67, Color.BLACK)
        assertFalse(card.isBlank())
    }

    @Test
    fun `a card with a hole in it is not sent either`() {
        val card = picture(0xFF8FA8FF.toInt())
        card.setPixel(card.width - 2, card.height - 2, Color.TRANSPARENT)
        assertTrue(card.isBlank())
    }

    @Test
    fun `each picture has a name of its own, and only the latest few are kept`() {
        val directory = File(folder.root, "share")
        val names = (1L..5L).map { moment ->
            shareFile(directory, now = moment).also {
                it.writeText("picture $moment")
                it.setLastModified(moment * 1_000)
            }.name
        }

        assertEquals(5, names.toSet().size)
        // The one just written and the two before it: whoever is still reading the last one has it.
        assertEquals(names.takeLast(3).toSet(), directory.listFiles().orEmpty().map { it.name }.toSet())
    }
}
