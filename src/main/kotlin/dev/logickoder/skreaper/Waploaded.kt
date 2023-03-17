package dev.logickoder.skreaper

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlAnchor
import com.gargoylesoftware.htmlunit.html.HtmlHeading1
import com.gargoylesoftware.htmlunit.html.HtmlPage

class Waploaded : Skrapper {

    private val baseUrl = "https://series.waploaded.com/"

    override suspend fun scrape(url: String): List<Season> {
        val webClient = WebClient().apply {
            options.isThrowExceptionOnScriptError = false
            options.isJavaScriptEnabled = false
            options.isDownloadImages = false
            options.isCssEnabled = false
        }

        val page: HtmlPage = webClient.getPage(url)

        val seriesName = page.querySelector<HtmlHeading1>("h1.post_title").textContent

        println("Extracting $seriesName seasons...")

        val seasons = page.retrieveLinks().let {
            println("Found ${it.size} seasons, extracting them")
            it
        }.map { node ->
            val seasonName = node.title()
            val seasonUrl = baseUrl + node.hrefAttribute
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

    private fun WebClient.episodesFromSeason(
        seasonUrl: String,
    ): List<Episode> {
        // retrieve all episodes from the season page
        return getPage<HtmlPage>(seasonUrl).retrieveLinks().map { node ->
            val episodeLink = baseUrl + node.hrefAttribute

            val episodeDownloadLink = getPage<HtmlPage>(episodeLink)
                .querySelectorAll("div.main_content div.file_attachment_wrapper a.button")[1] as HtmlAnchor

            Episode(
                name = node.title(),
                link = episodeLink,
                videoLink = episodeDownloadLink.click<HtmlPage>().querySelector<HtmlAnchor>("a.dl_link").let { anchor ->
                    val splits = anchor.getAttribute("onClick").split("=")
                    val link = if (splits.size > 1) splits[1] else splits[0]
                    link.replace("'", "")
                },
                subtitleLink = "",
            )
        }
    }

    private fun HtmlPage.retrieveLinks() = querySelectorAll("div.main_content div.post_list a").map {
        it as HtmlAnchor
    }

    private fun HtmlAnchor.title() = getAttribute("title")
}