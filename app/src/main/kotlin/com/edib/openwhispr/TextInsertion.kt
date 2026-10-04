package com.edib.openwhispr

internal data class TextInsertion(val text: String, val cursor: Int)

internal fun insertAtSelection(current: String, start: Int, end: Int, text: String): TextInsertion {
    val safeStart = if (start < 0) current.length else start.coerceAtMost(current.length)
    val safeEnd = if (end < 0) safeStart else end.coerceAtMost(current.length)
    val from = minOf(safeStart, safeEnd)
    val to = maxOf(safeStart, safeEnd)
    return TextInsertion(current.replaceRange(from, to, text), from + text.length)
}
