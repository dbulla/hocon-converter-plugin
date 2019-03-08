package com.nurflugel.hocon.generators

import com.nurflugel.hocon.MiscSpec
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ConfGeneratorSpec : StringSpec(
    {

        "wrap text in quotes".config(enabled = MiscSpec.ALL_TESTS_ENABLED) {
            ConfGenerator.writeValueMaybeQuotes("dibble") shouldBe "\"dibble\""
            ConfGenerator.writeValueMaybeQuotes("\"dibble\"") shouldBe "\"dibble\""
        }

        "is a number".config(enabled = MiscSpec.ALL_TESTS_ENABLED) {
            ConfGenerator.writeValueMaybeQuotes("100") shouldBe "100"
        }

        "is a boolean".config(enabled = MiscSpec.ALL_TESTS_ENABLED) {
            ConfGenerator.writeValueMaybeQuotes("true") shouldBe "true"
            ConfGenerator.writeValueMaybeQuotes("false") shouldBe "false"
        }

        "is a secret".config(enabled = MiscSpec.ALL_TESTS_ENABLED) {
            ConfGenerator.writeValueMaybeQuotes("\${xxxx_xxxxx}") shouldBe "\${xxxx_xxxxx}"
            ConfGenerator.writeValueMaybeQuotes("\${xxxx_xxxxx") shouldBe "\"\${xxxx_xxxxx\""
            ConfGenerator.writeValueMaybeQuotes("\$xxxx_xxxxx}") shouldBe "\"\$xxxx_xxxxx}\""
        }

        "is a time period".config(enabled = MiscSpec.ALL_TESTS_ENABLED) {
            ConfGenerator.writeValueMaybeQuotes("120 milliseconds") shouldBe "120 milliseconds"
            ConfGenerator.writeValueMaybeQuotes("120 seconds") shouldBe "120 seconds"
            ConfGenerator.writeValueMaybeQuotes("120 minutes") shouldBe "120 minutes"
            ConfGenerator.writeValueMaybeQuotes("120 hours") shouldBe "120 hours"
            ConfGenerator.writeValueMaybeQuotes("120 days") shouldBe "120 days"
            ConfGenerator.writeValueMaybeQuotes("120 years") shouldBe "120 years"
        }


    })

