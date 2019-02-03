package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.convertConfToProperties
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

/*

Test cases:


 *
 * cors = [
 *    "some url"
 *    "another URL"
 *    "3 times a charm"
 *  ]
 * 
 * 
 * //comments
 * cors = [
 *    "some url"
 *    "another URL"
 *    "3 times a charm"
 *  ]
 * 
 * 
 * alpha.omega=false
 * // A comment before some code
 * alpha.beta.gamma=7
 * alpha.beta.delta=5
 * 
 * alpha.omega=false
 * // A comment before some code
 * // 2 comments before some code
 * alpha.beta.gamma=7
 * alpha.beta.delta=5
 * 
 * 
 * 
 * aaaa {
 * // A comment before some code
 *    bbbb {
 *         abab= 5
 *         // A comment before some code
 *         cccc = "text"
 *         dddd = true
 *      }
 *  }
 *  
 *  also - auto quote wrapping
 */
class ConfToPropertiesSpec : StringSpec(
  {

    "conf format simple keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        one = "kkkk"
        two.three.four = 5
        """.trimIndent().split("\n")
      val propertyLines = convertConfToProperties(lines)
      propertyLines shouldBe lines
    }

    "conf format with map of keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        aaaa {
        bbbb = 5
        cccc = "text"
        dddd = true
        }
""".trimIndent().split("\n")
      val propertyLines = convertConfToProperties(lines)
      propertyLines shouldBe """
        aaaa.bbbb = 5
        aaaa.cccc = "text"
        aaaa.dddd = true
""".trimIndent().split("\n")
    }

    "don't lose the includes in property formatter".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """.trimIndent().split("\n")
      val outputLines = convertConfToProperties(lines)

      // same with the other conversion
      outputLines[0] shouldBe """include "reference2.conf""""
      outputLines[1] shouldBe """include "reference1.conf""""
      outputLines[2] shouldBe ""
    }
  }) {
  companion object {
    const val ALL_TESTS_ENABLED = true
//    const val ALL_TESTS_ENABLED=false
  }
}