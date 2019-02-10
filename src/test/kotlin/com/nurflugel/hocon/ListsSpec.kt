package com.nurflugel.hocon

import com.nurflugel.hocon.ConfToPropertiesSpec.Companion.convertToProperties
import com.nurflugel.hocon.Utils.getListFromString
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ListsSpec : StringSpec(
  {

      "read in a single line list".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString("""
        aa="ff"
        cors = ["dfd","sss","ddd" ]
        dd=false
      """)
        convertToProperties(propertyLines) shouldBe getListFromString(
              """
        aa = "ff"
        cors = [
          "dfd",
          "sss",
          "ddd"
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
      convertToProperties(propertyLines) shouldBe getListFromString(
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
      convertToProperties(propertyLines) shouldBe getListFromString(
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
      convertToProperties(propertyLines) shouldBe getListFromString(
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
      convertToProperties(propertyLines) shouldBe getListFromString(
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
// todo repeat with other output
  }) {
  companion object {
    //    const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true
  }
}

