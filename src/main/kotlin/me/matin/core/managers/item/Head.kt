package me.matin.core.managers.item

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
object Head {

    private val ID = UUID.fromString("7f7278e3-bcdc-45ea-b7ef-49a6922f64b7")!!

    operator fun get(player: OfflinePlayer): PlayerProfile {
        Core.skinsRestorer?.apply {
            val skin = playerStorage.getSkinForPlayer(player.uniqueId, player.name).takeIf { it.isPresent }?.get()
                ?: return player.playerProfile
            val stringURL = PropertyUtils.getSkinTextureUrl(skin)
            return get(stringURL)
        }
        return player.playerProfile
    }

    operator fun get(value: String, base64: Boolean = false): PlayerProfile {
        val stringURL = if (base64) base64toURL(value) else value
        val url = URI(stringURL).toURL()
        return createProfile(url)
    }

    private fun createProfile(url: URL): PlayerProfile {
        val profile = Bukkit.createProfile(ID)
        val textures = profile.textures
        textures.skin = url
        profile.setTextures(textures)
        return profile
    }

    enum class DataBase {

        HeadDatabase, HeadDB;

        operator fun get(id: Int): PlayerProfile? {
            when (this) {
                HeadDatabase -> Core.headDatabase?.apply {
                    val base64 = getBase64(id.toString()).takeIf { isHead(id.toString()) } ?: return null
                    return Head[base64, true]
                }

                HeadDB -> {
                    if (!Core.headDB) return null
                    val base64 = HeadAPI.getHeadById(id).takeIf { it.isPresent }?.get()?.texture ?: return null
                    return Head[base64, true]
                }
            }
            return null
        }
    }

    private fun base64toURL(value: String): String {
        val base = Base64.getDecoder().decode(value).toString(Charset.defaultCharset())
        val decoded = Json.decodeFromString<Textures>(base)
        return decoded.textures.SKIN.url
    }

    @Serializable
    data class Textures(val textures: Skin) {

        @Suppress("PropertyName")
        @Serializable
        data class Skin(val SKIN: SkinUrl)

        @Serializable
        data class SkinUrl(val url: String)
    }
}