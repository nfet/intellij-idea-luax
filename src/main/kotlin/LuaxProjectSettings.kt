package com.kapresoft.luax

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "LuaxProjectSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class LuaxProjectSettings : PersistentStateComponent<LuaxProjectSettings.State> {

    data class State(
        var isProjectLevel: Boolean = false,
        var logTemplate1: String = "",
        var logTemplate2: String = ""
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var isProjectLevel: Boolean
        get() = state.isProjectLevel
        set(value) { state.isProjectLevel = value }

    var logTemplate1: String
        get() = state.logTemplate1
        set(value) { state.logTemplate1 = value }

    var logTemplate2: String
        get() = state.logTemplate2
        set(value) { state.logTemplate2 = value }

    fun effectiveTemplate1(): String =
        if (isProjectLevel && logTemplate1.isNotBlank()) logTemplate1
        else LuaxAppSettings.getInstance().logTemplate1

    fun effectiveTemplate2(): String =
        if (isProjectLevel && logTemplate2.isNotBlank()) logTemplate2
        else LuaxAppSettings.getInstance().logTemplate2

    companion object {
        fun getInstance(project: Project): LuaxProjectSettings =
            project.getService(LuaxProjectSettings::class.java)
    }
}
