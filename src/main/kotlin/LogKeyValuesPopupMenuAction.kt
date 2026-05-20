package com.kapresoft.luax

import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction


@Suppress("ActionPresentationInstantiatedInCtor", "UnstableApiUsage")
class LogKeyValuesPopupMenuAction : AnAction(
    LuaBundle.lazyMessage("action.log.key-values.text"),
    LuaBundle.lazyMessage("action.log.key-values.description")
) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        if (!editor.selectionModel.hasSelection()) {
            val offset = editor.caretModel.offset
            val text = editor.document.charsSequence
            val start = (offset - 1 downTo 0).firstOrNull { !text[it].isLetterOrDigit() && text[it] != '_' }
                ?.plus(1) ?: 0
            val end = (offset until text.length).firstOrNull { !text[it].isLetterOrDigit() && text[it] != '_' }
                ?: text.length
            if (start < end) editor.selectionModel.setSelection(start, end)
        }

        val selectedText = editor.selectionModel.selectedText?.trim() ?: return
        val parts = selectedText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val args = parts.joinToString(", ") { "'$it=', $it" }

        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val currentLine = document.getLineNumber(caretOffset)

        val textUpToCaret = document.charsSequence
            .subSequence(0, document.getLineEndOffset(currentLine))
            .toString()
        val functionName = LuaEditorUtil.findLuaFunctionName(textUpToCaret) ?: "[DEBUG]"
        val templateText = LuaxProjectSettings.getInstance(project).effectiveTemplate()
        val rendered = renderTemplate(templateText, args, functionName)
        val lineStart = document.getLineStartOffset(currentLine)
        val lineEnd = document.getLineEndOffset(currentLine)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
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
        editor.caretModel.moveToOffset(insertedLineEnd)
        editor.selectionModel.removeSelection()
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

