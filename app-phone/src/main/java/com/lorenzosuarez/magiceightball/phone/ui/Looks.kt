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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import com.example.magiceightball.core.domain.model.CharacterLook
import com.lorenzosuarez.magiceightball.phone.R

/**
 * A look, as it is drawn: SVG paths on a ball that stands upright in a box of [LOOK_BOX] across,
 * a circle of radius [LOOK_BALL_RADIUS] about ([LOOK_BALL_X], [LOOK_BALL_Y]). Each shape is a
 * path of its own and is drawn by itself, so two that overlap never cut holes in each other.
 * Bottom to top:
 *
 * @property beside in the ball's ink, under the ball and as large as drawn: what is fixed to
 * the ball's sides.
 * @property behind in the ball's ink, under the ball: what grows from behind it, as ears do.
 * @property ink in the ball's ink, over the ball, with a rim of the ground round it so that it
 * is told from the ball it sits on.
 * @property white what is cut out of it in white: the detail that says what the shape is.
 * @property dot in ink again, over the white: the pupil of an eye.
 * @property fitted whether it is made to the ball's own measure, as a band round it is: then it
 * is drawn as large as it is, where anything else is drawn a size down about the crown.
 */
internal class LookArt(
    val beside: List<String> = emptyList(),
    val behind: List<String> = emptyList(),
    val ink: List<String> = emptyList(),
    val white: List<String> = emptyList(),
    val dot: List<String> = emptyList(),
    val fitted: Boolean = false
)

/** A look ready to be drawn: its paths read once, however many times it is drawn after. */
internal class LookPaths(
    val beside: List<Path>,
    val behind: List<Path>,
    val ink: List<Path>,
    val white: List<Path>,
    val dot: List<Path>,
    val fitted: Boolean
) {
    /** Whether the ball wears nothing at all. */
    val bare: Boolean get() = beside.isEmpty() && behind.isEmpty() && ink.isEmpty() && white.isEmpty()
}

/** The paths of [look]. Read on the thread that draws, and kept. */
internal fun lookPaths(look: CharacterLook): LookPaths = read.getOrPut(look) {
    val art = lookArt(look)
    LookPaths(
        beside = art.beside.map { it.toPath() },
        behind = art.behind.map { it.toPath() },
        ink = art.ink.map { it.toPath() },
        white = art.white.map { it.toPath() },
        dot = art.dot.map { it.toPath() },
        fitted = art.fitted
    )
}

private val read = HashMap<CharacterLook, LookPaths>()
private val eight by lazy(LazyThreadSafetyMode.NONE) { LOOK_EIGHT.toPath() }

private fun String.toPath(): Path = PathParser().parsePathString(this).toPath()

/**
 * The figure every character is: the ball, large and tipped towards the words, its white disc
 * off to one side as a face, wearing [look] on its tipped crown. Drawn about [centre] with the
 * ball [radius] across its half; whoever draws it decides how much of it is in sight.
 *
 * @param rim the colour of the ground, to draw the rim round what is worn in; null to cut the
 * rim through whatever has been drawn on this layer, where the ground is under the layer.
 * @param tilt how far the figure is tipped: none where what it is drawn in is tipped already.
 * @param number whether the number is drawn on the disc: not where it is set there in type.
 */
internal fun DrawScope.drawFigure(
    look: CharacterLook,
    ink: Color,
    centre: Offset,
    radius: Float,
    rim: Color?,
    tilt: Float = FIGURE_TILT,
    number: Boolean = true
) {
    val paths = lookPaths(look)
    val unit = radius / LOOK_BALL_RADIUS
    // What is worn is a size down beside the board it is drawn on, about the crown it sits on.
    val worn = if (paths.fitted) 1f else FIGURE_WORN
    val crown = Offset(LOOK_BALL_X, LOOK_BALL_Y - LOOK_BALL_RADIUS)
    val middle = Offset(LOOK_BALL_X, LOOK_BALL_Y)
    withTransform({
        translate(centre.x, centre.y)
        rotate(tilt, pivot = Offset.Zero)
        scale(unit, unit, pivot = Offset.Zero)
        translate(-LOOK_BALL_X, -LOOK_BALL_Y)
    }) {
        paths.beside.forEach { drawPath(it, ink) }
        withTransform({ scale(worn, worn, pivot = crown) }) { paths.behind.forEach { drawPath(it, ink) } }
        drawCircle(ink, radius = LOOK_BALL_RADIUS, center = middle)
        val face = Size(LOOK_BALL_RADIUS * FACE_WIDE * 2, LOOK_BALL_RADIUS * FACE_TALL * 2)
        drawOval(
            Color.White,
            topLeft = Offset(
                middle.x + LOOK_BALL_RADIUS * FACE_X - face.width / 2,
                middle.y + LOOK_BALL_RADIUS * FACE_Y - face.height / 2
            ),
            size = face
        )
        if (number) drawPath(eight, ink)
        withTransform({ scale(worn, worn, pivot = crown) }) {
            // Every rim before any of the ink: a shape's rim never cuts into the shape beside it.
            val round = Stroke(width = LOOK_RIM, join = StrokeJoin.Round)
            paths.ink.forEach { shape ->
                if (rim == null) drawPath(shape, Color.Black, style = round, blendMode = BlendMode.Clear) else drawPath(shape, rim, style = round)
            }
            paths.ink.forEach { drawPath(it, ink) }
            paths.white.forEach { drawPath(it, Color.White) }
            paths.dot.forEach { drawPath(it, ink) }
        }
    }
}

/**
 * The face of a character in a round: the figure coming into it from its lower corner, as much
 * of the ball in sight as leaves what it wears whole. A ball that wears nothing has the round
 * to itself, larger and further in. The ground is not drawn here; the rim round what is worn is
 * cut through to it, so this is drawn on a layer of its own, over the ground.
 */
internal fun DrawScope.drawFace(look: CharacterLook, ink: Color) {
    val unit = size.minDimension / LOOK_BOX
    val bare = lookPaths(look).bare
    drawFigure(
        look = look,
        ink = ink,
        centre = Offset((if (bare) BARE_X else FIGURE_X) * unit, (if (bare) BARE_Y else FIGURE_Y) * unit),
        radius = (if (bare) BARE_RADIUS else FIGURE_RADIUS) * unit,
        rim = null
    )
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
internal const val LOOK_BALL_Y = 66f
internal const val LOOK_BALL_RADIUS = 28f

/** How wide the rim round what is worn is, in the box's measure, half of it outside the shape: a hair at the size of a row's face. */
private const val LOOK_RIM = 5f
