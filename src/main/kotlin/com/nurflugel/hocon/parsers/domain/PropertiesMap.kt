package com.nurflugel.hocon.parsers.domain

class PropertiesMap {
  val map = mutableMapOf<String, Any>()
  val includesList = mutableListOf<String>()

  public fun addInclude(include: String) {
    includesList.add(include)
  }
}