package com.nurflugel.hocon

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
class ConverterSpec : StringSpec(
    {

        "properties format simple keys" {
            val lines = arrayListOf("""one="kkkk""", "two.three.four = 5")
            val confLines = FileUtil.convertPropertiesToConf(lines)


            val expectedLines = arrayListOf(
                """one = "kkkk""",
                "two {",
                "  three {",
                "    four = 5",
                "  }",
                "}"
            )
            confLines shouldBe expectedLines
        }

        "conf format simple keys" {
            val lines = arrayListOf("""one = "kkkk""", "two.three.four = 5")
            val propertyLines = FileUtil.convertConfToProperties(lines)
            propertyLines shouldBe lines
        }

//                                     * aaaa {
//                                     *    bbbb = 5
//                                     *    cccc = "text"
//                                     *    dddd = true
//                                     * }
//                                     *

        "conf format with map of keys"{
            val lines = arrayListOf("aaaa {", "   bbbb = 5", "   cccc = \"text\"", "   dddd = true", "}")
            val propertyLines = FileUtil.convertConfToProperties(lines)
            val expectedLines = arrayListOf("aaaa.bbbb = 5", "aaaa.cccc = \"text\"", "aaaa.dddd = true")
            propertyLines shouldBe expectedLines
        }

        "single mapped key should be as property"{
            val lines = arrayListOf("aaaa {", "   bbbb{", "   cccc {", "       dddd = true", "}", "}", "}")
            val propertyLines = FileUtil.convertConfToProperties(lines)
            val confLines = FileUtil.convertPropertiesToConf(propertyLines)
            val expectedLines = arrayListOf("aaaa.bbb.cccc.dddd = true")
            confLines shouldBe expectedLines
        }
        
    })