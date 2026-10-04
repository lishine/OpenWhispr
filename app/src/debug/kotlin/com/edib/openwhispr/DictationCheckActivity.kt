package com.edib.openwhispr

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** Disposable field for manual end-to-end dictation checks; no saving or sending. */
class DictationCheckActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 120, 32, 32)
            isFocusableInTouchMode = true
            addView(TextView(this@DictationCheckActivity).apply { text = "Dictation check — temporary text field" })
            addView(EditText(this@DictationCheckActivity).apply { hint = "Tap here, then tap the mic to dictate" })
        }
        setContentView(layout)
    }
}
