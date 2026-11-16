package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.CharacterPage
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.characters.CharactersQuery
import com.lorenzosuarez.magiceightball.phone.di.SampleCommunityCharacters
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The made-up characters of the build that is tried out, as that build shows them: their shelf,
 * and the ones that say least and most opened. What is looked at here is what is on the phone.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = "w432dp-h768dp-400dpi")
class SampleCommunityScreenshotTest {

    /** The bank at once, with none of the wait it is handed out with on a phone. */
    private object Bank : CommunityCharacters {
        override val open: Boolean = true
        override suspend fun page(after: String?, limit: Int) = CharacterPage(SampleCommunityCharacters.all.take(limit), next = null)
    }

    @Test
    fun the_shelf_of_the_bank() = listOf("phone_110_sample_community_dark" to true, "phone_111_sample_community_light" to false).forEach { (name, dark) ->
        captureRoboImage(filePath = "build/outputs/roborazzi/$name.png") {
            MagicEightBallPhoneTheme(dark = dark) {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    CharactersShown(query = CharactersQuery(shelf = CharacterShelf.COMMUNITY), community = Bank, onCreate = {})
                }
            }
        }
    }

    @Test
    fun the_shortest_and_the_longest_opened() {
        val all = SampleCommunityCharacters.all
        listOf(
            "phone_112_sample_opened_first_dark" to all.first(),
            "phone_113_sample_opened_shortest_light" to all.minBy { it.description.length },
            "phone_114_sample_opened_longest_dark" to all.maxBy { it.description.length }
        ).forEach { (name, listing) ->
            captureRoboImage(filePath = "build/outputs/roborazzi/$name.png") {
                MagicEightBallPhoneTheme(dark = name.endsWith("dark")) {
                    ProvideAppLanguage(AppLanguage.SPANISH) {
                        Surface(color = MaterialTheme.colorScheme.sheetGround) {
                            CharacterDetail(listing = listing, answering = false, onUse = {}, modifier = Modifier.padding(top = 24.dp))
                        }
                    }
                }
            }
        }
    }
}
