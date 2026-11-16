package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import com.example.magiceightball.core.domain.model.CharacterLook
import com.lorenzosuarez.magiceightball.phone.R

/**
 * A look, as it is drawn: SVG paths in a box of [LOOK_BOX] across, in which the ball is a circle
 * of radius [LOOK_BALL_RADIUS] about ([LOOK_BALL_X], [LOOK_BALL_Y]). Bottom to top:
 *
 * @property behind in the ball's ink, under the ball: what stands beside it and runs behind.
 * @property ink in the ball's ink, over the ball, with a rim of the ground round it so that it
 * is told from the ball it sits on.
 * @property white what is cut out of it in white: the detail that says what the shape is.
 * @property dot in ink again, over the white: the pupil of an eye.
 * @property lift how far up the whole figure stands, in the box's own measure, where the look
 * leaves the room over the ball empty.
 */
internal class LookArt(
    val behind: String? = null,
    val ink: String? = null,
    val white: String? = null,
    val dot: String? = null,
    val lift: Float = 0f
)

/** A look ready to be drawn: its paths read once, however many times it is drawn after. */
internal class LookPaths(val behind: Path?, val ink: Path?, val white: Path?, val dot: Path?, val lift: Float)

/** The paths of [look]; null for the one that wears nothing. Read on the thread that draws, and kept. */
internal fun lookPaths(look: CharacterLook): LookPaths? {
    if (look in read) return read[look]
    val paths = lookArt(look)?.let { art ->
        LookPaths(art.behind?.toPath(), art.ink?.toPath(), art.white?.toPath(), art.dot?.toPath(), art.lift)
    }
    read[look] = paths
    return paths
}

private val read = HashMap<CharacterLook, LookPaths?>()
private val eight by lazy(LazyThreadSafetyMode.NONE) { LOOK_EIGHT.toPath() }
private val eightPlain by lazy(LazyThreadSafetyMode.NONE) { LOOK_EIGHT_PLAIN.toPath() }

private fun String.toPath(): Path = PathParser().parsePathString(this).toPath()

/**
 * The face of a character, whole: the ball with its number and what it wears, filling the
 * round it is drawn in. The ground is not drawn here; where what is worn lies over the ball a
 * rim is cut through to it, so this is drawn on a layer of its own, over the ground.
 *
 * A ball that wears nothing takes the round: larger, in the middle, its number plain to read.
 */
internal fun DrawScope.drawFace(look: CharacterLook, ink: Color) {
    val unit = size.minDimension / LOOK_BOX
    val paths = lookPaths(look)
    withTransform({ scale(unit, unit, pivot = Offset.Zero) }) {
        if (paths == null) {
            drawCircle(ink, radius = 35f, center = Offset(50f, 52f))
            drawOval(Color.White, topLeft = Offset(35f, 32.5f), size = Size(30f, 33f))
            drawPath(eightPlain, ink)
        } else {
            translate(top = paths.lift) {
                paths.behind?.let { drawPath(it, ink) }
                drawCircle(ink, radius = LOOK_BALL_RADIUS, center = Offset(LOOK_BALL_X, LOOK_BALL_Y))
                drawOval(Color.White, topLeft = Offset(39.5f, 55f), size = Size(21f, 23f))
                drawPath(eight, ink)
                drawWorn(paths, ink, rim = null)
            }
        }
    }
}

/**
 * What [paths] has over the ball, in the box's own measure: its ink with a rim round it, then
 * its white, then what is drawn on the white.
 *
 * @param rim the colour of the ground, to draw the rim in; null to cut the rim through whatever
 * has been drawn on this layer, where the ground is under the layer.
 */
internal fun DrawScope.drawWorn(paths: LookPaths, ink: Color, rim: Color?) {
    paths.ink?.let { worn ->
        val round = Stroke(width = LOOK_RIM, join = StrokeJoin.Round)
        if (rim == null) drawPath(worn, Color.Black, style = round, blendMode = BlendMode.Clear) else drawPath(worn, rim, style = round)
        drawPath(worn, ink)
    }
    paths.white?.let { drawPath(it, Color.White) }
    paths.dot?.let { drawPath(it, ink) }
}

/** What a look is called where one is picked: the app's characters lend theirs their names. */
@StringRes
internal fun CharacterLook.labelRes(): Int = when (this) {
    CharacterLook.NEUTRAL -> R.string.character_no_emblem
    CharacterLook.SARCASTIC -> R.string.style_sarcastic
    CharacterLook.MYSTICAL -> R.string.style_mystical
    CharacterLook.GRANDMA -> R.string.style_grandma
    CharacterLook.PIRATE -> R.string.style_pirate
    CharacterLook.PHILOSOPHER -> R.string.style_philosopher
    CharacterLook.COACH -> R.string.style_coach
    CharacterLook.LOVESTRUCK -> R.string.style_lovestruck
    CharacterLook.DRAMA -> R.string.style_drama
    CharacterLook.ROBOT -> R.string.style_robot
    CharacterLook.DETECTIVE -> R.string.style_detective
    CharacterLook.CONSPIRACY -> R.string.style_conspiracy
    CharacterLook.CAT -> R.string.style_cat
    CharacterLook.ZOOMER -> R.string.style_zoomer
    CharacterLook.VILLAIN -> R.string.style_villain
    CharacterLook.POET -> R.string.style_poet
    CharacterLook.ASTRO -> R.string.style_astro
    CharacterLook.THERAPIST -> R.string.style_therapist
    CharacterLook.DOG -> R.string.style_dog
    CharacterLook.SINGER -> R.string.look_singer
    CharacterLook.ROCKSTAR -> R.string.look_rockstar
    CharacterLook.CHEF -> R.string.look_chef
    CharacterLook.WIZARD -> R.string.look_wizard
    CharacterLook.COWBOY -> R.string.look_cowboy
    CharacterLook.MAGICIAN -> R.string.look_magician
    CharacterLook.NINJA -> R.string.look_ninja
    CharacterLook.ANGEL -> R.string.look_angel
    CharacterLook.GRADUATE -> R.string.look_graduate
    CharacterLook.FLOWER -> R.string.look_flower
    CharacterLook.PARTY -> R.string.look_party
}

/** The box a look is drawn in, and the ball in it. */
internal const val LOOK_BOX = 100f
internal const val LOOK_BALL_X = 50f
internal const val LOOK_BALL_Y = 68f
internal const val LOOK_BALL_RADIUS = 30f

/** How wide the rim round what is worn is, in the box's measure: a hair at the size of a row's face. */
private const val LOOK_RIM = 3.4f
