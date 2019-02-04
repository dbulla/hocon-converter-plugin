package com.nurflugel.hocon.parsers.domain

/** Representation of a HOCON list.  These are all valid versions of a list:
 *
 * cors ["123", "234","456"]
 *
 * cors ["123",
 * "234","456"]
 *
 * cors [
 * "123",
 * "234","456"]
 *
 * cors [
 * "123",
 * "234","456"]
 *
 * cors [
 * "123",
 * "234",
 * "456"
 * ]
 */
data class HoconList(
  val key: String,
  val values: List<String>,
  val comments: List<String>
)
