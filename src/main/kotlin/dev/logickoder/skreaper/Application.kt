package dev.logickoder.skreaper

import kotlinx.coroutines.runBlocking

/**
 * Entry point to the scrapper cmd application.
 */
fun main() = runBlocking {
    while (true) {
        print("Enter the link to the series you want to fetch: ")
        val link = readln()

        // Determine the appropriate scrapper based on the provided link.
        val scrapper = when {
            link.contains("netnaija", true) -> Netnaija
            link.contains("waploaded", true) -> Waploaded
            // Exit the loop if the link is blank.
            link.isBlank() -> break
            // Skip this loop if no suitable scrapper is found for the provided link.
            else -> {
                println("No suitable scrapper found for the provided link\n")
                continue
            }
        }

        print("Enter the season to start scrapping from (Optional): ")
        val start = readlnOrNull()?.toIntOrNull() ?: 1

        print("Enter the season to end scrapping at (Optional): ")
        val end = readlnOrNull()?.toIntOrNull() ?: Integer.MAX_VALUE

        // Scrape the series episodes using the selected scrapper within the specified range of seasons.
        scrapper.scrape(link, start..end).forEach { season ->
            // Print the season name.
            println(season.name)

            // Print the video link and subtitle for each episode in the season.
            season.episodes.forEach { episode ->
                println(episode.video)
                println(episode.subtitle)
            }

            println() // Add an empty line between seasons for better readability.
        }
    }
}
