package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.geometry.Rect
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import androidx.compose.ui.graphics.asAndroidPath
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
 * @property behind in the ball's ink, under the ball: what grows from behind it, as ears do.
 * @property ink in the ball's ink, over the ball, with a rim of the ground round it so that it
 * is told from the ball it sits on.
 * @property white what is cut out of it in white: the detail that says what the shape is.
 * @property dot in ink again, over the white: the pupil of an eye.
 * @property fitted whether it is made to the ball's own measure, as a band round it is: then it
 * is drawn as large as it is, where anything else is drawn a size down about the crown.
 */
internal class LookArt(
    val behind: List<String> = emptyList(),
    val ink: List<String> = emptyList(),
    val white: List<String> = emptyList(),
    val dot: List<String> = emptyList(),
    val fitted: Boolean = false
)

/** A look ready to be drawn: its paths read once, however many times it is drawn after. */
internal class LookPaths(
    val behind: List<Path>,
    val ink: List<Path>,
    val white: List<Path>,
    val dot: List<Path>,
    val fitted: Boolean
) {
    /** Whether the ball wears nothing at all. */
    val bare: Boolean get() = behind.isEmpty() && ink.isEmpty() && white.isEmpty()
}

/** The paths of [look]. Read on the thread that draws, and kept. */
internal fun lookPaths(look: CharacterLook): LookPaths = read.getOrPut(look) {
    val art = lookArt(look)
    LookPaths(
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
 */
internal fun DrawScope.drawFigure(look: CharacterLook, ink: Color, centre: Offset, radius: Float, rim: Color?) =
    drawFigure(listOf(Worn(lookPaths(look))), ink, centre, radius, rim)

/**
 * Something the figure wears, as it is drawn at one moment.
 *
 * @property grown how large it is beside its own size: under one while it arrives or goes.
 * @property shown how much of it shows, from none to all of it.
 */
private class Worn(val paths: LookPaths, val grown: Float = 1f, val shown: Float = 1f)

/** The figure wearing each of [worn], one over the other: more than one only while one gives way to the next. */
private fun DrawScope.drawFigure(worn: List<Worn>, ink: Color, centre: Offset, radius: Float, rim: Color?) {
    val unit = radius / LOOK_BALL_RADIUS
    val crown = Offset(LOOK_BALL_X, LOOK_BALL_Y - LOOK_BALL_RADIUS)
    val middle = Offset(LOOK_BALL_X, LOOK_BALL_Y)
    // What is worn is a size down beside the board it is drawn on, about the crown it sits on.
    fun Worn.size() = (if (paths.fitted) 1f else FIGURE_WORN) * grown
    // What is made to the ball's own measure is round the ball, and grows about its middle.
    fun Worn.pivot() = if (paths.fitted) middle else crown
    withTransform({
        translate(centre.x, centre.y)
        rotate(FIGURE_TILT, pivot = Offset.Zero)
        scale(unit, unit, pivot = Offset.Zero)
        translate(-LOOK_BALL_X, -LOOK_BALL_Y)
    }) {
        worn.forEach { one ->
            if (one.paths.behind.isNotEmpty()) {
                faded(one.shown) { withTransform({ scale(one.size(), one.size(), pivot = one.pivot()) }) { one.paths.behind.forEach { drawPath(it, ink) } } }
            }
        }
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
        drawPath(eight, ink)
        worn.forEach { one ->
            faded(one.shown) {
                withTransform({ scale(one.size(), one.size(), pivot = one.pivot()) }) {
                    // Every rim before any of the ink: a shape's rim never cuts into the shape beside it.
                    val round = Stroke(width = LOOK_RIM, join = StrokeJoin.Round)
                    one.paths.ink.forEach { shape ->
                        if (rim == null) drawPath(shape, Color.Black, style = round, blendMode = BlendMode.Clear) else drawPath(shape, rim, style = round)
                    }
                    one.paths.ink.forEach { drawPath(it, ink) }
                    one.paths.white.forEach { drawPath(it, Color.White) }
                    one.paths.dot.forEach { drawPath(it, ink) }
                }
            }
        }
    }
}

/**
 * What [draw] draws, as one thing that shows by [shown]: drawn apart and then laid down, so that
 * shapes lying over each other fade as the one thing they make and not each by itself.
 */
private inline fun DrawScope.faded(shown: Float, draw: DrawScope.() -> Unit) {
    when {
        shown <= 0f -> Unit
        shown >= 1f -> draw()
        else -> {
            val canvas = drawContext.canvas
            canvas.saveLayer(Rect(-LOOK_BOX, -LOOK_BOX, LOOK_BOX * 2, LOOK_BOX * 2), Paint().apply { alpha = shown })
            draw()
            canvas.restore()
        }
    }
}

/**
 * The face of a character in a square: the figure coming into it from its lower corner, as much
 * of the ball in sight as leaves what it wears whole. It is the same for every look, so that in
 * a list the balls and their numbers stand in one column.
 *
 * @param rim the colour of the ground the square is drawn on; null where the ground is under a
 * layer this is drawn on, and the rim round what is worn is cut through to it.
 */
internal fun DrawScope.drawFace(look: CharacterLook, ink: Color, rim: Color? = null) {
    val unit = size.minDimension / LOOK_BOX
    drawFigure(look = look, ink = ink, centre = Offset(FIGURE_X * unit, FIGURE_Y * unit), radius = FIGURE_RADIUS * unit, rim = rim)
}

/**
 * The face while what it wears is changed: the ball stays where it is, what it wore draws in
 * and goes, and what it wears now grows onto the crown, a little past its size and back.
 *
 * @param arrived how far the change has come, from 0 to 1, and past 1 for a moment where it springs.
 * @param rim the colour of the ground the square is drawn on: there is no cutting through to it here.
 */
internal fun DrawScope.drawFaceChanging(left: CharacterLook, worn: CharacterLook, arrived: Float, ink: Color, rim: Color) {
    val unit = size.minDimension / LOOK_BOX
    // What goes is gone at once and what comes begins as it has gone: at no moment are both in sight.
    val going = (arrived / CHANGE_GONE_BY).coerceIn(0f, 1f)
    val coming = ((arrived - CHANGE_COMES_AT) / (CHANGE_HERE_BY - CHANGE_COMES_AT)).coerceIn(0f, 1f)
    val leaving = lookPaths(left)
    val arriving = lookPaths(worn)
    drawFigure(
        worn = listOf(
            // What is round the ball goes outwards from it and comes on from outside: drawn in, it would cross the face.
            Worn(leaving, grown = if (leaving.fitted) 1f + CHANGE_DRAWN_IN * going else 1f - CHANGE_DRAWN_IN * going, shown = 1f - going),
            Worn(
                paths = arriving,
                grown = if (arriving.fitted) 1f + CHANGE_FITTED_FROM * (1f - arrived.coerceAtMost(1f)) else CHANGE_FROM + (1f - CHANGE_FROM) * arrived,
                shown = coming
            )
        ),
        ink = ink,
        centre = Offset(FIGURE_X * unit, FIGURE_Y * unit),
        radius = FIGURE_RADIUS * unit,
        rim = rim
    )
}

/** When, of the way from one look to the next, the old one is gone, the new one begins to show and is all there. */
private const val CHANGE_GONE_BY = 0.2f
private const val CHANGE_COMES_AT = 0.2f
private const val CHANGE_HERE_BY = 0.45f

/**
 * How far what goes draws in, from what size what comes grows, so that it is seen to land and
 * spring once past its size, and how far outside its place what is round the ball comes on from.
 */
private const val CHANGE_DRAWN_IN = 0.1f
private const val CHANGE_FROM = 0.5f
private const val CHANGE_FITTED_FROM = 0.12f

/**
 * Where the figure of [look] is: points along the edge of the ball and of all it wears, each as
 * how far across and how far down it is from the middle of the ball, in the ball's own radius,
 * and tipped as the figure is. For whoever has to keep something clear of it.
 */
internal fun figureOutline(look: CharacterLook): FloatArray = outlines.getOrPut(look) {
    val paths = lookPaths(look)
    val worn = if (paths.fitted) 1f else FIGURE_WORN
    val crown = LOOK_BALL_Y - LOOK_BALL_RADIUS
    val tipped = Math.toRadians(FIGURE_TILT.toDouble())
    val across = cos(tipped).toFloat()
    val down = sin(tipped).toFloat()
    val points = ArrayList<Float>()
    fun add(x: Float, y: Float) {
        points += (x * across - y * down) / LOOK_BALL_RADIUS
        points += (x * down + y * across) / LOOK_BALL_RADIUS
    }
    (paths.behind + paths.ink).forEach { path ->
        // Each point comes as how far along the path it is, then where.
        val along = path.asAndroidPath().approximate(OUTLINE_WITHIN)
        for (at in along.indices step 3) {
            add((along[at + 1] - LOOK_BALL_X) * worn, crown + (along[at + 2] - crown) * worn - LOOK_BALL_Y)
        }
    }
    repeat(OUTLINE_OF_BALL) { step ->
        val angle = 2 * PI * step / OUTLINE_OF_BALL
        add(LOOK_BALL_RADIUS * cos(angle).toFloat(), LOOK_BALL_RADIUS * sin(angle).toFloat())
    }
    points.toFloatArray()
}

private val outlines = HashMap<CharacterLook, FloatArray>()

/** How near the points follow a curve, in the box's measure, and how many there are round the ball. */
private const val OUTLINE_WITHIN = 0.5f
private const val OUTLINE_OF_BALL = 72

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
    CharacterLook.MATE -> R.string.look_mate
    CharacterLook.BOTTLE -> R.string.look_bottle
    CharacterLook.SHARK -> R.string.look_shark
    CharacterLook.PUNK -> R.string.look_punk
    CharacterLook.VIKING -> R.string.look_viking
    CharacterLook.MOUSE -> R.string.look_mouse
    CharacterLook.BUNNY -> R.string.look_bunny
    CharacterLook.BIRD -> R.string.look_bird
    CharacterLook.PROPELLER -> R.string.look_propeller
    CharacterLook.BOW -> R.string.look_bow
}

/** The box a look is drawn in, and the ball in it. */
internal const val LOOK_BOX = 100f
internal const val LOOK_BALL_X = 50f
internal const val LOOK_BALL_Y = 66f
internal const val LOOK_BALL_RADIUS = 28f

/** How wide the rim round what is worn is, in the box's measure, half of it outside the shape: a hair at the size of a row's face. */
private const val LOOK_RIM = 5f
