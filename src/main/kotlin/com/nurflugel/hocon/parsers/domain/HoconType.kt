package com.nurflugel.hocon.parsers.domain

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
}

// couldn't I just extend Map?
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
  fun containsKey(key: String): Boolean {
    return innerMap.containsKey(key)
  }
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
  val value: String,
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = value
}

class HoconBlankLine(override var comments: List<String> = listOf()) : HoconType {
  override fun toName(): String = ""
}

class HoconInclude(
  val value: String,
  override var comments: List<String> = listOf()
) : HoconType {
  override fun toName(): String = value
}

class HoconUnknown(override var comments: List<String> = listOf()) : HoconType {
  override fun toName(): String = ""
}

