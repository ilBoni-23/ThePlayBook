package com.example.theplaybook.data.remote.models

import com.google.gson.annotations.SerializedName

data class SteamResponse<T>(
    @SerializedName("response") val response: T
)

data class OwnedGamesResponse(
    @SerializedName("game_count") val gameCount: Int,
    @SerializedName("games") val games: List<SteamGame>
)

data class SteamGame(
    @SerializedName("appid") val appId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("playtime_forever") val playtimeForever: Int,
    @SerializedName("playtime_2weeks") val playtime2Weeks: Int?,
    @SerializedName("img_icon_url") val imgIconUrl: String,
    @SerializedName("img_logo_url") val imgLogoUrl: String,
    @SerializedName("has_community_visible_stats") val hasCommunityVisibleStats: Boolean,
    @SerializedName("rtime_last_played") val rtimeLastPlayed: Long?
)

data class PlayerSummariesResponse(
    @SerializedName("players") val players: List<PlayerSummary>
)

data class PlayerSummary(
    @SerializedName("steamid") val steamId: String,
    @SerializedName("personaname") val personaName: String,
    @SerializedName("profileurl") val profileUrl: String,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("avatarmedium") val avatarMedium: String,
    @SerializedName("avatarfull") val avatarFull: String
)

data class PlayerAchievementsResponse(
    @SerializedName("playerstats") val playerStats: PlayerStats
)

data class PlayerStats(
    @SerializedName("steamID") val steamId: String,
    @SerializedName("gameName") val gameName: String,
    @SerializedName("achievements") val achievements: List<SteamAchievement>?,
    @SerializedName("success") val success: Boolean
)

data class SteamAchievement(
    @SerializedName("apiname") val apiName: String,
    @SerializedName("achieved") val achieved: Int,
    @SerializedName("unlocktime") val unlockTime: Long?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)