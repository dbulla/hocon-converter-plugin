package com.nurflugel.hocon.parsers.domain


/** a key value pair.  The value can be another map, a property, list, etc */
class HoconPair(val key: String,
                val value: HoconType,
                val comments: List<String> = listOf()) : HoconType {
  constructor(key: String, value: String) : this(key, HoconValue(value))
}
