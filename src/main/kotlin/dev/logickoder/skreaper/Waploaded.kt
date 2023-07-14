package dev.logickoder.skreaper

import org.htmlunit.html.HtmlAnchor
import org.htmlunit.html.HtmlHeading1
import org.htmlunit.html.HtmlPage

object Waploaded : Scrapper() {

    private val baseUrl = "https://series.waploaded.com/"

    override fun HtmlPage.getSeriesName(): String = querySelector<HtmlHeading1>("h1.post_title").textContent

    override fun HtmlPage.getSeasonLinks(): List<HtmlAnchor> =
        querySelectorAll("div.main_content div.post_list a").map {
            it as HtmlAnchor
        }

    override fun List<HtmlAnchor>.getSeasonNameAndUrl(): List<Season> = map { node ->
        Season(
            name = node.title(),
            link = baseUrl + node.hrefAttribute
        )
    }

    override suspend fun getEpisodesFromSeason(seasonUrl: String): List<Episode> {
        // retrieve all episodes from the season page
        return webClient.getPage<HtmlPage>(seasonUrl).getSeasonLinks().map { node ->
            val episodeLink = baseUrl + node.hrefAttribute

            val episodeDownloadLink = webClient.getPage<HtmlPage>(episodeLink).querySelectorAll(
                "div.main_content div.file_attachment_wrapper a.button"
            )[1] as HtmlAnchor

            // click on the episode download link
            val episodeDownloadLinkAnchor = episodeDownloadLink.click<HtmlPage>().querySelector<HtmlAnchor>(
                "a.dl_link"
            )

            // retrieve the video link from the anchor after splitting it via '='
            val splits = episodeDownloadLinkAnchor.getAttribute("onClick").split("=")
            val videoUrl = when {
                splits.size > 1 -> splits[1]
                else -> splits[0]
            }.replace("'", "")

            Episode(
                name = node.title(),
                link = episodeLink,
                video = videoUrl,
                subtitle = "",
            )
        }
    }

    private fun HtmlAnchor.title() = getAttribute("title")
}