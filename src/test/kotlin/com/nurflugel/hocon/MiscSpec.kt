package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.HoconParser
import com.nurflugel.hocon.parsers.HoconParser.Companion.isSingleKeyValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MiscSpec : StringSpec(
  {

    // Does the map contain only a single key/value pair (represented by a single line of property-style conf)?
      "is single key value".config(enabled = ALL_TESTS_ENABLED) {
      val lines = arrayListOf("aa.bb.cc.dd=f")
      val map = HoconParser.populatePropsMap(lines)
      val result = isSingleKeyValue(map.map)
      result shouldBe true
    }

      "is single deeper key value".config(enabled = ALL_TESTS_ENABLED) {
      val lines = arrayListOf("aaa.bb.ee=ff", "aa.bb.cc.dd=f")
      val map = HoconParser.populatePropsMap(lines)
      val result = isSingleKeyValue(map.map)
      result shouldBe false
    }


  }) {
  companion object {
    const val ALL_TESTS_ENABLED = true
//    const val ALL_TESTS_ENABLED=false
  }
}