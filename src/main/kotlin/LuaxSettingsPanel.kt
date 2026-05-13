package com.kapresoft.luax

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class LuaxSettingsPanel {

    val templateField = JBTextField().apply {
        emptyText.text = MyMessageBundle.message("settings.luax.logTemplate.placeholder")
        toolTipText = MyMessageBundle.message("settings.luax.logTemplate.tooltip")
    }

    val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel(MyMessageBundle.message("settings.luax.logTemplate.label")), templateField, 1, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    var template: String
        get() = templateField.text
        set(value) { templateField.text = value }
}