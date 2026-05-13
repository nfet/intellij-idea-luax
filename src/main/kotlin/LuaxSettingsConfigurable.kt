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
            .addLabeledComponent(MyMessageBundle.message("settings.luax.logTemplate.label"), settingsPanel.templateField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return root!!
    }

    override fun isModified(): Boolean {
        val checkbox = projectLevelCheckbox ?: return false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        if (checkbox.isSelected != projectSettings.isProjectLevel) return true
        val currentTemplate = if (checkbox.isSelected)
            projectSettings.logTemplate
        else
            LuaxAppSettings.getInstance().logTemplate
        return panel?.template != currentTemplate
    }

    override fun apply() {
        val template = panel?.template ?: LuaxAppSettings.DEFAULT_TEMPLATE
        if (!template.contains("\${args}"))
            throw ConfigurationException("Template must contain \${args}")
        val isProjectLevel = projectLevelCheckbox?.isSelected ?: false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        projectSettings.isProjectLevel = isProjectLevel
        if (isProjectLevel) {
            projectSettings.logTemplate = template
        } else {
            LuaxAppSettings.getInstance().logTemplate = template
        }
    }

    override fun reset() {
        val projectSettings = LuaxProjectSettings.getInstance(project)
        val isProjectLevel = projectSettings.isProjectLevel
        projectLevelCheckbox?.isSelected = isProjectLevel
        panel?.template = if (isProjectLevel)
            projectSettings.logTemplate
        else
            LuaxAppSettings.getInstance().logTemplate
    }

    override fun disposeUIResources() {
        panel = null
        projectLevelCheckbox = null
        root = null
    }

    private fun loadFromCurrentScope() {
        val isProjectLevel = projectLevelCheckbox?.isSelected ?: false
        val projectSettings = LuaxProjectSettings.getInstance(project)
        panel?.template = if (isProjectLevel)
            projectSettings.logTemplate.ifBlank { LuaxAppSettings.getInstance().logTemplate }
        else
            LuaxAppSettings.getInstance().logTemplate
    }
}