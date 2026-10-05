package com.edib.openwhispr

internal data class TextInsertion(val text: String, val cursor: Int)

internal fun insertAtSelection(
    exposedText: String?, showingHint: Boolean, start: Int, end: Int, text: String, maxLength: Int = -1
): TextInsertion? {
    // Accessibility text may contain an empty field's hint or be withheld entirely.
    val current = if (showingHint) "" else exposedText ?: return null
    // TextView can truncate accessibility text to 100,000 UTF-16 units (99,999 at a surrogate).
    if (current.length >= 99_999) return null
    val safeStart = if (current.isEmpty() && start == -1) 0 else start
    val safeEnd = if (current.isEmpty() && end == -1) 0 else end
    if (safeStart !in 0..current.length || safeEnd !in 0..current.length) return null
    val from = minOf(safeStart, safeEnd)
    val to = maxOf(safeStart, safeEnd)
    val updated = current.replaceRange(from, to, text)
    // Native paste handles a full field's limit without truncating its existing suffix.
    if (maxLength >= 0 && updated.length > maxLength) return null
    return TextInsertion(updated, from + text.length)
}
