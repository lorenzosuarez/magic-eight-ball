package com.example.magiceightball.core.network.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null,
    val candidateCount: Int? = null,
    val stopSequences: List<String>? = null,
    val responseMimeType: String? = null
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = "user"
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Part(
    val text: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Candidate(
    val content: Content?,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)
