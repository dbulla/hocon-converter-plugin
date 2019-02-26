package com.nurflugel.hocon

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

/** this class is for real examples which crop up in testing.  */
class Examples1Spec : StringSpec(
    {


        "realExample1".config(enabled = ALL_TESTS_ENABLED) {
            val lines = Utils.getListFromString(
                """
            das {
              aws {
                s3 {
                  excel {
                    accesskey = "aaaa"
                    bucket {
                      name = "dddd-preprod-ssss"
                    }
                    secretkey = "asdf"
                    toplevel {
                      key = "qa"
                    }
                  }
                  ipad {
                    accesskey = "dddddd"
                    secretkey = "adsfasdfasdfasdf"
                  }
                }
              }
              env = "qa"
            }

            database {
              atlas {
                jdbcUrl = "jdbc:oracle:thin:@dfd-atl-sssss.dfdfss.net:1521:LKJLKJ"
                password = "appl2QAd"
                url = "jdbc:oracle:thin:@sdfd-atl-sssss.dfdfss.net:1521:LKJLKJ"
                username = "user"
              }
              das {
                excel {
                  upload {
                    password = "aaaaaaaa"
                    url = "jdbc:postgresql://dfdf-ss.cluster-sdfsdf.com:5432/mine"
                    user = "user2"
                  }
                }
                mongodb {
                  eeeeFPassword = "ffff"
                  eeeeFUser = "eeeeFUser"
                  eeeeOCPassword = "ddddd!"
                  eeeeOCUser = "eeeeOCUser"
                  eeeeQRWPassword = "ggggg"
                  eeeeQRWUser = "eeeeQUser"
                  eeeeRWPassword = "ssssss"
                  eeeeRWUser = "eeeeUser"
                  host = "localhost"
                  isReplica = false
                }
                password = "aassdfadsfasdfasdf"
                url = "jdbc:oracle:thin:@aaaaa.bbbb.net:1521:DDDDD"
                username = "passs"
              }
            }
        """
            )
          val propertyLines = ConfToPropertiesSpec.convertToProperties(lines, false)
            propertyLines shouldBe Utils.getListFromString(
                """
                das.aws.s3.excel.accesskey = "aaaa"
                das.aws.s3.excel.bucket.name = "dddd-preprod-ssss"
                das.aws.s3.excel.secretkey = "asdf"
                das.aws.s3.excel.toplevel.key = "qa"
                das.aws.s3.ipad.accesskey = "dddddd"
                das.aws.s3.ipad.secretkey = "adsfasdfasdfasdf"
                das.env = "qa"
                database.atlas.jdbcUrl = "jdbc:oracle:thin:@dfd-atl-sssss.dfdfss.net:1521:LKJLKJ"
                database.atlas.password = "appl2QAd"
                database.atlas.url = "jdbc:oracle:thin:@sdfd-atl-sssss.dfdfss.net:1521:LKJLKJ"
                database.atlas.username = "user"
                database.das.excel.upload.password = "aaaaaaaa"
                database.das.excel.upload.url = "jdbc:postgresql://dfdf-ss.cluster-sdfsdf.com:5432/mine"
                database.das.excel.upload.user = "user2"
                database.das.mongodb.eeeeFPassword = "ffff"
                database.das.mongodb.eeeeFUser = "eeeeFUser"
                database.das.mongodb.eeeeOCPassword = "ddddd!"
                database.das.mongodb.eeeeOCUser = "eeeeOCUser"
                database.das.mongodb.eeeeQRWPassword = "ggggg"
                database.das.mongodb.eeeeQRWUser = "eeeeQUser"
                database.das.mongodb.eeeeRWPassword = "ssssss"
                database.das.mongodb.eeeeRWUser = "eeeeUser"
                database.das.mongodb.host = "localhost"
                database.das.mongodb.isReplica = false
                database.das.password = "aassdfadsfasdfasdf"
                database.das.url = "jdbc:oracle:thin:@aaaaa.bbbb.net:1521:DDDDD"
                database.das.username = "passs"
                """
            )
        }

        "realExample2 chained keys in key".config(enabled = ALL_TESTS_ENABLED) {
            val lines = Utils.getListFromString(
                """
            das.aws {
              key = "qa"
            }
        """
            )
          val propertyLines = ConfToPropertiesSpec.convertToProperties(lines, false)
            propertyLines shouldBe Utils.getListFromString(
                """
                das.aws.key = "qa"
                """
            )
        }

        "realExample2 chained keys in value".config(enabled = ALL_TESTS_ENABLED) {
            val lines = Utils.getListFromString(
                """
            das {
              toplevel.key = "qa"
            }
        """
            )
          val propertyLines = ConfToPropertiesSpec.convertToProperties(lines, false)
            propertyLines shouldBe Utils.getListFromString(
                """
                das.toplevel.key = "qa"
                """
            )
        }

        "realExample3 chained keys in value".config(enabled = ALL_TESTS_ENABLED) {
            val lines = Utils.getListFromString(
                """
            das.dos {
              toplevel.key = "qa"
              steven=true
            }
        """
            )
          val propertyLines = ConfToPropertiesSpec.convertToProperties(lines, false)
            propertyLines shouldBe Utils.getListFromString(
                """
                das.dos.steven = true
                das.dos.toplevel.key = "qa"
                """
            )
        }
    }

) {
    companion object {
        const val ALL_TESTS_ENABLED = true
//        const val ALL_TESTS_ENABLED=false
    }
}
