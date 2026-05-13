package com.kapresoft.luax

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "LuaxAppSettings", storages = [Storage("luax.xml")])
class LuaxAppSettings : PersistentStateComponent<LuaxAppSettings.State> {

    data class State(var logTemplate: String = DEFAULT_TEMPLATE)

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var logTemplate: String
        get() = state.logTemplate
        set(value) { state.logTemplate = value }

    companion object {
        // fallback [DEBUG]
        const val DEFAULT_TEMPLATE = """print(${"$"}{functionName}, ${"$"}{args})"""

        fun getInstance(): LuaxAppSettings =
            ApplicationManager.getApplication().getService(LuaxAppSettings::class.java)
    }
}
