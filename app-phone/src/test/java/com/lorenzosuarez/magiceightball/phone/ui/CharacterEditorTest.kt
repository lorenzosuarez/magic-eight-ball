package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.Bought
import com.lorenzosuarez.magiceightball.phone.account.CharacterSave
import com.lorenzosuarez.magiceightball.phone.account.Offer
import com.lorenzosuarez.magiceightball.phone.account.Profile
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import com.lorenzosuarez.magiceightball.phone.config.AppConfig
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Making a character of one's own, as the user meets it: from "Create" to the one that answers. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h2400dp-xhdpi")
class CharacterEditorTest {

    private val compose = createComposeRule()

    /** See [HistoryPageTest]: the rule's empty activity is declared to Robolectric here. */
    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(
            object : ExternalResource() {
                override fun before() {
                    val context = ApplicationProvider.getApplicationContext<Application>()
                    shadowOf(context.packageManager).addOrUpdateActivity(
                        ActivityInfo().apply {
                            packageName = context.packageName
                            name = ComponentActivity::class.java.name
                            theme = android.R.style.Theme_Material_NoActionBar
                        }
                    )
                }
            }
        )
        .around(compose)

    private fun count(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().size

    private fun showEditor(draft: CharacterDraft, isNew: Boolean = true, onSave: (String, String) -> Unit = { _, _ -> }, onDelete: (() -> Unit)? = null) =
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    CharacterEditorContent(
                        draft = draft,
                        isNew = isNew,
                        busy = false,
                        trouble = null,
                        onSave = onSave,
                        onCancel = {},
                        onDelete = onDelete
                    )
                }
            }
        }

    @Test
    fun `a character is saved once it has a name and something said of it, with the look picked for it`() {
        val draft = CharacterDraft()
        val saved = mutableListOf<Pair<String, String>>()
        showEditor(draft, onSave = { name, description -> saved += name to description })
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        // Why it waits is said while it does.
        compose.onNodeWithText("Ponle un nombre y di cómo responde").assertIsDisplayed()
        // Before the button, and said in full: where saving keeps it and where it is sent.
        compose.onNodeWithText("Se guarda en tu cuenta y se envía a Gemini, de Google, cuando este personaje responde. No pongas nada personal.")
            .assertIsDisplayed()

        compose.onNodeWithText("Nombre").performTextInput("Capitán Nemo")
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        // The field shows an example of what to write for as long as nothing is written.
        compose.onNodeWithText("Un capitán gruñón que todo lo compara con el mar").performTextInput("ok")
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        // Typed on after what is there: a few words more, and it says something.
        compose.onNodeWithText("ok").performTextInput(", pero gruñón como el mar ")
        // The colour of one of the app's characters, and what another wears: the first of each name is the colour.
        compose.onAllNodesWithContentDescription("Pirata")[0].performScrollTo().performClick()
        compose.onAllNodesWithContentDescription("Gato")[1].performScrollTo().performClick()

        assertEquals(0, count("Ponle un nombre y di cómo responde"))
        compose.onNodeWithText("Guardar").assertIsEnabled().performClick()
        assertEquals(listOf("Capitán Nemo" to "ok, pero gruñón como el mar"), saved)
        // What is worn is named, since a small drawing may not say it.
        compose.onNodeWithText("Accesorio \u00b7 Gato").assertIsDisplayed()
        assertEquals(Magic8BallPersonality.PIRATE, draft.colour)
        assertEquals(Magic8BallPersonality.CAT, draft.emblem)
        // The card follows what is typed.
        compose.onNodeWithText("— Capitán Nemo").assertIsDisplayed()
    }

    @Test
    fun `what is typed is kept to a name's length and a description's, and a name to one line`() {
        val draft = CharacterDraft()
        draft.typeName("Capitán\nNemo el del fondo del mar")
        draft.typeDescription("a".repeat(400))

        assertEquals("Capitán Nemo el ", draft.name)
        assertEquals(CustomCharacter.MAX_DESCRIPTION_LENGTH, draft.descriptionLength)
        assertEquals("Capitán Nemo el", draft.nameToSave)
        assertNull(CharacterDraft().nameToSave)
        // One kept longer than it should be is cut as it is opened, and counted as cut.
        assertEquals(CustomCharacter.MAX_DESCRIPTION_LENGTH, CharacterDraft(NEMO.copy(description = "b".repeat(300))).descriptionLength)
    }

    @Test
    fun `one that exists opens on what it is, and can be deleted from there`() {
        var asked = 0
        showEditor(CharacterDraft(NEMO), isNew = false, onDelete = { asked++ })

        compose.onNodeWithText("Tu personaje").assertIsDisplayed()
        compose.onNodeWithText("Capitán Nemo").assertIsDisplayed()
        compose.onNodeWithText(NEMO.description).assertIsDisplayed()
        compose.onNodeWithText("Guardar").assertIsEnabled()
        compose.onNodeWithText("Eliminar personaje").performScrollTo().performClick()
        assertEquals(1, asked)
    }

    @Test
    fun `a new one has nothing to delete`() {
        showEditor(CharacterDraft())

        compose.onNodeWithText("Nuevo personaje").assertIsDisplayed()
        assertEquals(0, count("Eliminar personaje"))
    }

    // ---- from the page of options

    private var state by mutableStateOf(EightBallUiState())
    private val chosen = mutableListOf<CustomCharacter>()
    private val saved = mutableListOf<CustomCharacter?>()
    private var signIns = 0

    private fun showOptions(
        account: AccountState,
        characters: List<CustomCharacter>?,
        profile: ProfileState = ProfileState.Present(LORENA),
        sold: Boolean = false,
        premium: Boolean = true,
        price: String? = null,
        onBuyPremium: suspend () -> Bought = { Bought.FAILED }
    ) {
        state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            config = AppConfig(accountEnabled = true, premiumEnabled = sold),
            account = account,
            profile = profile,
            characters = characters,
            premium = premium,
            offer = price?.let(::Offer)
        )
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        SettingsContent(
                            state = state,
                            onLanguageSelected = {},
                            onPersonalitySelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {},
                            onSignIn = {
                                signIns++
                                AccountOutcome.FAILED
                            },
                            onChooseCharacter = { chosen += it },
                            onSaveCharacter = { replacing, _, _, _, _ ->
                                saved += replacing
                                CharacterSave.Failed
                            },
                            onBuyPremium = onBuyPremium
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Ver todos").performScrollTo().performClick()
    }

    @Test
    fun `whoever has no account is shown the way to one, and told when it led nowhere`() {
        showOptions(AccountState.SignedOut, characters = null)
        assertEquals(0, count("Tuyos"))

        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Para crear personajes necesitas una cuenta").assertIsDisplayed()
        assertEquals(0, count("Nuevo personaje"))

        // The page's own button is under the sheet: the dialog's is the later of the two.
        compose.onAllNodesWithText("Continuar con Google")[1].performClick()
        compose.waitForIdle()
        assertEquals(1, signIns)
        compose.onNodeWithText("No se pudo entrar").assertIsDisplayed()
        compose.onNodeWithText("Revisa tu conexión e inténtalo de nuevo en un momento.").assertIsDisplayed()
    }

    @Test
    fun `signed in, create opens on a new character, and yours are listed first with the way to correct each`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO))
        compose.onNodeWithText("Tuyos").assertIsDisplayed()

        compose.onNodeWithContentDescription("Editar Capitán Nemo").performClick()
        compose.onNodeWithText("Tu personaje").assertIsDisplayed()
        compose.onNodeWithText("Guardar").performScrollTo().performClick()
        compose.waitForIdle()
        // Saved over the one that was opened; and what could not be saved is said by the button.
        assertEquals(listOf<CustomCharacter?>(NEMO), saved)
        compose.onNodeWithText("No se pudo guardar", substring = true).assertIsDisplayed()
    }

    @Test
    fun `one of yours is chosen with a touch`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO))

        compose.onNodeWithText("Capitán Nemo").performClick()
        compose.waitForIdle()

        assertEquals(listOf(NEMO), chosen)
    }

    @Test
    fun `with five there is no room for another, and with the account unheard of nothing is made on a guess`() {
        showOptions(SIGNED_IN, characters = (1..5).map { NEMO.copy(id = it.toString(), name = "Nemo $it") })
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Ya tienes cinco personajes").assertIsDisplayed()
        compose.onNodeWithText("Entendido").performClick()

        state = state.copy(characters = null)
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("No se pudo conectar con tu cuenta").assertIsDisplayed()
        assertEquals(0, count("Nuevo personaje"))
    }

    @Test
    fun `once it is sold, whoever has not paid is shown what paying brings, and the way back`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false)

        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Tus propios personajes, con Premium").assertIsDisplayed()
        // Said before the button: what saying no costs, which is nothing.
        compose.onNodeWithText("Los personajes de la\u00A0app siguen siendo gratis.").assertIsDisplayed()
        // The store has not named its price: the button is there, buys nothing, and says why.
        compose.onNodeWithText("Obtener Premium").assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText("Buscando el precio en Google\u00A0Play…").assertIsDisplayed()
        compose.onNodeWithText("Un solo pago, sin suscripción.\nQueda en tu cuenta de la\u00A0app.").assertIsDisplayed()
        assertEquals(0, count("Nuevo personaje"))

        compose.onNodeWithText("Ahora no").performClick()
        compose.waitForIdle()
        assertEquals(0, count("Tus propios personajes, con Premium"))
        // What was made before is still theirs to choose and to correct.
        compose.onNodeWithContentDescription("Editar Capitán Nemo").performClick()
        compose.onNodeWithText("Tu personaje").assertIsDisplayed()
    }

    @Test
    fun `buying it goes on to what they came for, and backing out says nothing`() {
        var ends = Bought.CANCELLED
        var asked = 0
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €") {
            asked++
            ends
        }
        compose.onNodeWithContentDescription("Crear un personaje").performClick()

        compose.onNodeWithText(BUY).assertIsEnabled().performClick()
        compose.waitForIdle()
        assertEquals(1, asked)
        // Still there, with nothing said: they changed their mind on Google Play's own screen.
        compose.onNodeWithText(BUY).assertIsEnabled()
        assertEquals(0, count("No se pudo completar la compra. Inténtalo de nuevo"))

        ends = Bought.DONE
        compose.onNodeWithText(BUY).performClick()
        compose.waitForIdle()
        assertEquals(0, count("Tus propios personajes, con Premium"))
        compose.onNodeWithText("Nuevo personaje").assertIsDisplayed()
    }

    @Test
    fun `a purchase that failed says so, and can be tried again`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €") { Bought.FAILED }
        compose.onNodeWithContentDescription("Crear un personaje").performClick()

        compose.onNodeWithText(BUY).performClick()
        compose.waitForIdle()

        compose.onNodeWithText("No se pudo completar la compra. Inténtalo de nuevo").assertIsDisplayed()
        compose.onNodeWithText(BUY).assertIsEnabled()
        compose.onNodeWithText("Ahora no").assertIsEnabled()
        assertEquals(0, count("Nuevo personaje"))
    }

    @Test
    fun `a payment made and not yet checked is checked again, and no price is asked twice`() {
        var ends = Bought.UNCONFIRMED
        var asked = 0
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €") {
            asked++
            ends
        }
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText(BUY).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(
            "El pago está hecho, pero no pudimos confirmarlo. Comprueba de nuevo: no se te cobrará otra\u00A0vez"
        ).assertIsDisplayed()
        assertEquals(0, count(BUY))

        ends = Bought.DONE
        compose.onNodeWithText("Comprobar de nuevo").performClick()
        compose.waitForIdle()
        assertEquals(2, asked)
        compose.onNodeWithText("Nuevo personaje").assertIsDisplayed()
    }

    @Test
    fun `a payment Google Play is waiting for is only to be understood`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €") { Bought.PENDING }
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText(BUY).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(
            "Google\u00A0Play está esperando el pago. Premium se activará automáticamente cuando se\u00A0complete"
        ).assertIsDisplayed()
        // Nothing here sells it a second time, and there is one way on.
        assertEquals(0, count(BUY))
        assertEquals(0, count("Ahora no"))

        compose.onNodeWithText("Entendido").performClick()
        compose.waitForIdle()
        assertEquals(0, count("Tus propios personajes, con Premium"))
        assertEquals(0, count("Nuevo personaje"))
    }

    @Test
    fun `what another account holds is not sold here, and the way to it is said`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €") { Bought.TAKEN }
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText(BUY).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(
            "Premium ya se compró con esta cuenta de Google\u00A0Play, en otra cuenta de la app. " +
                "No se te cobró de nuevo. Cierra sesión y entra con\u00A0esa"
        ).assertIsDisplayed()
        assertEquals(0, count(BUY))
        assertEquals(0, count("Obtener Premium"))

        compose.onNodeWithText("Cerrar").performClick()
        compose.waitForIdle()
        assertEquals(0, count("Tus propios personajes, con Premium"))
    }

    @Test
    fun `a payment that comes through while the sheet is open goes on by itself`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €")
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Tus propios personajes, con Premium").assertIsDisplayed()

        state = state.copy(premium = true, offer = null)
        compose.waitForIdle()

        assertEquals(0, count("Tus propios personajes, con Premium"))
        compose.onNodeWithText("Nuevo personaje").assertIsDisplayed()
    }

    @Test
    fun `when it stops being sold the sheet goes, and nothing opens in its place`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = false, price = "2,99 €")
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Tus propios personajes, con Premium").assertIsDisplayed()

        // Switched off from afar, or the app has just come back and does not know yet what is sold.
        state = state.copy(config = AppConfig(accountEnabled = true, premiumEnabled = false))
        compose.waitForIdle()

        assertEquals(0, count("Tus propios personajes, con Premium"))
        assertEquals(0, count("Nuevo personaje"))
    }

    @Test
    fun `whoever has paid makes one as before, and nobody is asked to pay for a sixth`() {
        showOptions(SIGNED_IN, characters = listOf(NEMO), sold = true, premium = true)
        compose.onNodeWithContentDescription("Crear un personaje").performClick()
        compose.onNodeWithText("Nuevo personaje").assertIsDisplayed()
        assertEquals(0, count("Tus propios personajes, con Premium"))
    }

    @Test
    fun `with five already, it is the five that is said, not the price`() {
        showOptions(SIGNED_IN, characters = (1..5).map { NEMO.copy(id = it.toString(), name = "Nemo $it") }, sold = true, premium = false)

        compose.onNodeWithContentDescription("Crear un personaje").performClick()

        compose.onNodeWithText("Ya tienes cinco personajes").assertIsDisplayed()
        assertEquals(0, count("Tus propios personajes, con Premium"))
    }

    @Test
    fun `where accounts are not offered there is no create, and nobody's characters`() {
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH), characters = listOf(NEMO), account = SIGNED_IN)
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    SettingsContent(
                        state = state,
                        onLanguageSelected = {},
                        onPersonalitySelected = {},
                        onAiAnswersChange = {},
                        onVoiceQuestionsChange = {},
                        onAllowMicrophone = {},
                        onShareApp = {}
                    )
                }
            }
        }
        compose.onNodeWithText("Ver todos").performScrollTo().performClick()

        assertEquals(0, compose.onAllNodesWithContentDescription("Crear un personaje").fetchSemanticsNodes().size)
        assertEquals(0, count("Tuyos"))
    }

    private companion object {
        val SIGNED_IN = AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com")
        val LORENA = Profile("Lorena", LocalDate.of(1990, 3, 12))
        val NEMO = CustomCharacter(
            id = "1",
            name = "Capitán Nemo",
            description = "Un capitán gruñón que todo lo compara con el mar.",
            colour = Magic8BallPersonality.PIRATE,
            emblem = Magic8BallPersonality.CAT
        )

        /** The button once the store has named its price: the price never breaks off its line. */
        const val BUY = "Obtener Premium por\u00A02,99\u00A0€"
    }
}
