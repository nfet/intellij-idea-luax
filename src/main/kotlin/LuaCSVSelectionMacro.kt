package com.kapresoft.luax

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.codeInsight.template.macro.MacroBase

class LuaCSVSelectionMacro : MacroBase("luaCSVSelection", "luaCSVSelection()") {

    override fun getPresentableName(): String = "luaCSVSelection()"

    override fun calculateResult(
        params: Array<out Expression>,
        context: ExpressionContext,
        quick: Boolean
    ): Result? {
        val selectedText = params.firstOrNull()?.calculateResult(context)?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return null

        val parts = selectedText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val result = parts.joinToString(", ") { "'$it=', $it" }
        return TextResult(result)
    }

    override fun calculateLookupItems(
        params: Array<out Expression>,
        context: ExpressionContext
    ): Array<LookupElement>? = null
}
