package dev.logickoder.skreaper

/**
 * Represents a season of a TV show or series.
 *
 * @property name The name of the season.
 * @property link The URL associated with the season.
 * @property episodes The list of episodes in the season. Empty list by default.
 */
data class Season(
    val name: String = "",
    val link: String = "",
    val episodes: List<Episode> = emptyList()
)