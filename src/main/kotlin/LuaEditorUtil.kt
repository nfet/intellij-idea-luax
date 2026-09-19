package com.kapresoft.luax

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.awt.datatransfer.StringSelection

object LuaEditorUtil {

    /** Copies [text] to the clipboard and shows a balloon notification confirming what was copied. */
    fun copyToClipboardWithNotification(project: Project?, text: String, title: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Luax Notifications")
            .createNotification(title, text, NotificationType.INFORMATION)
            .notify(project)
    }

    /**
     * The active selection's text, or -- when nothing is selected -- the run of characters
     * matching [isCandidateChar] around the caret, without mutating the editor's selection.
     * Used both to decide whether a conversion action applies (update) and, by the action
     * itself, to grab the same text it's about to convert.
     */
    fun candidateText(editor: Editor, isCandidateChar: (Char) -> Boolean): String? {
        if (editor.selectionModel.hasSelection()) return editor.selectionModel.selectedText?.trim()

        val offset = editor.caretModel.offset
        val text = editor.document.charsSequence
        val start = (offset - 1 downTo 0).firstOrNull { !isCandidateChar(text[it]) }?.plus(1) ?: 0
        val end = (offset until text.length).firstOrNull { !isCandidateChar(text[it]) } ?: text.length
        return if (start < end) text.subSequence(start, end).toString().trim() else null
    }

    /**
     * Shared update() body for the color-conversion popup actions: visible on any .lua file
     * with an open editor, enabled only when [candidateText] under the caret/selection actually
     * converts via [converter].
     */
    fun updateColorActionPresentation(e: AnActionEvent, isCandidateChar: (Char) -> Boolean, converter: (String) -> String?) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val isLua = file?.originalFile?.virtualFile?.extension.equals("lua", ignoreCase = true)
        val applies = editor != null && candidateText(editor, isCandidateChar)?.let(converter) != null
        e.presentation.isVisible = editor != null && isLua
        e.presentation.isEnabled = applies
    }

    private val hexColorRegex = Regex("""^#?([0-9a-fA-F]{6})$""")

    // "r, g, b" or "r, g, b, a" -- alpha (if present) is captured but ignored, since hex has no alpha channel.
    private val floatRgbRegex = Regex(
        """^([01](?:\.\d+)?)\s*,\s*([01](?:\.\d+)?)\s*,\s*([01](?:\.\d+)?)\s*(?:,\s*[01](?:\.\d+)?\s*)?$"""
    )

    /** Converts a "#RRGGBB" or "RRGGBB" hex string to "r, g, b" WoW-style 0.0-1.0 floats, or null if not a valid hex color. */
    fun hexToFloatRgb(hex: String): String? {
        val match = hexColorRegex.find(hex.trim()) ?: return null
        val value = match.groupValues[1]
        val r = value.substring(0, 2).toInt(16) / 255.0
        val g = value.substring(2, 4).toInt(16) / 255.0
        val b = value.substring(4, 6).toInt(16) / 255.0
        return "%.3f, %.3f, %.3f".format(r, g, b)
    }

    /** Converts "r, g, b" or "r, g, b, a" WoW-style 0.0-1.0 floats to a "#RRGGBB" hex string (alpha ignored), or null if not valid. */
    fun floatRgbToHex(floatRgb: String): String? {
        val match = floatRgbRegex.find(floatRgb.trim()) ?: return null
        val (r, g, b) = match.destructured
        val toByte = { v: String -> (v.toDouble() * 255).toInt().coerceIn(0, 255) }
        return "#%02X%02X%02X".format(toByte(r), toByte(g), toByte(b))
    }

    fun findLuaFunctionName(text: String): String? {
        val namedFunctionRegex = Regex("""(?:local\s+)?function\s+([a-zA-Z_][a-zA-Z0-9_.:]*)\s*\(""")
        val anonymousFunctionRegex = Regex("""\bfunction\s*\(""")
        val blockOpenerRegex = Regex("""\b(do|then|repeat)\b""")
        val endRegex = Regex("""\bend\b""")

        val lines = text.lines()
        var depth = 0

        for (line in lines.asReversed()) {
            val endCount = endRegex.findAll(line).count()
            val namedMatch = namedFunctionRegex.find(line)
            val anonCount = anonymousFunctionRegex.findAll(line).count()
            val openerCount = blockOpenerRegex.findAll(line).count()

            val openersOnLine = anonCount + openerCount + (if (namedMatch != null) 1 else 0)
            depth += endCount - openersOnLine

            if (namedMatch != null && depth <= 0) {
                var name = namedMatch.groupValues[1]
                name = name.substringAfterLast(".")
                name = name.substringAfterLast(":")
                return name
            }
        }
        return null
    }
}