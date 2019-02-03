package com.nurflugel.hocon

import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.convertConfToProperties
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.convertPropertiesToConf
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
class PropertiesToConfSpec : StringSpec(
  {

    "properties format simple keys map 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        bbb.three = 5
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe """
          bbb {
            three = 5
          }
""".trimIndent().split("\n")
    }


    "properties format simple keys map 2".config(enabled = true) {
      val lines = """
        bbb.three = 5
        ccc.five = 6
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe """
          bbb {
            three = 5
          }

          ccc {
            five = 6
          }
""".trimIndent().split("\n")
    }


    "properties format simple keys map 3".config(enabled = true) {
      val lines = """
        aaa.zzz=5
        bbb.three = 5
        bbb.four.five=6
        bbb.four.siz=6
        ccc.five = 6
        ddd.eee="wood"
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe """
          aaa {
            zzz = 5
          }

          bbb {
            four {
              five = 6
              siz = 6
            }
            three = 5
          }

          ccc {
            five = 6
          }

          ddd {
            eee = "wood"
          }
""".trimIndent().split("\n")
    }



    "properties format simple keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = """
        one="kkkk"
        two.three.four = 5
        """.trimIndent().split("\n")
      val confLines = convertPropertiesToConf(lines)


      confLines shouldBe """
          one = "kkkk"
          two {
            three {
              four = 5
            }
          }
""".trimIndent().split("\n")
    }


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

    "single mapped key should be as property".config(enabled = false) {
      //todo enable feature
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

  }) {
  companion object {
    const val ALL_TESTS_ENABLED = true
//    const val ALL_TESTS_ENABLED=false
  }
}