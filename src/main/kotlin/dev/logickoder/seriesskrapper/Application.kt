package dev.logickoder.seriesskrapper

import dev.logickoder.seriesskrapper.netnaija.Netnaija

suspend fun main() {
    println(Netnaija().scrape("https://www.thenetnaija.net/videos/series/10426-the-mandalorian"))
}