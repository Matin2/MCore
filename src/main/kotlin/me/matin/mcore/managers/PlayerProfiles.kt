package me.matin.mcore.managers

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.matin.mcore.Hooks
import net.skinsrestorer.api.PropertyUtils
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit.createProfile
import org.bukkit.OfflinePlayer
import org.bukkit.profile.PlayerTextures.SkinModel
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
	 * @param player Player witch you want the profile of.
	 * @return [PlayerProfile] of the player with SkinsRestorer support.
	 */
	@JvmStatic
	operator fun get(player: OfflinePlayer) = if (Hooks.skinsRestorer.isHooked) SkinsRestorerProvider.get()
		.playerStorage
		.runCatching { getSkinForPlayer(player.uniqueId, player.name).get() }
		.map { PropertyUtils.getSkinTextureUrl(it) }
		.map { get(it, model = SkinModel.valueOf(PropertyUtils.getSkinVariant(it).name)) }
		.getOrDefault(player.playerProfile)
	else player.playerProfile
	
	/**
	 * @param value URL or Base64 of the skin for the profile.
	 * @param base64 (Optional) Whether [value] is Base64. defaults to `false`
	 * @param model (Optional) Model of the skin. defaults to
	 *    [SkinModel.CLASSIC]
	 * @return [PlayerProfile] from the given url.
	 */
	@JvmStatic
	operator fun get(
		value: String,
		base64: Boolean = false,
		model: SkinModel = CLASSIC,
	) = createProfile(UUID.randomUUID()).apply {
		runCatching { URI(if (base64) value.url else value).toURL() }.onSuccess { textures.setSkin(it, model) }
	}
}