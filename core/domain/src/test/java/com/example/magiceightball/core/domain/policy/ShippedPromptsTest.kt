package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Zodiac
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import kotlin.random.Random

/**
 * The instruction of every character the app ships with, held to what it was when it was
 * recorded here. It was recorded before a sheet could say more than it did (its rhythm, the
 * world it draws from, its habits of construction, what it uses instead): a sheet that says
 * none of that is told to the service byte for byte as before.
 *
 * What is kept is a digest for each language and character, taken over the instruction in every
 * shape the app asks for it: plain, with a question, with what is known of the asker, and with
 * what the app draws for an answer. A sheet, a line or a rule changed on purpose changes its
 * lines here too: the failure prints them as they now read.
 */
class ShippedPromptsTest {

    private val policy = DefaultPromptPolicy()

    @Test
    fun `a character whose sheet says nothing new is told byte for byte what it was told before`() {
        val now = listOf("en", "es", "pt").flatMap { language ->
            Magic8BallPersonality.entries.map { "$language/$it ${digestOf(language, it)}" }
        }
        val changed = now - RECORDED.lines().toSet()

        assertTrue("No longer what was recorded:\n" + changed.joinToString("\n"), changed.isEmpty())
        assertEquals(RECORDED.lines().size, now.size)
    }

