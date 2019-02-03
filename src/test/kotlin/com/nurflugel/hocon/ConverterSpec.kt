package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.convertConfToProperties
import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.createParsingMap
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.convertPropertiesToConf
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.isSingleKeyValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

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
class ConverterSpec : StringSpec(
  {


    "properties format simple keys map 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        bbb.three = 5
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)
      val expectedLines = """
          bbb {
            three = 5
          }
""".trimIndent().split("\n")

      confLines shouldBe expectedLines


    }
    "properties format simple keys map 2".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        aaa="kkkk"
        bbb.three = 5
        ccc.five = 6
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)


      val expectedLines = """
          aaa = "kkkk"
          bbb {
            three = 5
          }
          ccc {
            five = 6
          }
""".trimIndent().split("\n")

      confLines shouldBe expectedLines
    }
    "properties format simple keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        one="kkkk"
        two.three.four = 5
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)


      val expectedLines = """
          one = "kkkk"
          two {
            three {
              four = 5
            }
          }
""".trimIndent().split("\n")

      confLines shouldBe expectedLines
    }

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
      val expectedLines = """
        aaaa.bbbb = 5
        aaaa.cccc = "text"
        aaaa.dddd = true
""".trimIndent().split("\n")
      propertyLines shouldBe expectedLines
    }

    "single mapped key should be as property".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        aaaa {
           bbbb{
              cccc {
                 dddd = true
              }
           },
        }
        """.trimIndent().split("\n")
      val propertyLines = convertConfToProperties(lines)
      val confLines = convertPropertiesToConf(propertyLines)
      val expectedLines = arrayListOf("aaaa.bbb.cccc.dddd = true")
      confLines shouldBe expectedLines
    }

    "is single key value 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = arrayListOf("aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map.map)
      result shouldBe true
    }

    "is single deeper key value 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = arrayListOf("aaa.bb.ee=ff", "aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map.map)
      result shouldBe false
    }


    //todo write test for 'include xxxxxx'
    "don't lose the includes in map formatter".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """.trimIndent().split("\n")
      val outputLines = convertPropertiesToConf(lines)
      // ensure order is preserved, as well as the includes just being there
      outputLines[0] shouldBe """include "reference2.conf""""
      outputLines[1] shouldBe """include "reference1.conf""""
      outputLines[2] shouldBe ""
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