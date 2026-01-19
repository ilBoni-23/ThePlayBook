package com.example.theplaybook.data.mock

import com.example.theplaybook.data.remote.SteamApiService
import com.example.theplaybook.data.remote.models.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import java.util.*
import com.example.theplaybook.data.remote.GlobalAchievementPercentagesResponse
import com.example.theplaybook.data.remote.AchievementPercentages
import com.example.theplaybook.data.remote.GlobalAchievement

class MockSteamRepository : SteamApiService {

    private val mockPlayers = listOf(
        MockPlayer.PLAYER_1,
        MockPlayer.PLAYER_2,
        MockPlayer.PLAYER_3
    )

    private val mockGames = MockGames.ALL_GAMES

    private suspend fun simulateNetworkDelay(minMs: Long = 300, maxMs: Long = 1500) {
        delay(Random.nextLong(minMs, maxMs))
    }

    override suspend fun getPlayerSummaries(
        steamids: String
    ): SteamResponse<PlayerSummariesResponse> {
        simulateNetworkDelay()

        val steamId = steamids.split(",").firstOrNull() ?: ""
        val player = mockPlayers.find { it.steamId == steamId }
            ?: mockPlayers.random().copy(steamId = steamId)

        return SteamResponse(
            PlayerSummariesResponse(players = listOf(player))
        )
    }

    override suspend fun getOwnedGames(
        steamid: String,
        include_appinfo: Int,
        include_played_free_games: Int
    ): SteamResponse<OwnedGamesResponse> {
        simulateNetworkDelay()

        val playerGames = when (steamid) {
            MockPlayer.PLAYER_1.steamId -> listOf(
                MockGames.CS2, MockGames.DOTA2, MockGames.GTA5,
                MockGames.ELDEN_RING, MockGames.CYBERPUNK
            )
            MockPlayer.PLAYER_2.steamId -> listOf(
                MockGames.MINECRAFT, MockGames.APEX_LEGENDS,
                MockGames.BALDURS_GATE3, MockGames.WITCHER3
            )
            else -> mockGames.shuffled().take(Random.nextInt(15, 50))
        }

        return SteamResponse(
            OwnedGamesResponse(
                gameCount = playerGames.size,  // ← gameCount, non game_count
                games = playerGames
            )
        )
    }

    override suspend fun getPlayerAchievements(
        steamid: String,
        appId: Long
    ): SteamResponse<PlayerAchievementsResponse> {
        simulateNetworkDelay()

        val game = mockGames.find { it.appId == appId }
        val gameName = game?.name ?: "Unknown Game"
        val achievements = MockAchievements.getForGame(appId)

        return SteamResponse(
            PlayerAchievementsResponse(
                playerStats = PlayerStats(
                    steamId = steamid,
                    gameName = gameName,
                    achievements = achievements,
                    success = true
                )
            )
        )
    }

    override suspend fun getGlobalAchievementPercentages(
        gameid: Long
    ): SteamResponse<GlobalAchievementPercentagesResponse> {
        simulateNetworkDelay()

        val achievements = MockAchievements.getGlobalPercentages(gameid)

        return SteamResponse(
            GlobalAchievementPercentagesResponse(
                achievementpercentages = AchievementPercentages(
                    achievements = achievements
                )
            )
        )
    }
}

// Modelli Mock
object MockPlayer {
    val PLAYER_1 = PlayerSummary(
        steamId = "76561197960287930",
        personaName = "ThePlayBook Tester",
        profileUrl = "https://steamcommunity.com/id/theplaybook",
        avatar = "https://i.pravatar.cc/32?img=1",
        avatarMedium = "https://i.pravatar.cc/64?img=1",
        avatarFull = "https://i.pravatar.cc/184?img=1"
    )

    val PLAYER_2 = PlayerSummary(
        steamId = "76561198040672323",
        personaName = "GamerPro",
        profileUrl = "https://steamcommunity.com/id/gamerpro",
        avatar = "https://i.pravatar.cc/32?img=5",
        avatarMedium = "https://i.pravatar.cc/64?img=5",
        avatarFull = "https://i.pravatar.cc/184?img=5"
    )

