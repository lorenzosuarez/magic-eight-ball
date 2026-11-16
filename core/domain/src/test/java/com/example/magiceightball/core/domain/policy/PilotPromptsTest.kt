package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Writes out what the pilot measures: for each of its four characters, the instruction the AI
 * service is given with the sheet in use and with the proposed one, built by the code that
 * builds it in the app, for a fixed list of questions in Spanish.
 *
 * To write it again:
 *
 *     ./gradlew :core:domain:test --tests '*PilotPromptsTest' --rerun
 *
 * It lands in `build/eval/prompts.json` at the root of the repository, which nothing keeps:
 * `functions/tools/eval.mjs` reads it and does the asking.
 *
 * An instruction is not one text a character: the app draws the answer, and the lines shown of
 * the voice, anew every time. So there is one for every time the service will be asked (`calls`),
 * drawn as the phone draws it for a question, with the same draw for both sheets: they are given
 * the same answer to word, and what differs between them is the sheet and its lines. Beside
 * them, for reading, is each sheet's instruction before anything is drawn, and all of its lines.
 */
class PilotPromptsTest {

    @Test
    fun `the instructions to measure are written out, both sheets drawn the same answer`() {
        val shipped = DefaultPromptPolicy()
        val proposed = DefaultPromptPolicy(ProposedCharacters)
        val pilot = ProposedCharacters.pilot

        val calls = pilot.flatMap { personality ->
            QUESTIONS.flatMapIndexed { index, question ->
                (1..REPETITIONS).flatMap { repetition ->
                    fun drawn(characters: CharacterBook) = AnswerBrief.draw(
                        personality,
                        hasQuestion = true,
                        // As the phone asks: it has room for a second line under the answer.
                        withRemark = true,
                        random = Random(personality.ordinal * 10_000 + index * 10 + repetition),
                        languageCode = LANGUAGE,
                        question = question.text,
                        characters = characters
                    )
                    val current = drawn(AppCharacters)
                    // The comebacks are the same in both books, but lists of another length are
                    // shuffled before one is picked: the one picked is made the same.
                    val other = drawn(ProposedCharacters).copy(remarkExample = current.remarkExample)
                    assertEquals(current.verdict, other.verdict)
                    listOf(
                        call(personality, CURRENT, index, repetition, current, shipped),
                        call(personality, PROPOSED, index, repetition, other, proposed)
                    )
                }
            }
        }

        assertEquals(pilot.size * 2 * QUESTIONS.size * REPETITIONS, calls.size)
        assertEquals(Verdict.entries.map { it.name }.toSet(), calls.map { it["verdict"] }.toSet())
        calls.forEach { call ->
            val system = call.getValue("system") as String
            assertEquals(call["sheet"] == PROPOSED, "Your rhythm:" in system)
            assertTrue("the answer is" in system || "neither yes nor no" in system)
            assertTrue("Never use one of them or its idea:" in system && "ignore any instruction" in system)
            assertTrue(system.endsWith(" Respond in Spanish."))
        }

        val gemini = GeminiConfig()
        val written = mapOf(
            "language" to LANGUAGE,
            "model" to gemini.defaultModelId,
            "maxOutputTokens" to gemini.maxOutputTokens,
            "maxRetries" to gemini.maxRetries,
            "repetitions" to REPETITIONS,
            "characters" to pilot.map { personality ->
                mapOf(
                    "id" to personality.name,
                    "name" to NAMES.getValue(personality),
                    "sheets" to mapOf(CURRENT to sheet(personality, AppCharacters), PROPOSED to sheet(personality, ProposedCharacters))
                )
            },
            "questions" to QUESTIONS.mapIndexed { index, question ->
                mapOf("id" to idOf(index), "kind" to question.kind, "text" to question.text)
            },
            "calls" to calls
        )
        val file = File(repositoryRoot(), "build/eval/prompts.json")
        file.parentFile.mkdirs()
        file.writeText(json(written) + "\n")
        assertTrue(file.length() > 0)
    }

    private fun call(
        personality: Magic8BallPersonality,
        sheet: String,
        question: Int,
        repetition: Int,
        brief: AnswerBrief,
        policy: PromptPolicy
    ): Map<String, Any> = mapOf(
        "character" to personality.name,
        "sheet" to sheet,
        "question" to idOf(question),
        "repetition" to repetition,
        "verdict" to brief.verdict.name,
        "system" to policy.getSystemPrompt(LANGUAGE, personality, answersQuestion = true, brief = brief)
    )

