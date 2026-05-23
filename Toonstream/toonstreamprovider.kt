package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class ToonStreamProvider : MainAPI() {
    override var mainUrl = "https://toonstream.vip"
    override var name = "ToonStream"
    override val hasMainPage = true
    override var lang = "hi"
    override val supportedTypes = setOf(
        TvType.Anime
    )

    override val mainPage = mainPageOf(
        "$mainUrl" to "Latest"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val document = app.get(request.data).document

        val home = document.select("article").mapNotNull {
            it.toSearchResult()
        }

        return newHomePageResponse(
            request.name,
            home
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {

        val document =
            app.get("$mainUrl/?s=$query").document

        return document.select("article").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {

        val title =
            this.selectFirst("h2")?.text() ?: return null

        val href =
            fixUrl(this.selectFirst("a")?.attr("href") ?: return null)

        val posterUrl =
            this.selectFirst("img")?.attr("src")

        return newAnimeSearchResponse(
            title,
            href,
            TvType.Anime
        ) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun load(url: String): LoadResponse {

        val document = app.get(url).document

        val title =
            document.selectFirst("h1")?.text() ?: "No Title"

        val poster =
            document.selectFirst("img")?.attr("src")

        val description =
            document.selectFirst("meta[name=description]")
                ?.attr("content")

        val episodes = document.select("a").mapIndexedNotNull { index, element ->

            val href = element.attr("href")

            if (href.contains("/episode")) {
                Episode(
                    data = fixUrl(href),
                    name = element.text(),
                    episode = index + 1
                )
            } else null
        }

        return newAnimeLoadResponse(
            title,
            url,
            TvType.Anime
        ) {
            posterUrl = poster
            plot = description
            this.episodes = episodes
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val document = app.get(data).document

        val iframe =
            document.selectFirst("iframe")
                ?.attr("src") ?: return false

        callback.invoke(
            ExtractorLink(
                this.name,
                this.name,
                iframe,
                "",
                Qualities.Unknown.value
            )
        )

        return true
    }
}
