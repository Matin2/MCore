package me.matin.mcore.methods

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.matin.mcore.Hooks
import net.skinsrestorer.api.PropertyUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.profile.PlayerTextures
import java.net.URI
import java.util.*

@Suppress("unused")
object PlayerProfiles {
	
	private typealias Base64 = String
	
	private inline val Base64.url
		get() = Json.parseToJsonElement(this)
			.jsonObject["textures"]!!
			.jsonObject["SKIN"]!!
			.jsonObject["url"]!!
			.jsonPrimitive.content
	
	/**
	 * @return [com.destroystokyo.paper.profile.PlayerProfile] of the player
	 *    with SkinsRestorer support.
	 * @receiver Player witch you want the profile of.
	 */
	@JvmStatic
	fun OfflinePlayer.getProfile() = Hooks.SkinsRestorerHook.api
		?.playerStorage
		?.runCatching { getSkinForPlayer(uniqueId, name).get() }
		?.map { PropertyUtils.getSkinTextureUrl(it) }
		?.map { get(it, model = PlayerTextures.SkinModel.valueOf(PropertyUtils.getSkinVariant(it).name)) }
		?.getOrNull() ?: playerProfile
	
	/**
	 * @param value URL or Base64 of the skin for the profile.
	 * @param base64 (Optional) Whether [value] is Base64. defaults to `false`
	 * @param model (Optional) Model of the skin. defaults to
	 *    [PlayerTextures.SkinModel.CLASSIC]
	 * @return [com.destroystokyo.paper.profile.PlayerProfile] from the given
	 *    url.
	 */
	@JvmStatic
	operator fun get(
		value: String,
		base64: Boolean = false,
		model: PlayerTextures.SkinModel = CLASSIC,
	) = Bukkit.createProfile(UUID.randomUUID()).apply {
		runCatching { URI(if (base64) value.url else value).toURL() }.onSuccess { textures.setSkin(it, model) }
	}
}