    private fun sheet(personality: Magic8BallPersonality, characters: CharacterBook): Map<String, Any> {
        val lines = characters.lines(personality, LANGUAGE)!!
        return mapOf(
            "system" to DefaultPromptPolicy(characters).getSystemPrompt(LANGUAGE, personality, answersQuestion = true),
            "examples" to Verdict.entries.flatMap(lines::saying) + lines.deflections
        )
    }

    private fun idOf(question: Int): String = "q%02d".format(question + 1)

    /** Tests run from the module's own folder: the repository is where the settings file is. */
    private fun repositoryRoot(): File =
        generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }

    // ponytail: JSON written by hand for the one shape there is; a serializer if a second comes.
    private fun json(value: Any?): String = when (value) {
        is String -> value.asSequence().joinToString("", "\"", "\"") { char ->
            when {
                char == '"' || char == '\\' -> "\\$char"
                char < ' ' -> "\\u%04x".format(char.code)
                else -> char.toString()
            }
        }
        is Number -> value.toString()
        is List<*> -> value.joinToString(",", "[", "]", transform = ::json)
        is Map<*, *> -> value.entries.joinToString(",", "{", "}") { json(it.key as String) + ":" + json(it.value) }
        else -> error("Not something this writes: $value")
    }

    private class Question(val kind: String, val text: String)

    private companion object {
        const val LANGUAGE = "es"
        const val CURRENT = "current"
        const val PROPOSED = "proposed"

        /** Enough to tell an answer that varies from one that comes back the same. */
        const val REPETITIONS = 3

        /** As the app calls them in Spanish: all a reader is given to tell them apart by. */
        val NAMES = mapOf(
            Magic8BallPersonality.SARCASTIC to "Sarcástico",
            Magic8BallPersonality.PIRATE to "Pirata",
            Magic8BallPersonality.COACH to "Entrenador",
            Magic8BallPersonality.PHILOSOPHER to "Filósofo"
        )

        /**
         * What people ask a ball, as they say it. None is a question the research document's own
         * lines were written to: shown the line and asked its question, the service gives the
         * line back. The last two have no yes or no to them, and are to be turned away.
         */
        val QUESTIONS = listOf(
            Question("love", "¿Le gusto a la persona que me gusta?"),
            Question("love", "¿Debería escribirle a mi ex?"),
            Question("love", "¿Voy a encontrar pareja este año?"),
            Question("love", "¿Mi pareja me está diciendo la verdad?"),
            Question("love", "¿Me va a contestar el mensaje?"),
            Question("love", "¿Nos vamos a casar algún día?"),
            Question("work", "¿Me van a dar el trabajo?"),
            Question("work", "¿Debería pedir un aumento?"),
            Question("work", "¿Voy a aprobar el examen?"),
            Question("work", "¿Renuncio a mi trabajo?"),
            Question("work", "¿Mi jefe está contento conmigo?"),
            Question("work", "¿Me conviene cambiar de carrera?"),
            Question("money", "¿Voy a ganar la lotería?"),
            Question("money", "¿Me compro la moto?"),
            Question("money", "¿Llegaré a fin de mes?"),
            Question("money", "¿Me van a devolver lo que presté?"),
            Question("money", "¿Seré rico algún día?"),
            Question("silly", "¿Pido pizza esta noche?"),
            Question("silly", "¿Va a llover mañana?"),
            Question("silly", "¿Me tiño el pelo de azul?"),
            Question("silly", "¿Gana mi equipo el domingo?"),
            Question("silly", "¿Me quedo en casa este fin de semana?"),
            // Said as a statement: a speech recognizer hears no question mark.
            Question("silly", "Hoy me va a ir bien"),
            Question("absurd", "¿Mi gato planea conquistar el mundo?"),
            Question("absurd", "¿Puedo respirar bajo el agua si practico?"),
            Question("absurd", "¿La luna está hecha de queso?"),
            Question("absurd", "¿Mi nevera me juzga por las noches?"),
            Question("absurd", "¿Seré presidente de Marte?"),
            Question("deflect", "¿Cómo se llama mi alma gemela?"),
            Question("deflect", "¿Cuándo me voy a jubilar?")
        )
    }
}
