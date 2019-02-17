package com.nurflugel.hocon.cofig.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.isFlattenKeys
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.isPluginEnabledInProject
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.isTopLevelListsAtBottom
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.setFlattenKeys
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.setPluginEnabledInProject
import com.nurflugel.hocon.cofig.ProjectSettings.Companion.setTopLevelListsAtBottom
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nls.Capitalization.Title
import java.awt.event.ActionEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class ProjectSettingsPage(private val propertiesComponent: PropertiesComponent) : SearchableConfigurable, Configurable.NoScroll {
    // these need to be vars so the Intellij GUI binder can handle them
    private var flattenKeysCheckbox: JCheckBox? = null
    private var enablePluginInProjectCheckBox: JCheckBox? = null
    private var containingPanel: JPanel? = null
    private var listsAtBottomCheckbox: JCheckBox? = null

    override fun getId(): String {
        return "HOCON Converter Plugin"
    }

    @Nls(capitalization = Title)
    override fun getDisplayName(): String? {
        return null
    }

    override fun createComponent(): JComponent? {
        initFromSettings()
        enablePluginInProjectCheckBox!!.addActionListener { createCheckbox(it, flattenKeysCheckbox) }
        listsAtBottomCheckbox!!.addActionListener { actionEvent -> createCheckbox(actionEvent, listsAtBottomCheckbox) }
        return containingPanel
    }

    private fun createCheckbox(actionEvent: ActionEvent, checkbox: JCheckBox?) {
        val checkBox = actionEvent.source as JCheckBox
        val selected = checkBox.model.isSelected
        checkbox!!.isSelected = selected
    }

    private fun initFromSettings() {
        println("initFromSettings")
        enablePluginInProjectCheckBox!!.isSelected = isPluginEnabledInProject(propertiesComponent)
        flattenKeysCheckbox!!.isSelected = isFlattenKeys(propertiesComponent)
        listsAtBottomCheckbox!!.isSelected = isTopLevelListsAtBottom(propertiesComponent)
    }

    override fun isModified(): Boolean {
        println("isModified")
        val enabledChanged = enablePluginInProjectCheckBox!!.isSelected != isPluginEnabledInProject(propertiesComponent)
        val flattenKeysChanged = flattenKeysCheckbox!!.isSelected != isFlattenKeys(propertiesComponent)
        return enabledChanged || flattenKeysChanged

    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        println("Applying settings")
        setPluginEnabledInProject(propertiesComponent, enablePluginInProjectCheckBox!!.isSelected)
        setFlattenKeys(propertiesComponent, flattenKeysCheckbox!!.isSelected)
        setTopLevelListsAtBottom(propertiesComponent, flattenKeysCheckbox!!.isSelected)
    }

    override fun reset() {
        initFromSettings()
    }

}
