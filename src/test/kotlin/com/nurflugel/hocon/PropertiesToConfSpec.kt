package com.nurflugel.hocon

import com.nurflugel.hocon.Utils.getListFromString
import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.convertConfToProperties
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.convertPropertiesToConf
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

            val confLines = convertPropertiesToConf(lines)

            confLines shouldBe getListFromString(
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
            val confLines = convertPropertiesToConf(lines)

            confLines shouldBe getListFromString(
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
            val confLines = convertPropertiesToConf(lines)

            confLines shouldBe getListFromString(
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
            val confLines = convertPropertiesToConf(lines)

            confLines shouldBe getListFromString(
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

        "read in a map".config(enabled = true) {
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
             bbbb{
                cccc {
                   dddd = true
                }
             }
          }
          """
            )
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
           },
        }
        """
            )
            val propertyLines = convertConfToProperties(lines)
            val confLines = convertPropertiesToConf(propertyLines)
            confLines shouldBe getListFromString("aaaa.bbb.cccc.dddd = true")
        }

    }) {
    companion object {
        //        const val ALL_TESTS_ENABLED = false
    const val ALL_TESTS_ENABLED = true
    }
}

