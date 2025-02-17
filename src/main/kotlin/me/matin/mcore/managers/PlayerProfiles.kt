package me.matin.mcore.managers

import com.destroystokyo.paper.profile.PlayerProfile
import me.matin.mcore.Depends
import me.matin.mlib.nullable
import net.skinsrestorer.api.Base64Utils.decode
import net.skinsrestorer.api.PropertyUtils
import org.bukkit.Bukkit.createProfile
import org.bukkit.OfflinePlayer
import org.bukkit.profile.PlayerTextures.SkinModel
import tsp.headdb.core.api.HeadAPI.getHeadById
import java.net.URI
import java.util.*

@Suppress("unused")
object PlayerProfiles {
	
	/**
	 * @param player Player witch you want the profile of.
	 * @return [PlayerProfile] of the player with SkinsRestorer support.
	 */
	operator fun get(player: OfflinePlayer): PlayerProfile {
		val skin = runCatching {
			Depends.skinsRestorer?.playerStorage?.getSkinForPlayer(player.uniqueId, player.name)?.nullable
		}.getOrNull() ?: return player.playerProfile
		return get(
			PropertyUtils.getSkinTextureUrl(skin),
			false,
			SkinModel.valueOf(PropertyUtils.getSkinVariant(skin).name)
		)
	}
	
	/**
	 * @param value URL or Base64 of the skin for the profile.
	 * @param base64 Whether [value] is Base64 or URL.
	 * @param model (Optional) Model of the skin.
	 * @return [PlayerProfile] from the given url.
	 */
	operator fun get(value: String, base64: Boolean, model: SkinModel = SkinModel.CLASSIC): PlayerProfile =
		createProfile(UUID.randomUUID()).apply {
			val url = URI(if (base64) PropertyUtils.getSkinTextureUrl(decode(value)) else value).toURL()
			setTextures(textures.apply { setSkin(url, model) })
		}
	
	enum class DataBase(private val base64: (Int) -> String?) {
		
		HeadDatabase({ Depends.headDatabase?.getBase64("$it") }),
		HeadDB({ if (Depends.headDB) getHeadById(it).nullable?.texture else null });
		
		/**
		 * @param id Head id witch the profile is created for.
		 * @return [PlayerProfile] from the given head id or `null` if not found.
		 */
		operator fun get(id: Int) = base64(id)?.let { get(it, true) }
	}
}