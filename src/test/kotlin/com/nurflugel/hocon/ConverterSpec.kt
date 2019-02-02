package com.nurflugel.hocon

import com.nurflugel.hocon.FileUtil.Companion.convertConfToProperties
import com.nurflugel.hocon.FileUtil.Companion.convertPropertiesToConf
import com.nurflugel.hocon.FileUtil.Companion.createParsingMap
import com.nurflugel.hocon.FileUtil.Companion.isSingleKeyValue
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

    "properties format simple keys" {
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

    "conf format simple keys" {
      val lines = """
        one = "kkkk"
        two.three.four = 5
        """.trimIndent().split("\n")
      val propertyLines = convertConfToProperties(lines)
      propertyLines shouldBe lines
    }

    "conf format with map of keys"{
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

    "single mapped key should be as property".config(enabled = false) {
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

    "is single key value 1".config(enabled = false) {
      val lines = arrayListOf("aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map)
      result shouldBe true
    }

    "is single deeper key value 1"{
      val lines = arrayListOf("aaa.bb.ee=ff", "aa.bb.cc.dd=f")
      val map = createParsingMap(lines, Stack(), mutableListOf())
      val result = isSingleKeyValue(map)
      result shouldBe false
    }


    //todo write test for 'include xxxxxx'
    "don't lose the includes".config(enabled = false) {
      val lines = """
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)
      val propertyLines = convertConfToProperties(lines)
      // ensure order is preserved, as well as the includes just being there
      confLines[0] shouldBe """include "reference2.conf""""
      confLines[1] shouldBe """include "reference1.conf""""
      // same with hte other conversion
      propertyLines[0] shouldBe """include "reference2.conf""""
      propertyLines[1] shouldBe """include "reference1.conf""""
    }
  })