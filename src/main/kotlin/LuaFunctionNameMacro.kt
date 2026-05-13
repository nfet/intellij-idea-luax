package com.kapresoft.luax

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.codeInsight.template.macro.MacroBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class LuaFunctionNameMacro : MacroBase("luaFunctionName", "Returns the name of the current Lua function") {

    // This makes the macro appear in the expression list
    override fun getPresentableName(): String {
        return "luaFunctionName()"
    }

    override fun calculateResult(
        params: Array<out Expression>,
        context: ExpressionContext,
        quick: Boolean
    ): Result? {
        val editor = context.editor ?: return null
        val project = editor.project ?: return null

        // CORRECT: Use PsiDocumentManager to get PSI file from document
        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return null
        val virtualFile = psiFile.virtualFile
        val isLuaFile = virtualFile?.extension.equals("lua", ignoreCase = true)

        // Only process Lua files
        if (!isLuaFile) return null

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return null

        val functionName = findLuaFunctionName(editor)

        return if (functionName != null) {
            TextResult(functionName)
        } else {
            null
        }
    }

    override fun calculateLookupItems(
        params: Array<out Expression>,
        context: ExpressionContext
    ): Array<LookupElement>? {
        val result = calculateResult(params, context, false)?.toString() ?: return null
        return arrayOf(LookupElementBuilder.create(result))
    }

    private fun findLuaFunctionName(editor: Editor): String? {
        val document = editor.document
        var line = editor.caretModel.offset.let { document.getLineNumber(it) }

        while (line >= 0) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            val lineText = document.charsSequence.subSequence(lineStart, lineEnd).toString()

            val regex = Regex("""(?:local\s+)?function\s+([a-zA-Z_][a-zA-Z0-9_.:]*)\b""")
            val match = regex.find(lineText)
            if (match != null) {
                var name = match.groupValues[1]
                name = name.substringAfterLast(".")
                name = name.substringAfterLast(":")
                return name
            }
            line--
        }
        return null
    }
}
