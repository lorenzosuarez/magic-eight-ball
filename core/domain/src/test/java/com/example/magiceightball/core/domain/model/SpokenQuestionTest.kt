package com.example.magiceightball.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SpokenQuestionTest {

    @Test
    fun `what was said is written as a question, in the way of its language`() {
        assertEquals("¿Voy a ganar la lotería?", "voy a ganar la lotería".asSpokenQuestion("es"))
        assertEquals("Will I win the lottery?", "will I win the lottery".asSpokenQuestion("en"))
        // Portuguese closes a question and does not open it.
        assertEquals("Vou ganhar na loteria?", "vou ganhar na loteria".asSpokenQuestion("pt"))
    }

    @Test
    fun `marks the recognizer already wrote are not written twice`() {
        assertEquals("¿Me va a llamar?", "¿Me va a llamar?".asSpokenQuestion("es"))
        assertEquals("¿Me va a llamar?", "Me va a llamar.".asSpokenQuestion("es"))
        assertEquals("Will it rain?", " will it rain?? ".asSpokenQuestion("en"))
    }

    @Test
    fun `nothing said is nothing asked`() {
        assertEquals("", "".asSpokenQuestion("es"))
        assertEquals("", "  ?  ".asSpokenQuestion("en"))
    }
}
