package com.kapresoft.luax

import com.intellij.openapi.project.Project

@Suppress("ActionPresentationInstantiatedInCtor", "UnstableApiUsage")
class LogKeyValuesPopupMenuAction2 : LogKeyValuesActionBase(
    LuaBundle.lazyMessage("action.log.key-values-2.text"),
    LuaBundle.lazyMessage("action.log.key-values-2.description")
) {
    override fun effectiveTemplate(project: Project): String =
        LuaxProjectSettings.getInstance(project).effectiveTemplate2()
}
