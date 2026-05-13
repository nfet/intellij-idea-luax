package com.kapresoft.luax

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@State(name = "LuaxProjectSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class LuaxProjectSettings : PersistentStateComponent<LuaxProjectSettings.State> {

    data class State(
        var isProjectLevel: Boolean = false,
        var logTemplate: String = ""
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var isProjectLevel: Boolean
        get() = state.isProjectLevel
        set(value) { state.isProjectLevel = value }

    var logTemplate: String
        get() = state.logTemplate
        set(value) { state.logTemplate = value }

    fun effectiveTemplate(): String =
        if (isProjectLevel && logTemplate.isNotBlank()) logTemplate
        else LuaxAppSettings.getInstance().logTemplate

    companion object {
        fun getInstance(project: Project): LuaxProjectSettings =
            project.getService(LuaxProjectSettings::class.java)
    }
}