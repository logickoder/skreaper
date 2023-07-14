package dev.logickoder.skreaper

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.htmlunit.html.HtmlAnchor
import org.htmlunit.html.HtmlHeading1
import org.htmlunit.html.HtmlPage
import org.htmlunit.html.HtmlSpan

object Netnaija : Scrapper() {

    override fun HtmlPage.getSeriesName(): String = querySelector<HtmlHeading1>("h1.page-h1").textContent

    override fun HtmlPage.getSeasonLinks(): List<HtmlAnchor> = querySelectorAll("div.video-seasons a").map {
        it as HtmlAnchor
    }

    override fun List<HtmlAnchor>.getSeasonNameAndUrl(): List<Season> = map { node ->
        Season(
            name = node.textContent,
            link = node.hrefAttribute,
        )
    }

    override suspend fun getEpisodesFromSeason(seasonUrl: String): List<Episode> {
        // retrieve all episodes from the season page
        return webClient.getPage<HtmlPage>(seasonUrl).querySelectorAll("div.video-files div.info").map { node ->
            val episodeLink = node.querySelector<HtmlAnchor>("a.anchor").hrefAttribute
            // retrieve the video and subtitle buttons from the episode page
            val downloadButtons = webClient.getPage<HtmlPage>(episodeLink).querySelectorAll(
                "div.download-block a.btn"
            ).map { it as HtmlAnchor }
            Episode(
                name = node.querySelector<HtmlSpan>("span.title").textContent,
                link = episodeLink,
                video = downloadButtons.first().getResourceLink(),
                subtitle = downloadButtons.last().getResourceLink(),
            )
        }
    }

    /**
     * Retrieve the resource download url from the given button
     */
    private suspend fun HtmlAnchor.getResourceLink(): String {
        // go to the resource page
        val resourcePage = click<HtmlPage>()
        // get the resource id from the given page
        val resourceId = resourcePage
            .baseURI
            .split("/")
            .last()
            .split("-")
            .first()
        // make a GET request to retrieve the url from the resource id
        val response = HttpClient(CIO)
            .get("https://api.sabishare.com/token/download/$resourceId")
            .bodyAsText()

        val result = Json.decodeFromString<JsonObject>(response)

        // extract the url from the result
        return result["data"]?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
    }
}