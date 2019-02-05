package com.nurflugel.hocon.parsers.domain

class HoconMap(val key: String, val map: Map<String, HoconType>) : HoconType {
}