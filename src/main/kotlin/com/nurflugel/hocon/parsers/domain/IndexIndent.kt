package com.nurflugel.hocon.parsers.domain

class IndexIndent(
    /** line index */
    var index: Int = 0,
    /** Indent level of current line */
    var indent: Int = 0
) {
    fun increment() {
        index++
    }
}
