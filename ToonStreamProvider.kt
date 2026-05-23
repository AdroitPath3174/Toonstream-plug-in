package com.toonstream

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class ToonStreamProvider : MainAPI() {

    override var mainUrl = "https://toonstream.vip"
    override var name = "ToonStream"
    override val hasMainPage = true
    override var lang = "hi"

    override val supportedTypes = setOf(
        TvType.Anime
    )

    override suspend fun search(query: String): List<SearchResponse> {

        val document = app.get(
            "$mainUrl/?s=$query"
        ).document

        return document.select(".result-item").map {

            val title = it.selectFirst("img")
                ?.attr("alt") ?: "No Title"

            val href = it.selectFirst("a")
                ?.attr("href") ?: ""

            val poster = it.selectFirst("img")
                ?.attr("src")

            newAnimeSearchResponse(
                title,
                href
            ) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {

        val document = app.get(url).document

        val title = document.selectFirst("h1")
            ?.text() ?: "Unknown"

        val poster = document.selectFirst("img")
            ?.attr("src")

        val description = document.selectFirst(".entry-content")
            ?.text()

        val episodes = document
            .select(".episodiodata")
            .map {

                Episode(
                    it.selectFirst("a")!!.attr("href"),
                    it.text()
                )
            }

        return newAnimeLoadResponse(
            title,
            url,
            TvType.Anime
        ) {

            posterUrl = poster
            plot = description
            this.episodes = episodes.reversed()
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val document = app.get(data).document

        val iframe = document
            .selectFirst("iframe")
            ?.attr("src")
            ?: return false

        loadExtractor(
            iframe,
            data,
            subtitleCallback,
            callback
        )

        return true
    }
}