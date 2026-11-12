package com.example.magiceightball.core.domain.model

/** The languages the app can show its texts and answers in. */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    SPANISH("es"),

    /** As it is spoken in Brazil. */
    PORTUGUESE("pt");

    companion object {
        /** Returns the language with this ISO 639 [code], or [ENGLISH] when the app has none. */
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: ENGLISH
    }
}
