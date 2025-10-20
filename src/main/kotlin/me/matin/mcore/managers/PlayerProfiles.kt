package me.matin.mcore.managers

import com.destroystokyo.paper.profile.PlayerProfile
import me.matin.mcore.Hooks
import net.skinsrestorer.api.Base64Utils.decode
import net.skinsrestorer.api.PropertyUtils
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit.createProfile
import org.bukkit.OfflinePlayer
import org.bukkit.profile.PlayerTextures.SkinModel
import java.net.URI
import java.util.*

@Suppress("unused")
object PlayerProfiles {
	
	/**
	 * @param player Player witch you want the profile of.
	 * @return [PlayerProfile] of the player with SkinsRestorer support.
	 */
	@JvmStatic
	operator fun get(player: OfflinePlayer, model: SkinModel? = null): PlayerProfile {
		if (!Hooks.skinsRestorer.isHooked) return player.playerProfile
		val skin = runCatching {
			SkinsRestorerProvider.get().playerStorage.getSkinForPlayer(player.uniqueId, player.name).get()
		}.getOrNull() ?: return player.playerProfile
		return get(
			PropertyUtils.getSkinTextureUrl(skin),
			false,
			model ?: SkinModel.valueOf(PropertyUtils.getSkinVariant(skin).name)
		)
	}
	
	/**
	 * @param value URL or Base64 of the skin for the profile.
	 * @param base64 Whether [value] is Base64 or URL.
	 * @param model (Optional) Model of the skin.
	 * @return [PlayerProfile] from the given url.
	 */
	@JvmStatic
	operator fun get(
		value: String,
		base64: Boolean,
		model: SkinModel = SkinModel.CLASSIC,
	): PlayerProfile = createProfile(UUID.randomUUID()).apply {
		runCatching {
			URI(if (base64) PropertyUtils.getSkinTextureUrl(decode(value)) else value).toURL()
		}.getOrNull()?.let { textures.apply { setSkin(it, model) } }
	}
}