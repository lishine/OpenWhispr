package com.edib.openwhispr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextInsertionTest {
    @Test fun `dictation preserves text on both sides of the caret`() {
        assertEquals(TextInsertion("Say hello now", 9), insertAtSelection("Say  now", false, 4, 4, "hello"))
    }

    @Test fun `dictation replaces selection in either direction`() {
        for ((start, end) in listOf(4 to 7, 7 to 4)) {
            assertEquals(TextInsertion("Say hello now", 9), insertAtSelection("Say bye now", false, start, end, "hello"))
        }
    }

    @Test fun `unavailable text or invalid selection falls back without reconstructing a field`() {
        assertNull(insertAtSelection(null, false, 0, 0, "hello"))
        assertNull(insertAtSelection("Hi", false, -1, -1, "hello"))
        assertNull(insertAtSelection("Hi", false, 99, 99, "hello"))
        assertNull(insertAtSelection("Hi", false, 1, -1, "hello"))
    }

    @Test fun `empty hinted field inserts only dictation without the hint`() {
        assertEquals(TextInsertion("hello", 5), insertAtSelection("Type here", true, -1, -1, "hello"))
        assertEquals(TextInsertion("hello", 5), insertAtSelection("", false, -1, -1, "hello"))
    }

    @Test fun `length limit cannot truncate the existing suffix`() {
        assertNull(insertAtSelection("ABCDE", false, 2, 2, "X", 5))
        assertEquals(TextInsertion("ABXDE", 3), insertAtSelection("ABCDE", false, 2, 3, "X", 5))
    }

    @Test fun `potentially truncated accessibility text is never reconstructed`() {
        assertNull(insertAtSelection("A".repeat(99_999), false, 1, 1, "hello"))
    }

    @Test fun `emoji selections use Android UTF16 offsets`() {
        assertEquals(TextInsertion("AhelloB", 6), insertAtSelection("A\uD83D\uDE00B", false, 1, 3, "hello"))
    }
}