    val PLAYER_3 = PlayerSummary(
        steamId = "76561198123456789",
        personaName = "CasualGamer",
        profileUrl = "https://steamcommunity.com/id/casual",
        avatar = "https://i.pravatar.cc/32?img=8",
        avatarMedium = "https://i.pravatar.cc/64?img=8",
        avatarFull = "https://i.pravatar.cc/184?img=8"
    )
}

object MockGames {
    val CS2 = SteamGame(
        appId = 730,
        name = "Counter-Strike 2",
        playtimeForever = Random.nextInt(500, 5000),
        playtime2Weeks = Random.nextInt(0, 600),
        imgIconUrl = "fcfb366051782b8ebf2aa297f3b746395858cb62",
        imgLogoUrl = "e4ad9cf1b7dc8475c1118625daf9abd4bdcbcad0",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - Random.nextLong(0, 604800)
    )

    val DOTA2 = SteamGame(
        appId = 570,
        name = "Dota 2",
        playtimeForever = Random.nextInt(1000, 10000),
        playtime2Weeks = Random.nextInt(0, 1200),
        imgIconUrl = "0bbb630d63262dd66d2fdd0f7d37e8661a410075",
        imgLogoUrl = "6e988b7c62e5d1e3e6c8a4b2e7c32d6b8d8e2f7c",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - Random.nextLong(0, 864000)
    )

    val GTA5 = SteamGame(
        appId = 271590,
        name = "Grand Theft Auto V",
        playtimeForever = Random.nextInt(100, 2000),
        playtime2Weeks = null,
        imgIconUrl = "gta5_icon_hash",
        imgLogoUrl = "gta5_logo_hash",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - Random.nextLong(604800, 2592000)
    )

    val ELDEN_RING = SteamGame(
        appId = 1245620,
        name = "Elden Ring",
        playtimeForever = 234,
        playtime2Weeks = 89,
        imgIconUrl = "elden_icon",
        imgLogoUrl = "elden_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - 259200
    )

    val CYBERPUNK = SteamGame(
        appId = 1091500,
        name = "Cyberpunk 2077",
        playtimeForever = 189,
        playtime2Weeks = 45,
        imgIconUrl = "cyber_icon",
        imgLogoUrl = "cyber_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - 1728000
    )

    val MINECRAFT = SteamGame(
        appId = 255710,
        name = "Minecraft",
        playtimeForever = Random.nextInt(500, 3000),
        playtime2Weeks = Random.nextInt(0, 500),
        imgIconUrl = "minecraft_icon",
        imgLogoUrl = "minecraft_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - Random.nextLong(0, 604800)
    )

    val APEX_LEGENDS = SteamGame(
        appId = 1172470,
        name = "Apex Legends",
        playtimeForever = 654,
        playtime2Weeks = 210,
        imgIconUrl = "apex_icon",
        imgLogoUrl = "apex_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - 43200
    )

    val BALDURS_GATE3 = SteamGame(
        appId = 1086940,
        name = "Baldur's Gate 3",
        playtimeForever = Random.nextInt(200, 1500),
        playtime2Weeks = Random.nextInt(0, 300),
        imgIconUrl = "bg3_icon",
        imgLogoUrl = "bg3_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - Random.nextLong(0, 864000)
    )

    val WITCHER3 = SteamGame(
        appId = 292030,
        name = "The Witcher 3: Wild Hunt",
        playtimeForever = 456,
        playtime2Weeks = null,
        imgIconUrl = "witcher_icon",
        imgLogoUrl = "witcher_logo",
        hasCommunityVisibleStats = true,
        rtimeLastPlayed = Date().time / 1000 - 2592000
    )

