package com.kapresoft.luax

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "LuaxAppSettings", storages = [Storage("luax.xml")])
class LuaxAppSettings : PersistentStateComponent<LuaxAppSettings.State> {

    data class State(
        var logTemplate1: String = DEFAULT_TEMPLATE_1,
        var logTemplate2: String = DEFAULT_TEMPLATE_2
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var logTemplate1: String
        get() = state.logTemplate1.ifBlank { DEFAULT_TEMPLATE_1 }
        set(value) { state.logTemplate1 = value }

    var logTemplate2: String
        get() = state.logTemplate2.ifBlank { DEFAULT_TEMPLATE_2 }
        set(value) { state.logTemplate2 = value }

    companion object {
        // fallback [DEBUG]
        const val DEFAULT_TEMPLATE_1 = """print(${"$"}{functionName}, ${"$"}{args})"""
        const val DEFAULT_TEMPLATE_2 = """print(libName, ${"$"}{functionName}, ${"$"}{args})"""

        fun getInstance(): LuaxAppSettings =
            ApplicationManager.getApplication().getService(LuaxAppSettings::class.java)
    }
}
