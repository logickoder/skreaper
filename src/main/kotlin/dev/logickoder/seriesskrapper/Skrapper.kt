package dev.logickoder.seriesskrapper

interface Skrapper {
    suspend fun scrape(url: String): List<Season>
}