package com.kapresoft.luax

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.Language
import org.codehaus.groovy.runtime.DefaultGroovyMethods.printf

class LuaTemplateContextType : TemplateContextType("LuaX") {
    override fun isInContext(context: TemplateActionContext): Boolean {
        val originalFile = context.file.originalFile
        val extension = originalFile.virtualFile?.extension

//        println("=== LuaTemplateContextType Debug ===")
//        println("originalFile.language.id: ${originalFile.language.id}")
//        println("originalFile.virtualFile?.extension: $extension")
//        println("is .lua? ${extension.equals("lua", ignoreCase = true)}")
//        println("=====================================")

        return extension.equals("lua", ignoreCase = true)
    }
}
