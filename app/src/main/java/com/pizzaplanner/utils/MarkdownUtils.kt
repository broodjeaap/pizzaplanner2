package com.pizzaplanner.utils

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import io.noties.markwon.Markwon

class MarkdownUtils {
    companion object {
        fun setMarkdownText(textView: TextView, markdownText: String) {
            val context = textView.context
            val markwon = Markwon.create(context)
            markwon.setMarkdown(textView, markdownText)
            
            // Enable link clicking
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
