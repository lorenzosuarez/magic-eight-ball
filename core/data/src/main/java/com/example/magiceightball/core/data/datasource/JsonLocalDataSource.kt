package com.example.magiceightball.core.data.datasource

import android.content.Context
import com.example.magiceightball.core.data.model.Magic8BallDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException

interface LocalDataSource {
    fun getRandomMessage(languageCode: String): String
}

@Singleton
class JsonLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json // Use the Hilt-provided Json instance
) : LocalDataSource {

    private var cachedData: Magic8BallDataDto? = null

    override fun getRandomMessage(languageCode: String): String {
        val data = getOrLoadData() ?: return "Outlook uncertain." // Fallback if load fails completely

        val list = when (languageCode.lowercase()) {
            "es" -> data.es
            else -> data.en
        }

        if (list.isEmpty()) return "Ask again later."

        return list.random().message
    }

    private fun getOrLoadData(): Magic8BallDataDto? {
        if (cachedData != null) return cachedData

        return try {
            val jsonString = context.assets.open("magic_8_ball_responses.json")
                .bufferedReader()
                .use { it.readText() }
            
            cachedData = json.decodeFromString<Magic8BallDataDto>(jsonString)
            cachedData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
