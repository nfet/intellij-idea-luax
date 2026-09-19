package com.kapresoft.luax

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class LuaxSettingsPanel {

    val templateField1 = JBTextField().apply {
        emptyText.text = MyMessageBundle.message("settings.luax.logTemplate1.placeholder")
        toolTipText = MyMessageBundle.message("settings.luax.logTemplate1.tooltip")
    }

    val templateField2 = JBTextField().apply {
        emptyText.text = MyMessageBundle.message("settings.luax.logTemplate2.placeholder")
        toolTipText = MyMessageBundle.message("settings.luax.logTemplate2.tooltip")
    }

    val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel(MyMessageBundle.message("settings.luax.logTemplate1.label")), templateField1, 1, false)
        .addLabeledComponent(JBLabel(MyMessageBundle.message("settings.luax.logTemplate2.label")), templateField2, 1, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    var template1: String
        get() = templateField1.text
        set(value) { templateField1.text = value }

    var template2: String
        get() = templateField2.text
        set(value) { templateField2.text = value }
}