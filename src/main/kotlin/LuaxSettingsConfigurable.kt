package com.kapresoft.luax

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class LuaxSettingsConfigurable(private val project: Project) : Configurable {

    private var panel: LuaxSettingsPanel? = null
    private var projectLevelCheckbox: JBCheckBox? = null
    private var root: JPanel? = null

    override fun getDisplayName(): String = "Luax"

    override fun createComponent(): JComponent {
        val settingsPanel = LuaxSettingsPanel()
        val checkbox = JBCheckBox(MyMessageBundle.message("settings.luax.projectLevel.checkbox"))
        panel = settingsPanel
        projectLevelCheckbox = checkbox

        checkbox.addActionListener { loadFromCurrentScope() }

        root = FormBuilder.createFormBuilder()
            .addComponent(checkbox)
            .addLabeledComponent(MyMessageBundle.message("settings.luax.logTemplate1.label"), settingsPanel.templateField1, 1, false)
            .addLabeledComponent(MyMessageBundle.message("settings.luax.logTemplate2.label"), settingsPanel.templateField2, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return root!!
    }

    override fun isModified(): Boolean {
        val checkbox = projectLevelCheckbox ?: return false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        if (checkbox.isSelected != projectSettings.isProjectLevel) return true
        val currentTemplate1 = if (checkbox.isSelected)
            projectSettings.logTemplate1
        else
            LuaxAppSettings.getInstance().logTemplate1
        val currentTemplate2 = if (checkbox.isSelected)
            projectSettings.logTemplate2
        else
            LuaxAppSettings.getInstance().logTemplate2
        return panel?.template1 != currentTemplate1 || panel?.template2 != currentTemplate2
    }

    override fun apply() {
        val template1 = panel?.template1 ?: ""
        if (template1.isNotBlank() && !template1.contains("\${args}"))
            throw ConfigurationException("Template must contain \${args}")
        val template2 = panel?.template2 ?: ""
        if (template2.isNotBlank() && !template2.contains("\${args}"))
            throw ConfigurationException("Template 2 must contain \${args}")
        val isProjectLevel = projectLevelCheckbox?.isSelected ?: false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        projectSettings.isProjectLevel = isProjectLevel
        if (isProjectLevel) {
            projectSettings.logTemplate1 = template1
            projectSettings.logTemplate2 = template2
        } else {
            LuaxAppSettings.getInstance().logTemplate1 = template1
            LuaxAppSettings.getInstance().logTemplate2 = template2
        }
    }

    override fun reset() {
        val projectSettings = LuaxProjectSettings.getInstance(project)
        val isProjectLevel = projectSettings.isProjectLevel
        projectLevelCheckbox?.isSelected = isProjectLevel
        panel?.template1 = if (isProjectLevel)
            projectSettings.logTemplate1
        else
            LuaxAppSettings.getInstance().logTemplate1
        panel?.template2 = if (isProjectLevel)
            projectSettings.logTemplate2
        else
            LuaxAppSettings.getInstance().logTemplate2
    }

    override fun disposeUIResources() {
        panel = null
        projectLevelCheckbox = null
        root = null
    }

    private fun loadFromCurrentScope() {
        val isProjectLevel = projectLevelCheckbox?.isSelected ?: false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        panel?.template1 = if (isProjectLevel)
            projectSettings.logTemplate1.ifBlank { LuaxAppSettings.getInstance().logTemplate1 }
        else
            LuaxAppSettings.getInstance().logTemplate1
        panel?.template2 = if (isProjectLevel)
            projectSettings.logTemplate2.ifBlank { LuaxAppSettings.getInstance().logTemplate2 }
        else
            LuaxAppSettings.getInstance().logTemplate2
    }
}