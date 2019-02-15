package com.nurflugel.hocon

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBus
import com.nurflugel.hocon.cofig.ProjectSettings
import io.mockk.every
import io.mockk.mockk
import org.picocontainer.PicoContainer


class MyMockProject(private val flattenKeys: Boolean) : Project {


  override fun getPicoContainer(): PicoContainer {
    val container = mockk<PicoContainer>()
    every { container.toString() } returns "dibble!"


    val mockComponent = mockk<PropertiesComponent>()

    every { mockComponent.getBoolean(ProjectSettings.FLATTEN_KEYS_ENABLED, any()) } returns flattenKeys
    every { container.getComponentInstance(any()) } returns mockComponent
    return container
  }

  override fun isDisposed(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getWorkspaceFile(): VirtualFile? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProjectFilePath(): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getName(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> getExtensions(extensionPointName: ExtensionPointName<T>): Array<T> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getComponent(name: String): BaseComponent {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> getComponent(interfaceClass: Class<T>): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> getComponent(interfaceClass: Class<T>, defaultImplementationIfAbsent: T): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getBaseDir(): VirtualFile {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun isOpen(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun save() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDisposed(): Condition<*> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> getComponents(baseClass: Class<T>): Array<T> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


  override fun getProjectFile(): VirtualFile? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Any?> getUserData(key: Key<T>): T? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun isInitialized(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun hasComponent(interfaceClass: Class<*>): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getMessageBus(): MessageBus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun isDefault(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getBasePath(): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getLocationHash(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun dispose() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}