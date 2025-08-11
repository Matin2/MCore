package me.matin.mcore.managers

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.coroutines.future.asDeferred
import me.matin.mcore.Hooks
import net.skinsrestorer.api.Base64Utils.decode
import net.skinsrestorer.api.PropertyUtils
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit.createProfile
import org.bukkit.OfflinePlayer
import org.bukkit.profile.PlayerTextures.SkinModel
import java.net.URI
import java.util.*
import kotlin.jvm.optionals.getOrNull

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
	operator fun get(value: String, base64: Boolean, model: SkinModel = SkinModel.CLASSIC): PlayerProfile =
		createProfile(UUID.randomUUID()).apply {
			val url = URI(if (base64) PropertyUtils.getSkinTextureUrl(decode(value)) else value).toURL()
			setTextures(textures.apply { setSkin(url, model) })
		}
	
	enum class DataBase(private val base64: suspend (Int) -> String?) {
		
		HeadDatabase({ Hooks.HeadDatabase.api?.await()?.getBase64("$it") }),
		HeadDB({ Hooks.HeadDB.api?.await()?.findById(it)?.asDeferred()?.await()?.getOrNull()?.texture });
		
		/**
		 * @param id Head id witch the profile is created for.
		 * @return [PlayerProfile] from the given head id or `null` if not found.
		 */
		suspend operator fun get(id: Int) = base64(id)?.let { get(it, true) }
	}
}