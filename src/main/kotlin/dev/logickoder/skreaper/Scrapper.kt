package dev.logickoder.skreaper

import org.htmlunit.WebClient
import org.htmlunit.html.HtmlAnchor
import org.htmlunit.html.HtmlPage

/**
 * Abstract class representing a web scraper for extracting TV show information and episodes from a website.
 * Extend this class to implement specific scraping logic for different websites.
 */
abstract class Scrapper {
    /**
     * The WebClient used for making HTTP requests and handling web pages.
     * It is configured with specific options to disable JavaScript, images, and CSS for efficient scraping.
     */
    open val webClient = WebClient().apply {
        options.isThrowExceptionOnScriptError = false
        options.isJavaScriptEnabled = false
        options.isDownloadImages = false
        options.isCssEnabled = false
    }

    /**
     * Scrapes the given URL and extracts the list of seasons for a TV show within the specified range.
     *
     * @param url The URL of the TV show's webpage.
     * @param range The range of seasons to extract (e.g., 1..5).
     * @return A list of [Season] objects containing information about each season and its episodes.
     */
    suspend fun scrape(url: String, range: IntRange): List<Season> {
        val page: HtmlPage = webClient.getPage(url)

        val seriesName = page.getSeriesName()

        println("Extracting $seriesName seasons...")

        val seasonLinks = page.getSeasonLinks()
        val seasonLinksToExtract = seasonLinks.filter { node ->
            node.textContent.seasonFromString() in range
        }

        println("Found ${seasonLinks.size} seasons, extracting ${seasonLinksToExtract.size} of them")

        val seasons = seasonLinksToExtract.getSeasonNameAndUrl().map { season ->
            println("Extracting episodes from ${season.name}")

            season.copy(
                episodes = getEpisodesFromSeason(season.link)
            )
        }

        webClient.close()

        return seasons
    }

    /**
     * Extracts the name of the TV show from the HTML page.
     *
     * @return The name of the TV show.
     */
    abstract fun HtmlPage.getSeriesName(): String

    /**
     * Extracts the list of season links from the HTML page.
     *
     * @return A list of [HtmlAnchor] elements representing the season links.
     */
    abstract fun HtmlPage.getSeasonLinks(): List<HtmlAnchor>

    /**
     * Extracts the list of seasons with their names and URLs from a list of [HtmlAnchor] elements.
     *
     * @return A list of [Season] objects containing the name and URL of each season.
     */
    abstract fun List<HtmlAnchor>.getSeasonNameAndUrl(): List<Season>

    /**
     * Retrieves the list of episodes for a specific season given its URL.
     *
     * @param seasonUrl The URL of the season's webpage.
     * @return A list of [Episode] objects containing information about each episode.
     */
    abstract suspend fun getEpisodesFromSeason(seasonUrl: String): List<Episode>

    /**
     * Helper function to extract the season number from a string representation (e.g., "Season 1").
     *
     * @return The season number as an integer, or 1 if no valid number is found.
     */
    private fun String.seasonFromString(): Int = split(Regex("\\s+")).last().toIntOrNull() ?: 1
}
