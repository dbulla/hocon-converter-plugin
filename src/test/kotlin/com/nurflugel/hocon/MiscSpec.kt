package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.HoconParser
import com.nurflugel.hocon.parsers.HoconParser.Companion.isSingleKeyValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MiscSpec : StringSpec(
  {

    // Does the map contain only a single key/value pair (represented by a single line of property-style conf)?
      "is single key value (is it flat)".config(enabled = ALL_TESTS_ENABLED) {
          val lines = Utils.getListFromString("aa.bb.cc.dd=f")
          val map = HoconParser.populatePropsMap(lines)
      val result = isSingleKeyValue(map.map)
      result shouldBe true
    }

      "is single deeper key value".config(enabled = ALL_TESTS_ENABLED) {
          val lines = Utils.getListFromString(
              """
      "aaa.bb.ee=ff",
      "aa.bb.cc.dd=f"
      """
          )
      val map = HoconParser.populatePropsMap(lines)
      val result = isSingleKeyValue(map.map)
      result shouldBe false
    }


      "flat path output - first case single key".config(enabled = true) {
          val lines = Utils.getListFromString("aaa.bb.ee=ff")
          PropertiesToConfSpec.convertToConf(lines, true) shouldBe Utils.getListFromString("aaa.bb.ee = ff")
      }

      "flat path output - second case nested key".config(enabled = false) {
          val lines = Utils.getListFromString(
              """
          aaa.bb.ee=ff
          aaa.bb.cc.dd=gg
          """.trimIndent()
          )
          PropertiesToConfSpec.convertToConf(lines, false) shouldBe Utils.getListFromString(
              """
          aaa.bb {
            ee = ff
          }
          """
          )
      }

  }) {
  companion object {
    const val ALL_TESTS_ENABLED = true
//    const val ALL_TESTS_ENABLED=false
  }
}