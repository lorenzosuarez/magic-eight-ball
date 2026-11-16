package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * How things that belong together are listed: each on a tile of its own, the tiles a hair apart,
 * and the group rounded as one thing, so that only its first tile is round at the top and only
 * its last at the bottom. A tile alone is round all the way.
 *
 * It is the one way the app lists rows: whatever lists them takes its shapes and its gap here.
 */
internal fun groupedShape(first: Boolean, last: Boolean): Shape = RoundedCornerShape(
    topStart = if (first) GROUP_CORNER else GROUP_SEAM,
    topEnd = if (first) GROUP_CORNER else GROUP_SEAM,
    bottomStart = if (last) GROUP_CORNER else GROUP_SEAM,
    bottomEnd = if (last) GROUP_CORNER else GROUP_SEAM
)

/** The corner of a group, the corner where two of its tiles meet, and the hair between them. */
internal val GROUP_CORNER = 20.dp
internal val GROUP_SEAM = 4.dp
internal val GROUP_GAP = 2.dp

/** The least a row is tall, and the room at its sides. */
internal val GROUP_ROW_HEIGHT = 64.dp
internal val GROUP_ROW_INSET = 16.dp
