package eu.animedrive

import eu.kanade.tachiyomi.animesource.model.*
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class AnimeDrive : AnimeHttpSource() {
    override val name = "AnimeDrive"
    override val baseUrl = "https://animedrive.hu"
    override val lang = "hu"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/search/?page=$page")

    override fun popularAnimeParse(response: Response): AnimesPage {
        val doc = Jsoup.parse(response.body.string())
        val animes = doc.select("a.nk-image-box-1").map {
            SAnime.create().apply {
                title = it.select("h4").text()
                thumbnail_url = it.select("img").attr("abs:src")
                url = it.attr("href")
            }
        }
        return AnimesPage(animes, animes.size == 30)
    }

    override fun episodeListRequest(anime: SAnime): Request =
        GET("$baseUrl${anime.url}")

    override fun episodeListParse(response: Response): List<SEpisode> {
        val doc = Jsoup.parse(response.body.string())
        val id = response.request.url.queryParameter("id")
        return doc.select("a.episode-item").mapIndexed { i, ep ->
            SEpisode.create().apply {
                name = ep.text()
                url = "/watch/?id=$id&ep=${i + 1}"
                episode_number = (i + 1).toFloat()
            }
        }
    }

    override fun videoListRequest(episode: SEpisode): Request =
        GET("$baseUrl${episode.url}")

    override fun videoListParse(response: Response): List<Video> {
        val id = response.request.url.queryParameter("id")
        val ep = response.request.url.queryParameter("ep")
        val playerUrl = "https://player.animedrive.hu/player_new.php?id=$id&ep=$ep"
        return listOf(Video(playerUrl, "AnimeDrive Player", playerUrl))
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/search/?q=$query&page=$page")

    override fun searchAnimeParse(response: Response) = popularAnimeParse(response)
    override fun latestUpdatesRequest(page: Int) = popularAnimeRequest(page)
    override fun latestUpdatesParse(response: Response) = popularAnimeParse(response)
    override fun animeDetailsParse(response: Response) = SAnime.create()
}
