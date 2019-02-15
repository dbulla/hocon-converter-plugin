package com.nurflugel.hocon

import com.nurflugel.hocon.ConfToPropertiesSpec.Companion.convertToProperties
import com.nurflugel.hocon.PropertiesToConfSpec.Companion.convertToConf
import com.nurflugel.hocon.Utils.getListFromString
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CommentsSpec : StringSpec(
    {

        "read in a single line comment".config(enabled = ALL_TESTS_ENABLED) {
            val propertyLines = getListFromString("""
        # comment 1
        aa="ff"
        dd=false
      """)
            // test prop output
            convertToProperties(propertyLines) shouldBe getListFromString("""
        # comment 1
        aa = "ff"
        dd = false
      """.trimIndent())
            // test conf output
          convertToConf(propertyLines, false) shouldBe getListFromString("""
        # comment 1
        aa = "ff"
        dd = false
      """.trimIndent())
        }

        "read in a single line comment not at top".config(enabled = ALL_TESTS_ENABLED) {
            val propertyLines = getListFromString("""
        aa="ff"
        // comment 2
        dd=false
      """)
            convertToProperties(propertyLines) shouldBe getListFromString("""
        aa = "ff"
        // comment 2
        dd = false
      """.trimIndent())
        }


        "read in a single line comment needs sorting".config(enabled = ALL_TESTS_ENABLED) {
            val propertyLines = getListFromString("""
        // comment 2
        dd=false
        aa="ff"
      """)
            convertToProperties(propertyLines) shouldBe getListFromString("""
        aa = "ff"
        // comment 2
        dd = false
      """.trimIndent())
        }


        "read in a multiline comment".config(enabled = ALL_TESTS_ENABLED) {
            val propertyLines = getListFromString("""
        aa="ff"
        // comment 2
        // comment 3
        // comment 4
        // comment 5
        dd=false
      """)
            convertToProperties(propertyLines) shouldBe getListFromString("""
        aa = "ff"
        // comment 2
        // comment 3
        // comment 4
        // comment 5
        dd = false
      """.trimIndent())
        }



        "read in a conf with comments".config(enabled = false) {
            val propertyLines = getListFromString("""
        aa="ff"
        // comment 2
        dd {
          sss="lkjlj"
          // comment 3
          conf = [
            "123"
          ]
        }
      """)
          convertToConf(propertyLines, false) shouldBe getListFromString("""
          aa = "ff"
          // comment 2
          dd {
            sss="lkjlj"
            // comment 3
            conf = [
              "123"
            ]
          }
      """.trimIndent())

            convertToProperties(propertyLines) shouldBe getListFromString("""
          aa = "ff"
          // comment 2
          dd.sss="lkjlj"
          // comment 3
          dd.conf = [
            "123"
          ]
      """.trimIndent())

        }


    }) {
    companion object {
        //            const val ALL_TESTS_ENABLED = false
        const val ALL_TESTS_ENABLED = true
    }
}

