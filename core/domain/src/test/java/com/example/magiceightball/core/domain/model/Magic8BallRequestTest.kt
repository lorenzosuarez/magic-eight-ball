package com.example.magiceightball.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Magic8BallRequestTest {

    @Test
    fun `a request has no question unless one is given`() {
        assertNull(Magic8BallRequest().question)
        assertNull(Magic8BallRequest().questionForAi)
    }

    @Test
    fun `a short question is sent as it was asked`() {
        assertEquals("Will it rain?", Magic8BallRequest(question = "Will it rain?").questionForAi)
    }

    @Test
    fun `a blank question is no question`() {
        assertNull(Magic8BallRequest(question = "").questionForAi)
        assertNull(Magic8BallRequest(question = " \n\t ").questionForAi)
    }

    @Test
    fun `the question becomes a single trimmed line`() {
        val request = Magic8BallRequest(question = "  Will it\nrain\r\n\ttomorrow?\u0000 ")

        assertEquals("Will it rain tomorrow?", request.questionForAi)
    }

    @Test
    fun `a long question is capped`() {
        val request = Magic8BallRequest(question = "a".repeat(Magic8BallRequest.MAX_QUESTION_LENGTH + 50))

        assertEquals("a".repeat(Magic8BallRequest.MAX_QUESTION_LENGTH), request.questionForAi)
    }

    @Test
    fun `the cap never leaves a trailing space`() {
        val words = "a".repeat(Magic8BallRequest.MAX_QUESTION_LENGTH - 1) + " b"

        assertEquals("a".repeat(Magic8BallRequest.MAX_QUESTION_LENGTH - 1), Magic8BallRequest(question = words).questionForAi)
    }
}
