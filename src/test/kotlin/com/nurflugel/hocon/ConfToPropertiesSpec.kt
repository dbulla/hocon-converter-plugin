package com.nurflugel.hocon

import com.nurflugel.hocon.generators.PropertiesGenerator.generatePropertiesOutput
import com.nurflugel.hocon.parsers.HoconParser.Companion.populatePropsMap
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.commons.lang3.StringUtils

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
      val lines = Utils.getListFromString("""
        one = "kkkk"
        two.three.four = 5
        """)
      val propertyLines = convertToProperties(lines)
      propertyLines shouldBe lines
    }

    "conf format with map of keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = Utils.getListFromString("""
        aaaa {
        bbbb = 5
        cccc = "text"
        dddd = true
        }
""")
      val propertyLines = convertToProperties(lines)
      propertyLines shouldBe Utils.getListFromString("""
        aaaa.bbbb = 5
        aaaa.cccc = "text"
        aaaa.dddd = true
""")
    }

    "don't lose the includes in property formatter".config(enabled = ALL_TESTS_ENABLED) {
      val lines = Utils.getListFromString("""
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """)
      val outputLines = convertToProperties(lines)

      // same with the other conversion
      outputLines[0] shouldBe """include "reference2.conf""""
      outputLines[1] shouldBe """include "reference1.conf""""
      outputLines[2] shouldBe ""
    }
  }) {
  companion object {
    //      const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true

    /**
     * this assumes the lines being parsed are pure property lines - just
     * stuff like aaa.bbb.ccc.dd=true, no maps
     */
    fun convertToProperties(existingLines: List<String>): MutableList<String> {
      val propsMap: PropertiesMap = populatePropsMap(existingLines)
      val generatePropertiesOutput = generatePropertiesOutput(propsMap)

      val lines = mutableListOf<String>()
      generatePropertiesOutput.forEach { line ->
        when {
          line.contains("\n") -> {
            val indent = getIndentFromLine(line)
            val split = line.split("\n")
            for (subLine in split) {

              lines.add("$indent  $subLine")
            }
          }
          else -> lines.add(line)
        }
      }

      return lines
    }

    private fun getIndentFromLine(line: String): String {
      val whiteSpace = StringUtils.substringBefore(line, line.trim())
      return whiteSpace
    }

  }
}