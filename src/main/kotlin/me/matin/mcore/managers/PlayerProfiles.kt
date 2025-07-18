package me.matin.mcore.managers

import com.destroystokyo.paper.profile.PlayerProfile
import me.arcaniax.hdb.api.HeadDatabaseAPI
import me.matin.mcore.Depends
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
	operator fun get(player: OfflinePlayer): PlayerProfile {
		if (!Depends.skinsRestorer) return player.playerProfile
		val skin = runCatching {
			SkinsRestorerProvider.get().playerStorage.getSkinForPlayer(player.uniqueId, player.name).get()
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
		
		HeadDatabase({ if (Depends.headDatabase) HeadDatabaseAPI().getBase64("$it") else null }),
		HeadDB({ Depends.HeadDB.api?.findById(it)?.get()?.getOrNull()?.texture });
		
		/**
		 * @param id Head id witch the profile is created for.
		 * @return [PlayerProfile] from the given head id or `null` if not found.
		 */
		operator fun get(id: Int) = base64(id)?.let { get(it, true) }
	}
}