package com.example.theplaybook.data.remote

import com.example.theplaybook.data.remote.models.*
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {

    @GET("IPlayerService/GetOwnedGames/v1/")
    suspend fun getOwnedGames(
        @Query("steamid") steamId: String,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includeFreeGames: Int = 1
    ): SteamResponse<OwnedGamesResponse>

    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getPlayerSummaries(
        @Query("steamids") steamIds: String
    ): SteamResponse<PlayerSummariesResponse>

    @GET("ISteamUserStats/GetPlayerAchievements/v1/")
    suspend fun getPlayerAchievements(
        @Query("steamid") steamId: String,
        @Query("appid") appId: Long
    ): SteamResponse<PlayerAchievementsResponse>

    @GET("ISteamUserStats/GetGlobalAchievementPercentagesForApp/v2/")
    suspend fun getGlobalAchievementPercentages(
        @Query("gameid") gameId: Long
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