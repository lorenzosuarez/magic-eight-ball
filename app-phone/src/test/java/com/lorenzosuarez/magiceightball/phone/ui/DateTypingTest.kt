package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTypingTest {

    /** Whether [digit], typed after [held] with the cursor at its end, leaves the number whole. */
    private fun after(held: String, digit: Char, most: Int) =
        typedWhole(TextFieldValue(held, TextRange(held.length)), held + digit, most)

    /** The digits that, typed after [held], leave the number whole. */
    private fun wholeAfter(held: String, most: Int) = ('0'..'9').filter { after(held, it, most) }.joinToString("")

    @Test
    fun `a day goes on at its second digit, or at a first one that no day of two digits begins with`() {
        assertEquals("456789", wholeAfter("", DAY))
        // Whatever the second is: a day there is none of is said by the date, not by the cursor.
        listOf("0", "1", "2", "3").forEach { first -> assertEquals("0123456789", wholeAfter(first, DAY)) }
    }

    @Test
    fun `a month goes on at its second digit, or at a first one from 2 to 9`() {
        assertEquals("23456789", wholeAfter("", MONTH))
        listOf("0", "1").forEach { first -> assertEquals("0123456789", wholeAfter(first, MONTH)) }
    }

    @Test
    fun `a year is whole at its fourth digit and not before`() {
        listOf("", "1", "19").forEach { begun -> assertEquals("", wholeAfter(begun, YEAR)) }
        assertEquals("0123456789", wholeAfter("199", YEAR))
    }

    @Test
    fun `a number selected whole and typed over goes on as it did the first time`() {
        val selected = TextFieldValue("15", TextRange(0, 2))

        assertTrue(typedWhole(selected, "7", DAY))
        assertFalse(typedWhole(selected, "2", DAY))
        assertTrue(typedWhole(TextFieldValue("2", TextRange(1)), "25", DAY))
    }

    @Test
    fun `whoever is correcting a number stays in it`() {
        // A digit put in before another, in a number that had room for it.
        assertFalse(typedWhole(TextFieldValue("5", TextRange(0)), "25", DAY))
        // One typed into the middle of a number that was whole: it takes the place of the last.
        assertFalse(typedWhole(TextFieldValue("15", TextRange(1)), "12", DAY))
        // One typed after a number that was whole changes nothing.
        assertFalse(typedWhole(TextFieldValue("15", TextRange(2)), "15", DAY))
    }

    @Test
    fun `deleting, or only moving the cursor, goes nowhere`() {
        assertFalse(typedWhole(TextFieldValue("15", TextRange(2)), "1", DAY))
        // What is left would be whole had it been typed.
        assertFalse(typedWhole(TextFieldValue("45", TextRange(2)), "4", DAY))
        assertFalse(typedWhole(TextFieldValue("15", TextRange(0, 2)), "", DAY))
        // A touch that puts the cursor at the end of a number that was selected.
        assertFalse(typedWhole(TextFieldValue("15", TextRange(0, 2)), "15", DAY))
    }

    private companion object {
        const val DAY = 31
        const val MONTH = 12
        const val YEAR = 9999
    }
}
