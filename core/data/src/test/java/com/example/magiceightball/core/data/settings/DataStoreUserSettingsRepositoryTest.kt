package com.example.magiceightball.core.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataStoreUserSettingsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val file: File by lazy { File(temporaryFolder.root, "user_settings.preferences_pb") }

    @Test
    fun `first launch follows a spanish device and keeps the other defaults`() = runTest {
        withRepository(deviceLanguageCode = "es") { repository ->
            assertEquals(
                UserSettings(
                    language = AppLanguage.SPANISH,
                    personality = Magic8BallPersonality.SARCASTIC,
                    aiAnswersEnabled = true,
                    voiceQuestionsEnabled = true
                ),
                repository.settings.first()
            )
        }
    }

    @Test
    fun `first launch is in english on a device in any other language`() = runTest {
        withRepository(deviceLanguageCode = "fr") { repository ->
            assertEquals(AppLanguage.ENGLISH, repository.settings.first().language)
        }
    }

    @Test
    fun `every choice survives a new launch`() = runTest {
        withRepository(deviceLanguageCode = "en") { repository ->
            repository.setLanguage(AppLanguage.SPANISH)
            repository.setPersonality(Magic8BallPersonality.MYSTICAL)
            repository.setAiAnswersEnabled(false)
            repository.setVoiceQuestionsEnabled(false)
        }

        withRepository(deviceLanguageCode = "en") { repository ->
            assertEquals(
                UserSettings(
                    language = AppLanguage.SPANISH,
                    personality = Magic8BallPersonality.MYSTICAL,
                    aiAnswersEnabled = false,
                    voiceQuestionsEnabled = false
                ),
                repository.settings.first()
            )
        }
    }

    @Test
    fun `a chosen language wins over the device language`() = runTest {
        withRepository(deviceLanguageCode = "es") { repository ->
            repository.setLanguage(AppLanguage.ENGLISH)
        }

        withRepository(deviceLanguageCode = "es") { repository ->
            assertEquals(AppLanguage.ENGLISH, repository.settings.first().language)
        }
    }

    @Test
    fun `settings saved before voice questions existed keep them on`() = runTest {
        withRepository(deviceLanguageCode = "en") { repository ->
            repository.setPersonality(Magic8BallPersonality.MYSTICAL)
            repository.setAiAnswersEnabled(false)
        }

        withRepository(deviceLanguageCode = "en") { repository ->
            assertEquals(
                UserSettings(
                    personality = Magic8BallPersonality.MYSTICAL,
                    aiAnswersEnabled = false,
                    voiceQuestionsEnabled = true
                ),
                repository.settings.first()
            )
        }
    }

    @Test
    fun `an unreadable file delivers the defaults`() = runTest {
        file.writeText("not a preferences file")

        withRepository(deviceLanguageCode = "en") { repository ->
            assertEquals(UserSettings(), repository.settings.first())
        }
    }

    /** Runs [block] on a repository over [file], then closes the store as a process end would. */
    private suspend fun withRepository(
        deviceLanguageCode: String,
        block: suspend (DataStoreUserSettingsRepository) -> Unit
    ) {
        val storeJob = Job()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + storeJob),
            produceFile = { file }
        )
        try {
            block(DataStoreUserSettingsRepository(dataStore) { deviceLanguageCode })
        } finally {
            storeJob.cancelAndJoin()
        }
    }
}
