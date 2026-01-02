package com.example.magiceightball.core.domain.policy

import javax.inject.Inject

class DefaultPromptPolicy @Inject constructor() : PromptPolicy {
    // "Generate a Magic 8 Ball style message, brief, max 6 words."
    // We add enforce instructions to ensure the model complies strictly.
    override val systemPrompt: String
        get() = "Generate a Magic 8 Ball style message, brief, max 6 words."
}
