package dev.logickoder.seriesskrapper.netnaija

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlAnchor
import com.gargoylesoftware.htmlunit.html.HtmlDivision
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlSpan
import dev.logickoder.seriesskrapper.Episode
import dev.logickoder.seriesskrapper.Season
import dev.logickoder.seriesskrapper.Skrapper
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

    override suspend fun scrape(url: String): List<Season> {
        val webClient = WebClient().apply {
            options.isThrowExceptionOnScriptError = false
            options.isJavaScriptEnabled = false
            options.isDownloadImages
        }

        val page: HtmlPage = webClient.getPage(url)

        val seasons = page.querySelectorAll("div.video-seasons a").map {
            it as HtmlAnchor
        }.map { node ->
            val season = Season(
                name = node.textContent,
                link = node.hrefAttribute,
            )
            season.copy(episodes = webClient.episodesFromSeason(season))
        }

        webClient.close()

        return seasons
    }

    private suspend fun WebClient.episodesFromSeason(
        season: Season,
    ): List<Episode> {
        val page: HtmlPage = getPage(season.link)
        val episodesContainer: HtmlDivision = page.querySelector("div.video-files")
        val episodesInfo = episodesContainer.querySelectorAll("div.info")

        println("Extracting ${season.name} Episodes...")
        return episodesInfo.map { node ->
            retrieveVideoAndSubtitleLinks(
                Episode(
                    name = node.querySelector<HtmlSpan>("span.title").textContent,
                    link = node.querySelector<HtmlAnchor>("a.anchor").hrefAttribute,
                )
            )
        }
    }

    private suspend fun WebClient.retrieveVideoAndSubtitleLinks(
        episode: Episode,
    ): Episode {
        val page: HtmlPage = getPage(episode.link)
        val buttons = page.querySelectorAll("div.download-block a.btn").map {
            it as HtmlAnchor
        }
        return episode.copy(
            videoLink = buttons.first().click<HtmlPage>().getResourceLink(),
            subtitleLink = buttons.last().click<HtmlPage>().getResourceLink(),
        )
    }

    private suspend fun HtmlPage.getResourceLink(): String {
        // get file id
        val id = baseURI.split("/").last().split("-").first()
        val response = HttpClient(CIO).get("https://api.sabishare.com/token/download/$id").bodyAsText()
        val result = Json.decodeFromString<JsonObject>(response)
        return result["data"]?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
    }
}