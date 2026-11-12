package com.example.magiceightball

import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.magiceightball.core.common.BrandPalette
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * The ball as a tile, one swipe from the watch face. It shows the "8" until it is touched; a
 * touch reloads the tile with an answer, and the next touch with another.
 *
 * Its answers are always the bundled ones, in the saved language and style: a tile is touched
 * in passing, and has no time to wait for the AI service nor reason to spend a call on it.
 */
@AndroidEntryPoint
class AnswerTileService : TileService() {

    @Inject
    lateinit var sendMessage: SendMessageUseCase

    @Inject
    lateinit var userSettings: UserSettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        CallbackToFutureAdapter.getFuture { completer ->
            val asked = requestParams.currentState.lastClickableId == ASK_ID
            scope.launch {
                // The system waits only so long for a tile: no settings or no answer by then is the ball at rest.
                val settings = withTimeoutOrNull(TIMEOUT_MS) { userSettings.settings.first() }
                val answer = if (asked && settings != null) {
                    withTimeoutOrNull(TIMEOUT_MS) {
                        val result = sendMessage(
                            trigger = ASK_TRIGGER,
                            languageCode = settings.language.code,
                            personality = settings.personality,
                            useAi = false
                        )
                        (result as? Magic8BallResult.Success)?.message?.takeIf { it.isNotBlank() }
                    }
                } else {
                    null
                }
                val language = settings?.language ?: AppLanguage.ENGLISH
                completer.set(
                    TileBuilders.Tile.Builder()
                        .setResourcesVersion(RESOURCES_VERSION)
                        .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(layout(answer, language)))
                        .build()
                )
            }
            "AnswerTileService.onTileRequest"
        }

    /** The tile uses no images: everything on it is drawn from shapes and text. */
    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> = CallbackToFutureAdapter.getFuture { completer ->
        completer.set(ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build())
        "AnswerTileService.onTileResourcesRequest"
    }

    /** The whole tile is the button: the ball in the middle and, at rest, what to do under it. */
    private fun layout(answer: String?, language: AppLanguage): LayoutElementBuilders.LayoutElement {
        val hint = when (language) {
            AppLanguage.ENGLISH -> "Tap to ask"
            AppLanguage.SPANISH -> "Toca para preguntar"
            AppLanguage.PORTUGUESE -> "Toque para perguntar"
        }
        val ask = ModifiersBuilders.Clickable.Builder()
            .setId(ASK_ID)
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build()
        val column = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(if (answer == null) ballAtRest() else ballWith(answer))
        if (answer == null) {
            column
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(8f)).build())
                .addContent(text(hint, HINT_SP, BrandPalette.ON_SURFACE_QUIET, bold = false))
        }
        return LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(ask).build())
            .addContent(column.build())
            .build()
    }

    private fun ballAtRest(): LayoutElementBuilders.LayoutElement =
        disc(BALL_DP, BALL_COLOR, disc(EIGHT_DISC_DP, WHITE, text("8", EIGHT_SP, BALL_COLOR, bold = true)))

    private fun ballWith(answer: String): LayoutElementBuilders.LayoutElement = disc(
        ANSWER_BALL_DP,
        BALL_COLOR,
        disc(
            WINDOW_DP,
            BrandPalette.ACCENT_DEEP,
            LayoutElementBuilders.Box.Builder()
                .setWidth(dp(ANSWER_TEXT_DP))
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(text(answer, ANSWER_SP, BrandPalette.ON_ACCENT, bold = true))
                .build()
        )
    )

    private fun disc(sizeDp: Float, color: Long, content: LayoutElementBuilders.LayoutElement) =
        LayoutElementBuilders.Box.Builder()
            .setWidth(dp(sizeDp))
            .setHeight(dp(sizeDp))
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(argb(color.toInt()))
                            .setCorner(ModifiersBuilders.Corner.Builder().setRadius(dp(sizeDp / 2f)).build())
                            .build()
                    )
                    .build()
            )
            .addContent(content)
            .build()

    private fun text(value: String, sizeSp: Float, color: Long, bold: Boolean) =
        LayoutElementBuilders.Text.Builder()
            .setText(value)
            .setMaxLines(ANSWER_MAX_LINES)
            .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
            .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(sizeSp))
                    .setWeight(if (bold) LayoutElementBuilders.FONT_WEIGHT_BOLD else LayoutElementBuilders.FONT_WEIGHT_NORMAL)
                    .setColor(argb(color.toInt()))
                    .build()
            )
            .build()

    private companion object {
        const val ASK_ID = "ask"
        const val ASK_TRIGGER = "tile"
        const val RESOURCES_VERSION = "1"
        const val TIMEOUT_MS = 3_000L

        /** A lighter black than the phone's ball: on a watch the tile's own background is black. */
        const val BALL_COLOR = BrandPalette.BALL_CLOSE_TOP
        const val WHITE = 0xFFFFFFFF

        const val BALL_DP = 96f
        const val EIGHT_DISC_DP = 46f
        const val EIGHT_SP = 30f
        const val HINT_SP = 13f

        const val ANSWER_BALL_DP = 148f
        const val WINDOW_DP = 124f
        const val ANSWER_TEXT_DP = 100f
        const val ANSWER_SP = 15f
        const val ANSWER_MAX_LINES = 4
    }
}
