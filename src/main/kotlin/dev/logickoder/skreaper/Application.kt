package dev.logickoder.skreaper

import dev.logickoder.skreaper.netnaija.Netnaija

suspend fun main() {
    println(Netnaija().scrape("https://www.thenetnaija.net/videos/series/10426-the-mandalorian"))
}