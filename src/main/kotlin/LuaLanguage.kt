package com.kapresoft.luax

import com.intellij.lang.Language

class LuaLanguage : Language("Luax") {
    companion object {
        const val PLUGIN_NAME = "Luax"
        val INSTANCE = LuaLanguage()
    }
}
