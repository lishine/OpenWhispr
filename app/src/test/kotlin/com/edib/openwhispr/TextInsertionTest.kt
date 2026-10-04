package com.edib.openwhispr

import org.junit.Assert.assertEquals
import org.junit.Test

class TextInsertionTest {
    @Test fun `dictation preserves text on both sides of the caret`() {
        assertEquals(TextInsertion("Say hello now", 9), insertAtSelection("Say  now", 4, 4, "hello"))
    }

    @Test fun `dictation replaces selection in either direction`() {
        for ((start, end) in listOf(4 to 7, 7 to 4)) {
            assertEquals(TextInsertion("Say hello now", 9), insertAtSelection("Say bye now", start, end, "hello"))
        }
    }

    @Test fun `unavailable selection appends and stale bounds cannot crash`() {
        assertEquals(TextInsertion("Hi there", 8), insertAtSelection("Hi", -1, -1, " there"))
        assertEquals(TextInsertion("Hi!", 3), insertAtSelection("Hi", 99, 99, "!"))
        assertEquals(TextInsertion("!", 1), insertAtSelection("", -1, -1, "!"))
    }

    @Test fun `emoji selections use Android UTF16 offsets`() {
        assertEquals(TextInsertion("AhelloB", 6), insertAtSelection("A\uD83D\uDE00B", 1, 3, "hello"))
    }
}
