package dev.logickoder.skreaper

fun String.seasonFromString(): Int = split(Regex("\\s+")).last().toIntOrNull() ?: 1