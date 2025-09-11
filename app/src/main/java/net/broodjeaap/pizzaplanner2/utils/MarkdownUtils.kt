package net.broodjeaap.pizzaplanner2.utils

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.image.glide.GlideImagesPlugin

class MarkdownUtils {
    companion object {
        fun setMarkdownText(textView: TextView, markdownText: String) {
            val context = textView.context
            val markwon = Markwon.builder(context)
                .usePlugin(GlideImagesPlugin.create(context))
                .build()
            markwon.setMarkdown(textView, markdownText)
            
            // Enable link clicking
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
