package com.nurflugel.hocon.parsers.domain

import com.nurflugel.hocon.generators.ConfGenerator.writeText

/** A HoconType can be a map, list, value, etc - it can have comments attached to it regardless
 * of the sorting used or how the data is formatted for output */
interface HoconType {
  var comments: List<String>
  // using toName instead of toString, as I might want
  fun toName(): String
}

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
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = key

  /** output the list as a \n delimited String */
  override fun toString(): String {
    val sb = StringBuilder("[\n")

    val joinToString = values.joinToString(",\n") { writeText(it) }
    sb.append(joinToString)

    sb.append("\n]")
    return sb.toString()
  }
}

// couldn't I just extend Map with a generic type?
class HoconMap(
  val key: String,
  private val innerMap: MutableMap<String, HoconType> = mutableMapOf(),
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = key
  fun getKeys() = innerMap.keys
  fun get(key: String) = innerMap[key]
  fun set(key: String, value: HoconType) {
    innerMap[key] = value
  }

  fun getValues(): Set<HoconType> = innerMap.values.toSet()
  fun containsKey(key: String): Boolean = innerMap.containsKey(key)
  fun entries() = innerMap.entries
}


/** a key value pair.  The value can be another map, a property, list, etc */
class HoconPair(
  val key: String,
  val value: HoconType,
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = key
}

class HoconString(
  private val value: String,
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = value
  override fun toString(): String = value
}

class HoconBlankLine(override var comments: List<String> = listOf()) : HoconType {
  override fun toName(): String = ""
}

class HoconInclude(
  private val value: String,
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = value
}

class HoconUnknown(override var comments: List<String> = listOf()) : HoconType {
  override fun toName(): String = ""
}

/** A special class - represents nothing.  But, could still have comments :) */
class HoconVoid(override var comments: List<String> = listOf()) : HoconType {
  override fun toName(): String = ""
}

