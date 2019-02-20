package com.nurflugel.hocon.parsers.domain

import java.util.*

class PropertiesMap {
  val map = HoconMap("toplevel")
  val includesList = mutableListOf<String>()

  public fun addInclude(include: String) {
    includesList.add(include)
  }

  fun addList(key: String, list: HoconList) {
    map.set(key, list)
  }

  fun addList(keyStack: Stack<String>, key: String, list: HoconList) {
    // parse through the map to get the submap for this key, then add to that submap
    val mapToGet = keyStack.joinToString(separator = ".")
    val mapFromPath = getMapFromPath(mapToGet, map)
    mapFromPath.set(key, list)
  }

  private fun getMapFromPath(mapToGet: String, submap: HoconMap): HoconMap {
    val key = mapToGet.substringBefore(".")

    // do we need to drill down further?
    if (mapToGet.contains(".")) {
      val remainder = mapToGet.substringAfter(".")
      val value = submap.get(key)

      return when (value) {
        is HoconMap -> {
          when {
            remainder.isBlank() -> submap
            else -> getMapFromPath(remainder, value)
          }
        }
        else -> submap.get(key) as HoconMap
      }
    } else {// we're done recursing
      return submap.get(key) as HoconMap
    }
  }
}