    val ALL_GAMES = listOf(
        CS2, DOTA2, GTA5, ELDEN_RING, CYBERPUNK,
        MINECRAFT, APEX_LEGENDS, BALDURS_GATE3, WITCHER3,
        SteamGame(578080, "PUBG", 789, 120, "pubg_icon", "pubg_logo", true, Date().time/1000 - 86400),
        SteamGame(1085660, "Destiny 2", 321, 67, "destiny_icon", "destiny_logo", true, Date().time/1000 - 345600),
        SteamGame(359550, "Tom Clancy's Rainbow Six Siege", 543, 89, "r6_icon", "r6_logo", true, Date().time/1000 - 691200),
        SteamGame(553850, "Hellblade: Senua's Sacrifice", 87, null, "hellblade_icon", "hellblade_logo", true, Date().time/1000 - 2592000),
        SteamGame(230410, "Warframe", 1234, 340, "warframe_icon", "warframe_logo", true, Date().time/1000 - 86400),
        SteamGame(252950, "Rocket League", 876, 210, "rocket_icon", "rocket_logo", true, Date().time/1000 - 172800),
        SteamGame(730310, "Sea of Thieves", 543, 98, "sea_icon", "sea_logo", true, Date().time/1000 - 259200),
        SteamGame(275850, "No Man's Sky", 321, 45, "nms_icon", "nms_logo", true, Date().time/1000 - 604800),
        SteamGame(381210, "Dead by Daylight", 654, 120, "dbd_icon", "dbd_logo", true, Date().time/1000 - 86400),
        SteamGame(374320, "DARK SOULS™ III", 432, null, "ds3_icon", "ds3_logo", true, Date().time/1000 - 2592000),
        SteamGame(582010, "Monster Hunter: World", 765, 230, "mhw_icon", "mhw_logo", true, Date().time/1000 - 345600),
        SteamGame(218620, "PAYDAY 2", 987, 150, "payday_icon", "payday_logo", true, Date().time/1000 - 172800),
        SteamGame(236390, "War Thunder", 1234, 320, "wt_icon", "wt_logo", true, Date().time/1000 - 86400),
        SteamGame(346110, "ARK: Survival Evolved", 876, 210, "ark_icon", "ark_logo", true, Date().time/1000 - 604800),
        SteamGame(304930, "Unturned", 543, 89, "unturned_icon", "unturned_logo", true, Date().time/1000 - 2592000)
    )
}

object MockAchievements {
    fun getForGame(appId: Long): List<SteamAchievement> {
        return when (appId) {
            730L -> listOf( // CS2
                SteamAchievement("WIN_10_MATCHES", 1, Date().time/1000 - 864000, "First Blood", "Win 10 matches"),
                SteamAchievement("HEADSHOT_MASTER", 1, Date().time/1000 - 604800, "Headshot Master", "100 headshots"),
                SteamAchievement("DEFUSE_EXPERT", 0, null, "Defuse Expert", "Defuse 50 bombs"),
                SteamAchievement("MVP_50", 1, Date().time/1000 - 2592000, "MVP", "MVP 50 times"),
                SteamAchievement("CLUTCH_KING", 0, null, "Clutch King", "Win 1v5 situation"),
                SteamAchievement("PISTOL_ROUND", 1, Date().time/1000 - 3456000, "Pistol Round", "Win pistol round"),
                SteamAchievement("ACE", 0, null, "Ace", "Get an ace"),
                SteamAchievement("SMOKE_MASTER", 1, Date().time/1000 - 4320000, "Smoke Master", "Use 1000 smokes"),
                SteamAchievement("ECO_WIN", 0, null, "Eco Win", "Win eco round"),
                SteamAchievement("KNIFE_KILL", 1, Date().time/1000 - 5184000, "Knife Kill", "Get a knife kill")
            )
            570L -> listOf( // Dota 2
                SteamAchievement("FIRST_BLOOD", 1, Date().time/1000 - 1728000, "First Blood", "Get first blood"),
                SteamAchievement("RAMPAGE", 0, null, "Rampage", "Get a rampage"),
                SteamAchievement("GODLIKE", 1, Date().time/1000 - 2592000, "Godlike", "Get godlike streak"),
                SteamAchievement("PERFECT_GAME", 0, null, "Perfect Game", "Win without dying"),
                SteamAchievement("COMEBACK", 1, Date().time/1000 - 3456000, "Epic Comeback", "Win from mega creeps"),
                SteamAchievement("SUPPORT_MASTER", 1, Date().time/1000 - 4320000, "Support Master", "Place 1000 wards"),
                SteamAchievement("CARRY_GAME", 0, null, "Carry Game", "Get 1000 last hits"),
                SteamAchievement("ROSHAN_KILLER", 1, Date().time/1000 - 5184000, "Roshan Killer", "Kill Roshan 100 times"),
                SteamAchievement("DENY_MASTER", 0, null, "Deny Master", "Deny 1000 creeps"),
                SteamAchievement("ULTIMATE_COMBO", 1, Date().time/1000 - 6048000, "Ultimate Combo", "Land 5-man ultimate")
            )
            else -> (1..15).map {
                val unlocked = Random.nextFloat() > 0.6f // 40% sbloccati
                SteamAchievement(
                    apiName = "ACHIEVEMENT_${appId}_$it",
                    achieved = if (unlocked) 1 else 0,
                    unlockTime = if (unlocked) Date().time/1000 - Random.nextLong(0, 31536000) else null,
                    name = when (Random.nextInt(1, 6)) {
                        1 -> "First Steps"
                        2 -> "Master Explorer"
                        3 -> "Completionist"
                        4 -> "Speed Runner"
                        5 -> "Hidden Secret"
                        else -> "Special Achievement"
                    },
                    description = when (Random.nextInt(1, 6)) {
                        1 -> "Complete the tutorial"
                        2 -> "Explore all areas"
                        3 -> "Collect all items"
                        4 -> "Finish under 10 hours"
                        5 -> "Find hidden easter egg"
                        else -> "Special challenge completed"
                    }
                )
            }
        }
    }

