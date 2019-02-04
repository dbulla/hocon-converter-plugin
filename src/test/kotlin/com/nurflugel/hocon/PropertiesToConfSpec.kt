package com.nurflugel.hocon

import com.nurflugel.hocon.Utils.getListFromString
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
      val lines = getListFromString("""
        bbb.three = 5
        """)

      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe getListFromString("""
          bbb {
            three = 5
          }
""")
    }


    "properties format simple keys map 2".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString("""
        bbb.three = 5
        ccc.five = 6
        """)
      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe getListFromString("""
          bbb {
            three = 5
          }

          ccc {
            five = 6
          }
""")
    }


    "properties format simple keys map 3".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString("""
        aaa.zzz=5
        bbb.three = 5
        bbb.four.five=6
        bbb.four.siz=6
        ccc.five = 6
        ddd.eee="wood"
        """)
      val confLines = convertPropertiesToConf(lines)

      confLines shouldBe getListFromString("""
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
""")
    }


    "properties format simple keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString("""
        one="kkkk"
        two.three.four = 5
        """)
      val confLines = convertPropertiesToConf(lines)


      confLines shouldBe getListFromString("""
          one = "kkkk"
          two {
            three {
              four = 5
            }
          }
""")
    }


    "don't lose the includes in map formatter".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString("""
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """)
      val outputLines = convertPropertiesToConf(lines)
      // ensure order is preserved, as well as the includes just being there
      outputLines[0] shouldBe """include "reference2.conf""""
      outputLines[1] shouldBe """include "reference1.conf""""
      outputLines[2] shouldBe ""
    }

    "single mapped key should be as property".config(enabled = false) {
      //todo enable feature
      val lines = getListFromString("""
        aaaa {
           bbbb{
              cccc {
                 dddd = true
              }
           },
        }
        """)
      val propertyLines = convertConfToProperties(lines)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines shouldBe getListFromString("aaaa.bbb.cccc.dddd = true")
    }

    "read in a single line list".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        aa="ff"
        cors = ["123","456","789" ]
        dd=false
      """)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines.forEach { println(it) }
      confLines shouldBe getListFromString("""
        aa = "ff"
        cors = [
          "123",
          "456",
          "789"
        ]
        dd = false
      """.trimIndent())
    }

    /** I figure you should be all properties or not, but lists may be vertical, so we gotta deal with them */
    "read in a multi-line list all lines separate".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        cors = [
          "123",
          "456",
          "789"
        ]
      """)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines shouldBe getListFromString("""
        cors = [
          "123",
          "456",
          "789"
        ]
      """.trimIndent())
    }

    "read in a multi-line list [ same lines".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        cors = [ "123",
          "456",
          "789"
        ]
      """)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines shouldBe getListFromString("""
        cors = [
          "123",
          "456",
          "789"
        ]
      """.trimIndent())
    }

    "read in a multi-line list ] same lines".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        cors = [
          "123",
          "456",
          "789" ]
      """)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines shouldBe getListFromString("""
        cors = [
          "123",
          "456",
          "789"
        ]
      """.trimIndent())
    }


    "read in a multi-line list with other keys ] same lines".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        abc = 678
        cors = [
          "123",
          "456",
          "789" ]
        def=999
      """)
      val confLines = convertPropertiesToConf(propertyLines)
      confLines shouldBe getListFromString("""
        abc = 678
        cors = [
          "123",
          "456",
          "789"
        ]
        def = 999
      """.trimIndent())
    }

  }) {
  companion object {
    //    const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true
  }
}

