package com.kapresoft.luax

object LuaEditorUtil {

    fun findLuaFunctionName(text: String): String? {
        val namedFunctionRegex = Regex("""(?:local\s+)?function\s+([a-zA-Z_][a-zA-Z0-9_.:]*)\s*\(""")
        val anonymousFunctionRegex = Regex("""\bfunction\s*\(""")
        val blockOpenerRegex = Regex("""\b(do|then|repeat)\b""")
        val endRegex = Regex("""\bend\b""")

        val lines = text.lines()
        var depth = 0

        for (line in lines.asReversed()) {
            val endCount = endRegex.findAll(line).count()
            val namedMatch = namedFunctionRegex.find(line)
            val anonCount = anonymousFunctionRegex.findAll(line).count()
            val openerCount = blockOpenerRegex.findAll(line).count()

            val openersOnLine = anonCount + openerCount + (if (namedMatch != null) 1 else 0)
            depth += endCount - openersOnLine

            if (namedMatch != null && depth <= 0) {
                var name = namedMatch.groupValues[1]
                name = name.substringAfterLast(".")
                name = name.substringAfterLast(":")
                return name
            }
        }
        return null
    }
}