    fun getGlobalPercentages(gameId: Long): List<GlobalAchievement> {
        return when (gameId) {
            730L -> listOf(
                GlobalAchievement("WIN_10_MATCHES", 85.5f),
                GlobalAchievement("HEADSHOT_MASTER", 67.2f),
                GlobalAchievement("DEFUSE_EXPERT", 42.8f),
                GlobalAchievement("MVP_50", 23.1f),
                GlobalAchievement("CLUTCH_KING", 12.5f),
                GlobalAchievement("PISTOL_ROUND", 78.9f),
                GlobalAchievement("ACE", 34.2f),
                GlobalAchievement("SMOKE_MASTER", 56.7f),
                GlobalAchievement("ECO_WIN", 45.3f),
                GlobalAchievement("KNIFE_KILL", 89.1f)
            )
            570L -> listOf(
                GlobalAchievement("FIRST_BLOOD", 92.3f),
                GlobalAchievement("RAMPAGE", 18.7f),
                GlobalAchievement("GODLIKE", 32.4f),
                GlobalAchievement("PERFECT_GAME", 5.2f),
                GlobalAchievement("COMEBACK", 28.9f),
                GlobalAchievement("SUPPORT_MASTER", 67.8f),
                GlobalAchievement("CARRY_GAME", 41.2f),
                GlobalAchievement("ROSHAN_KILLER", 73.5f),
                GlobalAchievement("DENY_MASTER", 54.6f),
                GlobalAchievement("ULTIMATE_COMBO", 22.1f)
            )
            else -> (1..15).map {
                GlobalAchievement(
                    name = "ACHIEVEMENT_${gameId}_$it",
                    percent = when (Random.nextInt(1, 101)) {
                        in 1..10 -> Random.nextFloat() * 10f  // 0-10% (rari)
                        in 11..40 -> Random.nextFloat() * 30f + 10f  // 10-40%
                        in 41..70 -> Random.nextFloat() * 30f + 40f  // 40-70%
                        in 71..90 -> Random.nextFloat() * 20f + 70f  // 70-90%
                        else -> Random.nextFloat() * 10f + 90f  // 90-100%
                    }
                )
            }
        }
    }
}