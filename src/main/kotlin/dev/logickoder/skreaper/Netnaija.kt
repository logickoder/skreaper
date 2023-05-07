package dev.logickoder.skreaper

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlAnchor
import com.gargoylesoftware.htmlunit.html.HtmlHeading1
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlSpan
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class Netnaija : Skrapper {

    override suspend fun scrape(url: String, range: IntRange): List<Season> {
        val webClient = WebClient().apply {
            options.isThrowExceptionOnScriptError = false
            options.isJavaScriptEnabled = false
            options.isDownloadImages = false
            options.isCssEnabled = false
        }

        val page: HtmlPage = webClient.getPage(url)

        val seriesName = page.querySelector<HtmlHeading1>("h1.page-h1").textContent

        println("Extracting $seriesName seasons...")

        val seasons = page.querySelectorAll("div.video-seasons a").map {
            it as HtmlAnchor
        }.let {
            println("Found ${it.size} seasons, extracting them")
            it
        }.filter { node ->
            node.textContent.seasonFromString() in range
        }.map { node ->
            val seasonName = node.textContent
            val seasonUrl = node.hrefAttribute
            println("Extracting episodes from $seasonName")

            Season(
                name = seasonName,
                link = seasonUrl,
                episodes = webClient.episodesFromSeason(seasonUrl)
            )
        }

        webClient.close()

        return seasons
    }

    private suspend fun WebClient.episodesFromSeason(
        seasonUrl: String,
    ): List<Episode> {
        // retrieve all episodes from the season page
        return getPage<HtmlPage>(seasonUrl).querySelectorAll("div.video-files div.info").map { node ->
            val episodeLink = node.querySelector<HtmlAnchor>("a.anchor").hrefAttribute
            // retrieve the video and subtitle buttons from the episode page
            val downloadButtons = getPage<HtmlPage>(episodeLink).querySelectorAll("div.download-block a.btn").map {
                it as HtmlAnchor
            }
            Episode(
                name = node.querySelector<HtmlSpan>("span.title").textContent,
                link = episodeLink,
                videoLink = downloadButtons.first().getResourceLink(),
                subtitleLink = downloadButtons.last().getResourceLink(),
            )
        }
    }

    /**
     * Retrieve the resource download url from the given button
     */
    private suspend fun HtmlAnchor.getResourceLink(): String {
        // get file id
        val id = click<HtmlPage>()
            .baseURI
            .split("/")
            .last()
            .split("-")
            .first()
        val response = HttpClient(CIO)
            .get("https://api.sabishare.com/token/download/$id")
            .bodyAsText()
        val result = Json.decodeFromString<JsonObject>(response)
        return result["data"]?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
    }
}