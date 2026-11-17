package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.magiceightball.core.common.BrandPalette
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The light around the ball, as the colour of whoever answers and the page it is on make it. */
class BallLightTest {

    private val red = shareColor(Magic8BallPersonality.DRAMA)

    @Test
    fun `on a dark page a character's colour comes out softened, and still its own`() {
        val light = ballLight(DARK.ink, red, BALL_LIGHT_TINT)

        assertTrue(light.luminance() > red.luminance())
        assertTrue(light.luminance() < DARK.ink.luminance())
        assertTrue(light.red > light.green && light.red > light.blue)
    }

    @Test
    fun `on a light page it comes out darkened, and still its own`() {
        val light = ballLight(LIGHT.ink, red, BALL_LIGHT_TINT)

        assertTrue(light.luminance() < red.luminance())
        assertTrue(light.luminance() > LIGHT.ink.luminance())
        assertTrue(light.red > light.green && light.red > light.blue)
    }

    /**
     * What is seen is the page with the light laid over it. With the classic character that
     * comes out as it did when the light was the page's ink and nothing else, on either page.
     */
    @Test
    fun `the classic character leaves the page lit as it was before the ball wore a colour`() = PAGES.forEach { page ->
        val light = ballLight(page.ink, shareColor(Magic8BallPersonality.NEUTRAL), BALL_LIGHT_TINT)
        val weight = ballLightWeight(light, page.ink, page.ground)

        CHANNELS.forEach { channel ->
            val ground = channel(page.ground)
            val before = ground + (channel(page.ink) - ground) * STRONGEST
            val now = ground + (channel(light) - ground) * STRONGEST * weight
            assertEquals(before, now, HARDLY_ANYTHING)
        }
    }

    /**
     * A colour the page swallowed would have to be drawn many times over to show, and one that
     * stood out more than ink hardly at all: no character's is either, on either page.
     */
    @Test
    fun `no character's light is lost on a page, and none glares`() = PAGES.forEach { page ->
        CustomCharacter.LOOKS.forEach { character ->
            val light = ballLight(page.ink, shareColor(character), BALL_LIGHT_TINT)
            val weight = ballLightWeight(light, page.ink, page.ground)

            assertTrue("$character is drawn $weight times over", weight in 0.9f..3f)
        }
    }

    /** A page as the ball stands on it: what is written on it, and what it is. */
    private class Page(val ink: Color, val ground: Color)

    private companion object {
        /** The app's two pages, as [MagicEightBallPhoneTheme] has them. */
        val DARK = Page(ink = Color(BrandPalette.ON_SURFACE), ground = Color(0xFF0D1218))
        val LIGHT = Page(ink = Color(0xFF141B23), ground = Color(0xFFF1F3F7))
        val PAGES = listOf(DARK, LIGHT)

        val CHANNELS = listOf<(Color) -> Float>({ it.red }, { it.green }, { it.blue })

        /** As strong as the light gets: where the three rings lie one over the other. */
        const val STRONGEST = 0.2f

        /** Five of the 255 steps a channel has: less than is told apart on a page. */
        const val HARDLY_ANYTHING = 0.02f
    }
}