    private fun digestOf(language: String, personality: Magic8BallPersonality): String {
        val known = AskerFacts(firstName = "Lorena", zodiac = Zodiac.LEO, birthdayToday = true)
        val told = mutableListOf(
            policy.getSystemPrompt(language, personality),
            policy.getSystemPrompt(language, personality, answersQuestion = true),
            policy.getSystemPrompt(language, personality, answersQuestion = true, asker = known)
        )
        repeat(DRAWS) { seed ->
            fun drawn(hasQuestion: Boolean, detailed: Boolean = false) = policy.getSystemPrompt(
                language,
                personality,
                answersQuestion = hasQuestion,
                brief = AnswerBrief.draw(
                    personality,
                    hasQuestion = hasQuestion,
                    withRemark = true,
                    random = Random(seed),
                    languageCode = language,
                    detailed = detailed
                )
            )
            told += listOf(drawn(hasQuestion = true), drawn(hasQuestion = false), drawn(hasQuestion = true, detailed = true))
        }
        val digest = MessageDigest.getInstance("SHA-256")
        told.forEach { digest.update(it.toByteArray()); digest.update(0) }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        /** Enough for every answer to be drawn, and most pairs of the lines shown with it. */
        const val DRAWS = 8

        val RECORDED = """
            en/SARCASTIC d0f96ee4bb7413241c42caae2973f47395469113936f548baded5f03e3c0e89b
            en/MYSTICAL 13432b7f0a338b9cb1b57944c7d1a746127a0827084ae6acf6e7b8f9a3f88fc7
            en/NEUTRAL f39a2e53784016eb3e0792ddbff9610ed547fa2257f135bf632897fd59341b97
            en/GRANDMA b116e8ebf6c88268133fb85a0d10382c289e7d22bf83cd5a0436ad62b7221444
            en/PIRATE 0b164bc8e252bf5f20c8eaf6b12f298d766939c657c4fdc5ed534651e7cb4802
            en/PHILOSOPHER 8c9621f4a2801360b72c5de128e8a36d6f37c89a84d47ca2155c56f43e5005c3
            en/COACH 1f217a4641ead8c8fbdd0fe32547c764ee909c8d724cacbaaa1b56a16e4947ec
            en/LOVESTRUCK 87497018bd4a700812ab29869b7137ed256f35be0c3858c4727ce0d4e1cce861
            en/DRAMA 0a24a82484b881c0aa0a59161ebcf5138b0a5c0002fb0f8f49837df65920433d
            en/ROBOT 72f193a307be1607941f1d3f3af47e90ed6046d315413e5d67da3454e20934e9
            en/DETECTIVE 71ed2544c93b08f59d4df9fea406049f3289748aff379c179b0f6cfab1c8734b
            en/CONSPIRACY 46d4b5a4fe27f6837494e434e88374256b3056a793ecb60900381bd40c8edf06
            en/CAT 98b85dee557ea1a0dc4363cf7569a1969ee7cab34362391a5950f10e10b145b7
            en/ZOOMER 12057f158b3a802b364d9bc961e0921b57dd729f73db4244902a000d6eb8a021
            en/VILLAIN 6cf0859c31db8bcb813edd933b45ea5b47aeda93656ca1e979be0492eee8647c
            en/POET ed8383e94a3762b175578e5bde9032bcee7e91923dda522c713408e03decdd82
            en/ASTRO 3b258521dd6c2a550ed3af1b0460007ec529c806e9b6ab74f32b1951c2a06819
            en/THERAPIST bd9c3cb3beecf3b967ffa1c7afd13d40641461078e9cadb03337ef29ef38e733
            en/DOG fe907547686a83d915ecbf52b2720345481ac4ba49a1e60a2d9cdb01884cc0cc
            en/GAMER ff39d378fccf78f9e4e99b2bc2e2a1ceb48a96f60f63b0e39a7730f00d4c088c
            en/CUSTOM 49a4bc7d104cf6dad79cc89fd954fcffdd7e5dd28431fc38dcea3299f79ed581
            es/SARCASTIC b5d36e869f3e3c18c7b0930885209a72f5b5ed19ea79da52713d97475442d2c5
            es/MYSTICAL 000d808dca739cff2fedb9ea8423514a2de28d6f623a125124b49aaeb5aaa3c0
            es/NEUTRAL 5f1d3a1ab38e2fe0b6a74f99f2493be9f76e338937f7d2eea5e0ead0e3b1fa2a
            es/GRANDMA 044d37865fb55665a9ea7a584e4aa9932562a132840fe5629313804d6df0c670
            es/PIRATE c369ca4fd816fb493fb58df48f934fb855f3138b29b06a0a491cbcee37298417
            es/PHILOSOPHER a4eb0df0e7aa64ecb25b0b63a75198cc04b678dea156aa6ed6a7465cfa0a19e7
            es/COACH ea5b0af0b0c5ef34e3c25a179d24f781ad0966f688601665b660d4344a5120ad
            es/LOVESTRUCK b328b23f00bc69554fc2af426d022f126e5b8e7751d16d28d8416fea73a24436
            es/DRAMA ab055b0c89df0a6220b08848b356606356322d465663cd474ecbddd967ce58f4
            es/ROBOT f786a7f47568c4b06054110b30d088f9e51c5b0e374ac2b2605625099260a56d
            es/DETECTIVE e050a5d1fa9595b298205a717b19ce518c20fc8d4113c746cfbb2ce6ee8b0f51
            es/CONSPIRACY a030a5191b98cad546de467c195958f1987e755dd3df0f7f2ec21d9f26940677
            es/CAT 7a854e4bc549d5c6eef0e3f95e834cc52b2fbf91f3255b1107e0322cc84351ac
            es/ZOOMER 7ede16c620ab668aa77a3464d30e5b197673804965357520887fca462730f2fc
            es/VILLAIN a2b7be13eed485731b884a112f31568bc37da8b7c4030111c176bbb9efc13949
            es/POET e781df8228ae659cde36a9101049425061726adecfd46dcbd2232d75a8554957
            es/ASTRO 288bb65cb3e816cee8e1e2c93ef65868cdd53ae544b5f0b1c755633e97679cb1
            es/THERAPIST 5dd230e98d4897bb3ef6c7a6c109d6739f548162412a01d6320529f548ed6c71
            es/DOG cf10e8ae8e550f8fd79af156ae8e9dba08ea701092411aac0c7dfdfac9ad0728
            es/GAMER 579e8fb3eb8cffe85ed3fbf2a51ea70e72364ab8c1d3d15e9c4cacef86870f2e
            es/CUSTOM 1e5e28d4ff7a45858bb677b3309e8ab3e09b5f67c1a153d70fc1809b4158b7e3
            pt/SARCASTIC 0dd9ffe3f7ab6b622e992d46ad9720b332c6c7d3b61dfa17a055d28b9e2a04ad
            pt/MYSTICAL 81bc12a3151ef5509d01a887125de1fa81814d62d86ab650366de6bb1fbfc649
            pt/NEUTRAL 1da75ad7cb34bf47759137e92c17669496c647db8024c08324263c85fe2b96da
            pt/GRANDMA 300b9d7b76fae630b7b037ab58d397561cd10c3c4c009be3c3c3ea5920f5c601
            pt/PIRATE ab3f266f93cd99e720e29bbb18fe0d6d5b867d01a96e40248b4a24107c16af1c
            pt/PHILOSOPHER d14e77354bad506457645f1d12d4b92eb65fb373f0b115a96f5e6e7f5d90a826
            pt/COACH cb66ab37ff6372aa3131bb7b2763bafd9f6120441beb60eec32d22e28f563c88
            pt/LOVESTRUCK 5953a88f19b6d03f5a5d712aad6f6cee68a574a370d6f26a8ba4136c33bb7fa8
            pt/DRAMA 3bfb4ad73acd521fbd2669540e03abdbd71004241a7fdda440310f1b57d4706d
            pt/ROBOT 520d095bb7748f4445e386b32120d6aff66bf971a5da50a6f191e0d09a9d304e
            pt/DETECTIVE 8ddab985098d160cd904c05d3fdfd7fe55ef0e8d90882487ab259b83c81b82ed
            pt/CONSPIRACY 48857d9dfbdbb665bd34350bec342f5a2aa9c2a72071655c009395cb5a22eebf
            pt/CAT 73d53efe3ff2ecc0a80a74d85901f5f6178c58ed150f3a44722ab63e2f267ad4
            pt/ZOOMER d056a9b8eba8b8958ae9d5df63a3e0dd2eeed21db0a03dbceca8bd7e14e47c70
            pt/VILLAIN d0ea8665f9a5d4fd0b5fa2193373c8e6428bc7d0fdb925f7ab715850b2b3e03e
            pt/POET 87ed341da9501501750f56d8646246858619c382616641518ba0ed24a11f4f9f
            pt/ASTRO 474d8810063fbf83ce028ab39106e556c6a940c974ddf839d48c742c73839b50
            pt/THERAPIST ba21bb8ad410c5b7a1c6cd02885bcc969364cb886baf6c2ca6ab31f3c8d5e5b5
            pt/DOG 5aadc292f00082fdc051280f8d60addf3ab7ba171da0ed68e0b134a9fa9e5310
            pt/GAMER 4ed0d41c8c15db84a3b74556d4187affc6c91d53ea5bd06a3be01f6a71853af1
            pt/CUSTOM 4382f49b972cc7d79a73dcd8daaf06cd1a96ba412803f7b87e47c679ee6b53d8
        """.trimIndent()
    }
}
