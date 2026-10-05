package com.edib.openwhispr

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Button
import android.text.InputFilter
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** Disposable field for manual end-to-end dictation checks; no saving or sending. */
class DictationCheckActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clipboardCheck = intent.getBooleanExtra("clipboard_check", false)
        val sentinel = "dictation-check-sentinel"
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboardCheck) clipboard.setPrimaryClip(ClipData.newPlainText("dictation-check", sentinel))
        val field = EditText(this).apply {
            hint = "Tap here, then tap the mic to dictate"
            val limit = intent.getIntExtra("max_length", -1)
            if (limit >= 0) filters = arrayOf(InputFilter.LengthFilter(limit))
            setText(intent.getStringExtra("seed").orEmpty())
            val start = intent.getIntExtra("selection_start", text.length).coerceIn(0, text.length)
            val end = intent.getIntExtra("selection_end", start).coerceIn(0, text.length)
            setSelection(start, end)
            setOnFocusChangeListener { _, focused ->
                if (focused) post { setSelection(start, end) }
            }
        }
        val status = TextView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 120, 32, 32)
            isFocusableInTouchMode = true
            addView(TextView(this@DictationCheckActivity).apply { text = "Dictation check — temporary text field" })
            addView(field)
            if (clipboardCheck) addView(Button(this@DictationCheckActivity).apply {
                text = "Check clipboard and cursor"
                isFocusable = false
                setOnClickListener {
                    val unchanged = clipboard.primaryClip?.getItemAt(0)?.text == sentinel
                    status.text = "Clipboard unchanged: $unchanged; cursor: ${field.selectionStart},${field.selectionEnd}"
                }
            })
            addView(status)
        }
        setContentView(layout)
    }
}
