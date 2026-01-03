package com.example.magiceightball.feature.chat

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: ENGLISH
    }
}
