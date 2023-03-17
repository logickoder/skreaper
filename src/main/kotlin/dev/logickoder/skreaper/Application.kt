package dev.logickoder.skreaper

suspend fun main() {
    val link = readln()
    val scrapper = when {
        link.contains("netnaija", true) -> Netnaija()
        link.contains("waploaded", true) -> Waploaded()
        else -> null
    }
    println(scrapper?.scrape("https://series.waploaded.com/series/441686/shadow-and-bone"))
}