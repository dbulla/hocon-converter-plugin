package com.nurflugel.hocon.cofig

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

object ProjectSettings {
  val PREFIX = "HOCON_Converter_"
  val PLUGIN_ENABLED_IN_PROJECT = PREFIX + "EnabledInProject"
  val FLATTEN_KEYS_ENABLED = PREFIX + "IS_FLATTEN_KEYS_Enabled"

  fun isPluginEnabledInProject(project: Project): Boolean {
    return isEnabled(project, PLUGIN_ENABLED_IN_PROJECT)
  }

  fun setPluginEnabledInProject(project: Project, value: Boolean) {
    setEnabled(project, PLUGIN_ENABLED_IN_PROJECT, value)
  }

  fun isFlattenKeys(project: Project?): Boolean {
    if (project == null) return true // todo remove when fixed
    return ProjectSettings.isEnabled(project, FLATTEN_KEYS_ENABLED)
  }

  fun isEnabled(project: Project, propertyName: String): Boolean {
    return isEnabled(PropertiesComponent.getInstance(project), propertyName)
  }

  fun isEnabled(project: Project, propertyName: String, defaultValue: Boolean): Boolean {
    return isEnabled(PropertiesComponent.getInstance(project), propertyName, defaultValue)
  }

  @JvmOverloads
  fun isEnabled(properties: PropertiesComponent, propertyName: String, defaultValue: Boolean = true): Boolean {
    return properties.getBoolean(propertyName, defaultValue)
  }

  fun setEnabled(project: Project, propertyName: String, value: Boolean) {
    setEnabled(PropertiesComponent.getInstance(project), propertyName, value)
  }

  fun setEnabled(properties: PropertiesComponent, propertyName: String, value: Boolean) {
    properties.setValue(propertyName, value.toString(), "")
  }


}
