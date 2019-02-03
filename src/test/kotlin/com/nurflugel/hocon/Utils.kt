package com.nurflugel.hocon

object Utils {
  fun getListFromString(dd: String) = dd.trimIndent().split("\n")
}