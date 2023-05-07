package dev.logickoder.skreaper

suspend fun main() {
    while (true) {
        print("Enter the link to the series you want to fetch: ")
        val link = readln()
        val scrapper = when {
            link.contains("netnaija", true) -> Netnaija()
            link.contains("waploaded", true) -> Waploaded()
            link.isBlank() -> break
            else -> null
        }
        print("Enter the season to start scrapping from (Optional): ")
        val start = readlnOrNull()?.toIntOrNull() ?: 1
        print("Enter the season to end scrapping at (Optional): ")
        val end = readlnOrNull()?.toIntOrNull() ?: Integer.MAX_VALUE
        scrapper?.scrape(link, start..end)?.forEach { season ->
            println(season.name)
            season.episodes.forEach { episode ->
                println(episode.videoLink)
                println(episode.subtitleLink)
            }
            println()
        }
    }
}