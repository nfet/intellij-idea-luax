package com.kapresoft.luax

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

private val isFloatListChar = { c: Char -> c.isDigit() || c == '.' || c == ',' || c == ' ' }

@Suppress("ActionPresentationInstantiatedInCtor", "UnstableApiUsage")
class FloatToHexPopupMenuAction : AnAction(
    LuaBundle.lazyMessage("action.float-to-hex.text"),
    LuaBundle.lazyMessage("action.float-to-hex.description")
) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = LuaEditorUtil.candidateText(editor, isFloatListChar) ?: return
        val hex = LuaEditorUtil.floatRgbToHex(selectedText) ?: return

        LuaEditorUtil.copyToClipboardWithNotification(e.project, hex, "Copied color hex")
    }

    override fun update(e: AnActionEvent) =
        LuaEditorUtil.updateColorActionPresentation(e, isFloatListChar, LuaEditorUtil::floatRgbToHex)
}
