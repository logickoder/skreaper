package dev.logickoder.skreaper

/**
 * Data class representing an episode.
 *
 * @param name The name of the episode.
 * @param link The URL associated with the episode.
 * @param video The video URL for the episode.
 * @param subtitle The subtitle URL for the episode.
 */
data class Episode(
    val name: String = "",
    val link: String = "",
    val video: String = "",
    val subtitle: String = "",
)