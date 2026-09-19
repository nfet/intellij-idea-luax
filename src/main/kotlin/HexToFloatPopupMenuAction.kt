package com.kapresoft.luax

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

private val isHexChar = { c: Char -> c.isLetterOrDigit() || c == '#' }

@Suppress("ActionPresentationInstantiatedInCtor", "UnstableApiUsage")
class HexToFloatPopupMenuAction : AnAction(
    LuaBundle.lazyMessage("action.hex-to-float.text"),
    LuaBundle.lazyMessage("action.hex-to-float.description")
) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectedText = LuaEditorUtil.candidateText(editor, isHexChar) ?: return
        val floatRgb = LuaEditorUtil.hexToFloatRgb(selectedText) ?: return

        LuaEditorUtil.copyToClipboardWithNotification(e.project, floatRgb, "Copied color float")
    }

    override fun update(e: AnActionEvent) =
        LuaEditorUtil.updateColorActionPresentation(e, isHexChar, LuaEditorUtil::hexToFloatRgb)
}
