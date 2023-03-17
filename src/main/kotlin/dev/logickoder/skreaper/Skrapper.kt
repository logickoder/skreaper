package dev.logickoder.skreaper

interface Skrapper {
    suspend fun scrape(url: String): List<Season>
}