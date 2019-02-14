package com.nurflugel.hocon.cofig.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.nurflugel.hocon.cofig.ProjectSettings.FLATTEN_KEYS_ENABLED
import com.nurflugel.hocon.cofig.ProjectSettings.PLUGIN_ENABLED_IN_PROJECT
import com.nurflugel.hocon.cofig.ProjectSettings.isEnabled
import com.nurflugel.hocon.cofig.ProjectSettings.setEnabled
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nls.Capitalization.Title
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class ProjectSettingsPage(private val propertiesComponent: PropertiesComponent) : SearchableConfigurable, Configurable.NoScroll {
  // these need to be vars so the Intellij GUI binder can handle them
  private var flattenKeysCheckbox: JCheckBox? = null
  private var enablePluginInProjectCheckBox: JCheckBox? = null
  private var containingPanel: JPanel? = null

  override fun getId(): String {
    return "HOCON Converter Plugin"
  }

  @Nls(capitalization = Title)
  override fun getDisplayName(): String? {
    return null
  }

  override fun createComponent(): JComponent? {
    initFromSettings()
    enablePluginInProjectCheckBox!!.addActionListener { actionEvent ->
      val checkBox = actionEvent.source as JCheckBox
      val selected = checkBox.model.isSelected
      flattenKeysCheckbox!!.isSelected = selected
    }
    return containingPanel
  }

  private fun initFromSettings() {
    println("initFromSettings")
    enablePluginInProjectCheckBox!!.isSelected = isEnabled(propertiesComponent, PLUGIN_ENABLED_IN_PROJECT)
    flattenKeysCheckbox!!.isSelected = isEnabled(propertiesComponent, FLATTEN_KEYS_ENABLED)
  }

  override fun isModified(): Boolean {
    println("isModified")
    val enabledChanged = enablePluginInProjectCheckBox!!.isSelected != isEnabled(propertiesComponent, PLUGIN_ENABLED_IN_PROJECT)
    val flattenKeysChanged = flattenKeysCheckbox!!.isSelected != isEnabled(propertiesComponent, FLATTEN_KEYS_ENABLED)
    return enabledChanged || flattenKeysChanged

  }

  @Throws(ConfigurationException::class)
  override fun apply() {
    println("Applying settings")
    setEnabled(propertiesComponent, PLUGIN_ENABLED_IN_PROJECT, enablePluginInProjectCheckBox!!.isSelected)
    setEnabled(propertiesComponent, FLATTEN_KEYS_ENABLED, flattenKeysCheckbox!!.isSelected)

  }

  override fun reset() {
    initFromSettings()
  }

}
