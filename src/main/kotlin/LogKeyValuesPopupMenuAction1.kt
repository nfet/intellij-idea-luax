package com.kapresoft.luax

import com.intellij.openapi.project.Project

@Suppress("ActionPresentationInstantiatedInCtor", "UnstableApiUsage")
class LogKeyValuesPopupMenuAction1 : LogKeyValuesActionBase(
    LuaBundle.lazyMessage("action.log.key-values.text"),
    LuaBundle.lazyMessage("action.log.key-values.description")
) {
    override fun effectiveTemplate(project: Project): String =
        LuaxProjectSettings.getInstance(project).effectiveTemplate1()
}
