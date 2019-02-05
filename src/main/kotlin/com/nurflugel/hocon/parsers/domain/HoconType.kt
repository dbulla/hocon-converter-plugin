package com.nurflugel.hocon.parsers.domain

interface HoconType {
    var comments: List<String>
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
) : HoconType

class HoconMap(
    val key: String,
    val map: Map<String, HoconType>,
    override var comments: List<String> = listOf()
) : HoconType


/** a key value pair.  The value can be another map, a property, list, etc */
class HoconPair(
    val key: String,
    val value: HoconType,
    override var comments: List<String> = listOf()
) : HoconType //{
//  constructor(key: String, value: String) : this(key, HoconValue(value))
//}


class HoconValue(
    val value: String,
    override var comments: List<String> = listOf()
) : HoconType

class HoconBlankLine(override var comments: List<String> = listOf()) : HoconType

class HoconInclude(
    val value: String,
    override var comments: List<String> = listOf()
) : HoconType

class HoconUnknown(override var comments: List<String> = listOf()) : HoconType

