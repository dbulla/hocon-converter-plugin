package com.nurflugel.hocon.parsers.domain

import java.util.*

class IndexIndent(
  /** line index */
    var index: Int = 0,
  /** Indent level of current line */
  var indent: Int = 0,
  val keyStack: Stack<String> = Stack()
) {
    fun increment() {
        index++
    }

  override fun toString(): String {
    return "index: $index, indent: $indent keyStack:${keyStack.joinToString { ":" }}"
  }
}
