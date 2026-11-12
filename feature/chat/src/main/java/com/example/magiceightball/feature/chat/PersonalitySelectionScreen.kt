package com.example.magiceightball.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.feature.chat.components.BallColors
import com.example.magiceightball.feature.chat.components.SettingsLink
import com.example.magiceightball.feature.chat.components.SettingsMenu
import com.example.magiceightball.feature.chat.components.SettingsOption

@Composable
fun PersonalitySelectionScreen(
    selectedPersonality: Magic8BallPersonality,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    // The styles do not all fit a watch page, and this page is one of several swiped through:
    // it shows the style in use, and a tap opens the whole list over it.
    var choosing by rememberSaveable { mutableStateOf(false) }
    SettingsMenu(title = localizedString(Strings.PersonalityTitle)) {
        SettingsLink(label = localizedString(selectedPersonality.label()), onClick = { choosing = true })
    }
    if (choosing) {
        Dialog(
            onDismissRequest = { choosing = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PersonalityList(
                selectedPersonality = selectedPersonality,
                onPersonalitySelected = {
                    onPersonalitySelected(it)
                    choosing = false
                }
            )
        }
    }
}

/** Every style, in a list that scrolls and opens on the one in use. */
@Composable
internal fun PersonalityList(
    selectedPersonality: Magic8BallPersonality,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    val styles = Magic8BallPersonality.entries
    // The title is the first item: the style in use is one further down.
    val list = rememberScalingLazyListState(initialCenterItemIndex = styles.indexOf(selectedPersonality) + 1)
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BallColors.Background)
            .selectableGroup(),
        state = list
    ) {
        item {
            Text(
                text = localizedString(Strings.PersonalityTitle),
                modifier = Modifier.semantics { heading() },
                color = BallColors.OnSurfaceQuiet,
                style = MaterialTheme.typography.bodySmall
            )
        }
        items(styles.size) { index ->
            val style = styles[index]
            SettingsOption(
                label = localizedString(style.label()),
                isSelected = style == selectedPersonality,
                onClick = { onPersonalitySelected(style) },
                modifier = Modifier.padding(horizontal = LIST_SIDE_PADDING)
            )
        }
    }
}

private val LIST_SIDE_PADDING = 20.dp

private fun Magic8BallPersonality.label(): LocalizedString = when (this) {
    Magic8BallPersonality.SARCASTIC -> Strings.PersonalitySarcastic
    Magic8BallPersonality.MYSTICAL -> Strings.PersonalityMystical
    Magic8BallPersonality.NEUTRAL -> Strings.PersonalityNeutral
    Magic8BallPersonality.GRANDMA -> Strings.PersonalityGrandma
    Magic8BallPersonality.PIRATE -> Strings.PersonalityPirate
    Magic8BallPersonality.PHILOSOPHER -> Strings.PersonalityPhilosopher
    Magic8BallPersonality.COACH -> Strings.PersonalityCoach
    Magic8BallPersonality.LOVESTRUCK -> Strings.PersonalityLovestruck
    Magic8BallPersonality.DRAMA -> Strings.PersonalityDrama
    Magic8BallPersonality.ROBOT -> Strings.PersonalityRobot
    Magic8BallPersonality.DETECTIVE -> Strings.PersonalityDetective
    Magic8BallPersonality.CONSPIRACY -> Strings.PersonalityConspiracy
    Magic8BallPersonality.CAT -> Strings.PersonalityCat
    Magic8BallPersonality.ZOOMER -> Strings.PersonalityZoomer
    Magic8BallPersonality.VILLAIN -> Strings.PersonalityVillain
    Magic8BallPersonality.POET -> Strings.PersonalityPoet
    Magic8BallPersonality.ASTRO -> Strings.PersonalityAstro
    Magic8BallPersonality.THERAPIST -> Strings.PersonalityTherapist
    Magic8BallPersonality.DOG -> Strings.PersonalityDog
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun PersonalitySelectionScreenPreview() {
    PersonalitySelectionScreen(
        selectedPersonality = Magic8BallPersonality.SARCASTIC,
        onPersonalitySelected = {}
    )
}
