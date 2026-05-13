package com.kapresoft.luax

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object LuaFileType : LanguageFileType(LuaLanguage.INSTANCE) {

    private val icon: Icon = IconLoader.getIcon("/icons/lua.svg", LuaFileType::class.java)

    override fun getName(): String = "Lua"
    override fun getDescription(): String = "Lua source file"
    override fun getDefaultExtension(): String = "lua"
    override fun getIcon(): Icon = icon
}


