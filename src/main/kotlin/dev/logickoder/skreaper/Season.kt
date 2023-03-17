package dev.logickoder.skreaper

data class Season(
    val name: String = "",
    val link: String = "",
    val episodes: List<Episode> = emptyList()
)
