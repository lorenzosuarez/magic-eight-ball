package com.example.magiceightball.core.domain.gateway

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * Tells the developer how the app is used, in numbers: which styles are chosen, how the ball is
 * asked, what is shared. It is what shows whether a change made the app better.
 *
 * An event says that something happened and which of a few fixed kinds it was. It never carries
 * what the user asked, what the ball answered, a name typed for party mode or anything else the
 * user wrote or said: [UsageEvent] has no way to hold one.
 */
interface UsageTracker {
    fun track(event: UsageEvent)

    /** For where nothing is measured: tests, and builds without the measuring service. */
    object None : UsageTracker {
        override fun track(event: UsageEvent) = Unit
    }
}

/** Something the user did. [name] and [params] are what is sent: short words and small numbers. */
sealed interface UsageEvent {
    val name: String
    val params: Map<String, String> get() = emptyMap()

    /** The ball answered. */
    data class Asked(
        val how: How,
        val style: Magic8BallPersonality,
        val fromAi: Boolean,
        val withRemark: Boolean
    ) : UsageEvent {
        override val name get() = "ask"
        override val params
            get() = mapOf(
                "how" to how.label,
                "style" to style.label,
                "source" to if (fromAi) "ai" else "bundled",
                "remark" to withRemark.label
            )

        enum class How(val label: String) {
            TAP("tap"),
            SHAKE("shake"),
            VOICE("voice"),

            /** "Who of you…?" at a party. */
            WHO("who")
        }
    }

    data class AnswerShared(val style: Magic8BallPersonality, val fromAi: Boolean) : UsageEvent {
        override val name get() = "share_answer"
        override val params get() = mapOf("style" to style.label, "source" to if (fromAi) "ai" else "bundled")
    }

    data object AppShared : UsageEvent {
        override val name get() = "share_app"
    }

    data class StyleChosen(val style: Magic8BallPersonality) : UsageEvent {
        override val name get() = "choose_style"
        override val params get() = mapOf("style" to style.label)
    }

    /** One of the switches in the options was turned. */
    data class SettingChanged(val setting: Setting, val on: Boolean) : UsageEvent {
        override val name get() = "change_setting"
        override val params get() = mapOf("setting" to setting.label, "on" to on.label)

        enum class Setting(val label: String) {
            AI_ANSWERS("ai_answers"),
            VOICE_QUESTIONS("voice_questions"),
            SPOKEN_ANSWERS("spoken_answers")
        }
    }

    /** A party was set up or ended: how many are playing, never who. */
    data class PartySet(val players: Int) : UsageEvent {
        override val name get() = "set_party"
        override val params get() = mapOf("players" to players.toString())
    }
}

private val Magic8BallPersonality.label: String get() = name.lowercase()
private val Boolean.label: String get() = if (this) "yes" else "no"
