package me.matin.core.managers.item

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import me.matin.core.Core
import net.skinsrestorer.api.PropertyUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import tsp.headdb.core.api.HeadAPI
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.util.*

@Suppress("unused")
object SkinProfile {

    /**
     * @param player Player witch you want the profile of.
     * @return [PlayerProfile] of the player with SkinsRestorer support.
     */
    operator fun get(player: OfflinePlayer): PlayerProfile {
        val skinsRestorer = Core.skinsRestorer ?: return player.playerProfile
        val skin = runCatching {
            skinsRestorer.playerStorage.getSkinForPlayer(player.uniqueId, player.name).takeIf { it.isPresent }?.get()
        }.getOrNull() ?: return player.playerProfile
        return get(PropertyUtils.getSkinTextureUrl(skin)) ?: player.playerProfile
    }

    /**
     * @param value URL or Base64 of the skin witch the profile is created for.
     * @param base64 (Optional) Whether [value] is Base64 or URL.
     * @return [PlayerProfile] from the given value or `null` if value is
     *    invalid.
     */
    operator fun get(value: String, base64: Boolean = false): PlayerProfile? {
        val stringURL = if (base64) base64toURL(value) else value
        val url = runCatching { URI(stringURL).toURL() }.getOrNull() ?: return null
        return createProfile(url)
    }

    private fun createProfile(url: URL): PlayerProfile {
        val profile = Bukkit.createProfile(UUID.randomUUID())
        val textures = profile.textures
        textures.skin = url
        profile.setTextures(textures)
        return profile
    }

    enum class Head {

        HeadDatabase, HeadDB;

        /**
         * @param id Head id witch the profile is created for.
         * @return [PlayerProfile] from the given head id or `null` if not found.
         */
        operator fun get(id: Int): PlayerProfile? {
            when (this) {
                HeadDatabase -> Core.headDatabase?.apply {
                    val base64 = getBase64(id.toString()).takeIf { isHead(id.toString()) } ?: return null
                    return SkinProfile[base64, true]
                }

                HeadDB -> {
                    if (!Core.headDB) return null
                    val base64 = HeadAPI.getHeadById(id).takeIf { it.isPresent }?.get()?.texture ?: return null
                    return SkinProfile[base64, true]
                }
            }
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun base64toURL(value: String): String {
        val base = Base64.getDecoder().decode(value).toString(Charset.defaultCharset())
        val decoded = Json.decodeFromString<JsonObject>(base)
        val foundURL = decoded["textures"]?.jsonObject?.get("SKIN")?.jsonObject?.get("url")?.toString()?.trim('"')
        val url = requireNotNull(foundURL) { "Provided value is not a correct skin base64!" }
        return url
    }
}