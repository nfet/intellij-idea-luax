package com.kapresoft.luax

import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import java.util.function.Supplier

/**
 * Shared behavior for the "Log Key Values" popup actions: given the selection (or, per
 * caret when there's none, the word under it), builds a debug-log line from a template and
 * inserts it after the bottom-most caret's line. Subclasses only differ in which template
 * ([effectiveTemplate]) and bundle text ([text]/[description]) they use.
 */
@Suppress("UnstableApiUsage")
abstract class LogKeyValuesActionBase(text: Supplier<String>, description: Supplier<String>) : AnAction(text, description) {

    /** The template to render for this action, e.g. LuaxProjectSettings.effectiveTemplate1()/effectiveTemplate2(). */
    protected abstract fun effectiveTemplate(project: Project): String

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    /** Expands [caret]'s own selection to the word under it when it has none; returns its (trimmed) selected text. */
    private fun caretText(editor: Editor, caret: Caret): String? {
        if (!caret.hasSelection()) {
            val offset = caret.offset
            val text = editor.document.charsSequence
            val start = (offset - 1 downTo 0).firstOrNull { !text[it].isLetterOrDigit() && text[it] != '_' }
                ?.plus(1) ?: 0
            val end = (offset until text.length).firstOrNull { !text[it].isLetterOrDigit() && text[it] != '_' }
                ?: text.length
            if (start < end) caret.setSelection(start, end)
        }
        return caret.selectedText?.trim()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        // Multiple carets: resolve each one's own text (expanding to word if unselected),
        // in document order, and combine into a single args list -- one debug line covering
        // all of them, inserted after the bottom-most caret's line.
        val carets = editor.caretModel.allCarets.sortedBy { it.offset }
        val parts = carets.mapNotNull { caretText(editor, it) }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (parts.isEmpty()) return
        val args = parts.joinToString(", ") { "'$it=', $it" }

        val document = editor.document
        val caretOffset = carets.last().offset
        val currentLine = document.getLineNumber(caretOffset)

        val textUpToCaret = document.charsSequence
            .subSequence(0, document.getLineEndOffset(currentLine))
            .toString()
        val functionName = LuaEditorUtil.findLuaFunctionName(textUpToCaret) ?: "[DEBUG]"
        val templateText = effectiveTemplate(project)
        val rendered = renderTemplate(templateText, args, functionName)
        val lineStart = document.getLineStartOffset(currentLine)
        val lineEnd = document.getLineEndOffset(currentLine)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }
        val nextLine = currentLine + 1

        var insertedLineEnd = 0
        WriteCommandAction.runWriteCommandAction(project) {
            if (nextLine < document.lineCount) {
                val nextLineStart = document.getLineStartOffset(nextLine)
                document.insertString(nextLineStart, "$indent$rendered\n")
                insertedLineEnd = nextLineStart + indent.length + rendered.length
            } else {
                document.insertString(lineEnd, "\n$indent$rendered")
                insertedLineEnd = lineEnd + 1 + indent.length + rendered.length
            }
        }
        carets.forEach { it.removeSelection() }
        editor.caretModel.removeSecondaryCarets()
        editor.caretModel.moveToOffset(insertedLineEnd)
    }

    private fun renderTemplate(templateText: String, args: String, functionName: String): String {
        return try {
            FileTemplateUtil.mergeTemplate(mapOf("args" to args, "functionName" to functionName), templateText, false)
        } catch (_: Exception) {
            templateText.replace("\${args}", args).replace("\${functionName}", functionName)
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val extension = file?.originalFile?.virtualFile?.extension
        val isLua = extension.equals("lua", ignoreCase = true)
        e.presentation.isEnabledAndVisible = editor != null && isLua
    }
}
