package com.example.data.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardBridge(private val context: Context) {
    private val clipboard: ClipboardManager?
        get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    fun readText(): String {
        val clip = clipboard?.primaryClip ?: return ""
        if (clip.itemCount <= 0) return ""
        return clip.getItemAt(0).coerceToText(context)?.toString() ?: ""
    }

    fun writeText(label: String, text: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
