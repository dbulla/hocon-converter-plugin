package com.nurflugel.hocon

import com.nurflugel.hocon.Utils.getListFromString
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.convertPropertiesToConf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ListsSpec : StringSpec(
  {

      "read in a single line list".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        aa="ff"
        cors = ["123","456","789" ]
        dd=false
      """)
          convertPropertiesToConf(propertyLines) shouldBe getListFromString(
              """
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
        convertPropertiesToConf(propertyLines) shouldBe getListFromString(
            """
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
        convertPropertiesToConf(propertyLines) shouldBe getListFromString(
            """
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
        convertPropertiesToConf(propertyLines) shouldBe getListFromString(
            """
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
        convertPropertiesToConf(propertyLines) shouldBe getListFromString(
            """
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

