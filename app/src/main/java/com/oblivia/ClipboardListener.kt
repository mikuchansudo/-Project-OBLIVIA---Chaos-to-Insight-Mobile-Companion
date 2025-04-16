package com.oblivia

import android.content.ClipboardManager
import android.content.Context

object ClipboardListener {
    fun getClipboardText(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
    }
}
