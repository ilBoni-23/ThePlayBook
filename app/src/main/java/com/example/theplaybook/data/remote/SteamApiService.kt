package com.example.theplaybook.data.remote

import com.example.theplaybook.data.remote.models.*
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {

    @GET("IPlayerService/GetOwnedGames/v1/")
    suspend fun getOwnedGames(
        @Query("steamid") steamid: String,
        @Query("include_appinfo") include_appinfo: Int = 1,
        @Query("include_played_free_games") include_played_free_games: Int = 1
    ): SteamResponse<OwnedGamesResponse>

    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getPlayerSummaries(
        @Query("steamids") steamids: String
    ): SteamResponse<PlayerSummariesResponse>

    @GET("ISteamUserStats/GetPlayerAchievements/v1/")
    suspend fun getPlayerAchievements(
        @Query("steamid") steamid: String,
        @Query("appid") appid: Long
    ): SteamResponse<PlayerAchievementsResponse>

    @GET("ISteamUserStats/GetGlobalAchievementPercentagesForApp/v2/")
    suspend fun getGlobalAchievementPercentages(
        @Query("gameid") gameid: Long
    ): SteamResponse<GlobalAchievementPercentagesResponse>
}

data class GlobalAchievementPercentagesResponse(
    val achievementpercentages: AchievementPercentages
)

data class AchievementPercentages(
    val achievements: List<GlobalAchievement>
)

data class GlobalAchievement(
    val name: String,
    val percent: Float
)