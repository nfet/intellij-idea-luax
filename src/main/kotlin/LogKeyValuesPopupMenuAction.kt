package com.kapresoft.luax

import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class LogKeyValuesPopupMenuAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
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
        e.presentation.isEnabledAndVisible =
            editor?.selectionModel?.hasSelection() == true &&
            extension.equals("lua", ignoreCase = true)
    }
}

