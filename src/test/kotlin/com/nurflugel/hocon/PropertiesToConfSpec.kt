package com.nurflugel.hocon

import com.nurflugel.hocon.ConfToPropertiesSpec.Companion.convertToProperties
import com.nurflugel.hocon.Utils.getListFromString
import com.nurflugel.hocon.generators.ConfGenerator
import com.nurflugel.hocon.parsers.HoconParser
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class PropertiesToConfSpec : StringSpec(
  {

    "properties format simple keys map 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        bbb.three = 5
        """
      )

      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          bbb {
            three = 5
          }
          """
      )
    }

    "properties format simple keys map 2".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        bbb.three = 5
        ccc.five = 6
        """
      )

      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          bbb {
            three = 5
          }

          ccc {
            five = 6
          }
          """
      )
    }

    "properties format simple keys map 3".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        aaa.zzz=5
        bbb.three = 5
        bbb.four.five=6
        bbb.four.siz=6
        ccc.five = 6
        ddd.eee="wood"
        """
      )

      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
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
          """
      )
    }

    "properties format simple keys".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        one="kkkk"
        two.three.four = 5
        """
      )

      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          one = "kkkk"
          two {
            three {
              four = 5
            }
          }
          """
      )
    }

    "don't lose the includes in map formatter".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        include "reference2.conf"
        include "reference1.conf"

        aaa.bb.ee="ff"
        aa.bb.cc.dd="f"
        """
      )
      val outputLines = convertPropertiesToConf(lines)
      // ensure order is preserved, as well as the includes just being there
      outputLines[0] shouldBe """include "reference2.conf""""
      outputLines[1] shouldBe """include "reference1.conf""""
      outputLines[2] shouldBe ""
    }


    "single mapped key should be as property".config(enabled = false) {
      //todo enable feature
      val lines = getListFromString(
        """
        aaaa {
           bbbb{
              cccc {
                 dddd = true
              }
           }
        }
        """
      )
      val propertyLines = convertToProperties(lines)
      convertPropertiesToConf(propertyLines) shouldBe getListFromString("aaaa.bbb.cccc.dddd = true")
    }


    "nested key should be as property".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
        aaaa {
           bbbb{
              cccc.dddd = true
           }
        }
        """
      )
      val propertyLines = convertToProperties(lines)
      convertPropertiesToConf(propertyLines) shouldBe getListFromString("""
        aaaa {
          bbbb {
            cccc {
              dddd = true
            }
          }
        }
        """
      )
    }


    /////////////////////////////////////////////////////////////////////////
    "read in a map level 1".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
          aaaa {
             bbbb = true
          }
          """
      )
      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          aaaa {
            bbbb = true
          }
          """
      )
    }

    /////////////////////////////////////////////////////////////////////////
    "read in a map level 2".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
          aaaa {
                                 bbbb{
   cccc  = true
             }
          }
          """
      )
      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          aaaa {
            bbbb {
              cccc = true
            }
          }
          """
      )
    }

    "read in a map level 3".config(enabled = ALL_TESTS_ENABLED) {
      val lines = getListFromString(
        """
          aaaa {
             bbbb{
                cccc {
                   dddd = true
                }
             }
          }
          """
      )
      convertPropertiesToConf(lines) shouldBe getListFromString(
        """
          aaaa {
            bbbb {
              cccc {
                dddd = true
              }
            }
          }
          """
      )
    }

/*

server {

  # enable response compression
  compression {
    enabled = true
    mime-types = [
      "application/json",
      "application/xml",
      "text/html",
      "text/xml",
      "text/plain",
    ]
  }
}
 should be like:
server.compression.enabled=true
server.compression.mime-types = [
      "application/json",
      "application/xml",
      "text/html",
      "text/xml",
      "text/plain",
    ]
 or like:
server {
  compression {
    enabled = true
    mime-types = [
      "application/json",
      "application/xml",
      "text/html",
      "text/xml",
      "text/plain",
    ]
  }
}


*/
  }) {
  companion object {
    //      const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true

    /**
     * this assumes the lines being parsed are pure property lines - just
     * stuff like aaa.bbb.ccc.dd=true, no maps
     */
    fun convertPropertiesToConf(existingLines: List<String>): MutableList<String> {
      val propsMap: PropertiesMap = HoconParser.populatePropsMap(existingLines)
      return ConfGenerator.generateConfOutput(propsMap)
    }


  }
}

