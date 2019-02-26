package com.nurflugel.hocon

import com.nurflugel.hocon.ConfToPropertiesSpec.Companion.convertToProperties
import com.nurflugel.hocon.PropertiesToConfSpec.Companion.convertToConf
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
        convertToProperties(propertyLines, false) shouldBe getListFromString(
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
      convertToProperties(propertyLines, false) shouldBe getListFromString(
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
      convertToProperties(propertyLines, false) shouldBe getListFromString(
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
      convertToProperties(propertyLines, false) shouldBe getListFromString(
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
      convertToProperties(propertyLines, false) shouldBe getListFromString(
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



    "top-level lists at bottom properties".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString(
        """
abc = 678
cors = [
  "123",
  "456",
  "789"
]
def = 999
      """
      )
      convertToProperties(propertyLines, true) shouldBe getListFromString(
        """
abc = 678
def = 999
cors = [
  "123",
  "456",
  "789"
]
      """.trimIndent()
      )
    }

    "top-level lists at bottom conf".config(enabled = ALL_TESTS_ENABLED) {
      val propertyLines = getListFromString(
        """
abc = 678
cors = [
  "123",
  "456",
  "789"
]
def = 999
      """
      )
      convertToConf(propertyLines, flattenKeys = false, putTopLevelListsAtBottom = true) shouldBe getListFromString(
        """
abc = 678
def = 999
cors = [
  "123",
  "456",
  "789"
]
      """.trimIndent()
      )
    }

    "top-level lists at bottom but leave intermediate lists in place props".config(enabled = false) {
      val propertyLines = getListFromString(
        """
abc {
   ddd = [
      1,
      2
   ]
}
cors = [
  "123",
  "456",
  "789"
]
def = 999
      """
      )
      convertToProperties(propertyLines, false) shouldBe getListFromString(
        """
abc.ddd = [
      1,
      2
]
def = 999
cors = [
  "123",
  "456",
  "789"
]
      """.trimIndent()
      )
    }

    "top-level lists at bottom but leave intermediate lists in place conf ".config(enabled = false) {
      val propertyLines = getListFromString(
        """
abc {
   ddd = [
      1,
      2
   ]
}
cors = [
  "123",
  "456",
  "789"
]
def = 999
      """
      )
      convertToConf(propertyLines, flattenKeys = false, putTopLevelListsAtBottom = true) shouldBe getListFromString(
        """
abc {
ddd = [
  1,
  2
]
}
def = 999
cors = [
"123",
"456",
"789"
]
  """.trimIndent()
      )


    }

    /*


mime-types = [
  "application/json",
  "application/xml",
  "text/html",
  "text/xml",
  "text/plain"
]
compression.enabled = true


      # enable response compression
  compression.enabled = true
    mime-types = [
      "application/json",
      "application/xml",
      "text/html",
      "text/xml",
      "text/plain",
    ]











     */


// todo repeat with other output
  }) {
  companion object {
    //        const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true
  }
}

