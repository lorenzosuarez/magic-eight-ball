package com.example.magiceightball.core.domain.model

/**
 * What was said to the ball, written as the question it is. A speech recognizer hears words and
 * not the tone they were said in, and in Spanish a question and a statement are often the same
 * words: whatever is said to the ball is asked of it, so it is given its question marks here,
 * for the screen, the history and the AI service alike. Blank stays blank.
 */
fun String.asSpokenQuestion(languageCode: String): String {
    val said = trim().trim('¿', '?', '¡', '!', '.', '…', ',', ' ')
    if (said.isEmpty()) return ""
    val worded = said.replaceFirstChar { it.uppercase() }
    return if (languageCode == "es") "¿$worded?" else "$worded?"
}
