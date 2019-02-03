package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.createParsingMap
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.isSingleKeyValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

class MiscSpec : StringSpec(
  {

    // Does the map contain only a single key/value pair (represented by a single line of property-style conf)?
    "is single key value".config(enabled = false) {
      val lines = arrayListOf("aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map.map)
      result shouldBe true
    }

    "is single deeper key value".config(enabled = false) {
      val lines = arrayListOf("aaa.bb.ee=ff", "aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map.map)
      result shouldBe false
    }


  }) {
  companion object {
    const val ALL_TESTS_ENABLED = true
//    const val ALL_TESTS_ENABLED=false
  }
}