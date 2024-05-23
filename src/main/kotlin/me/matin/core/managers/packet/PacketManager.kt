package me.matin.core.managers.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus
import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("unused")
object PacketManager {

    @JvmStatic
    fun swingHand(player: Player, mainHand: Boolean) {
        val animationType =
            if (mainHand) WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
            else WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND
        WrapperPlayServerEntityAnimation(player.entityId, animationType).let { packet ->
            getNearbyPlayers(player).forEach { PacketEvents.getAPI().playerManager.sendPacket(it, packet) }
        }
    }

    @JvmStatic
    private fun getNearbyPlayers(player: Player): Collection<Player> {
        val players: ArrayList<Player> = arrayListOf(player)
        val range = Core.corePlayerTrackingRange.getOrDefault(player.location.world, 64).let { it * it }
        val location = player.location
        Bukkit.getOnlinePlayers().forEach {
            val loc = it.location
            if (loc.world == location.world && loc.distanceSquared(location) <= range)
                players.add(it)
        }
        return players
    }

    @JvmStatic
    fun showTotem(player: Player, model: Int?) {
        if (model == null) playTotem(player).also { return }
        val oldItem = player.inventory.itemInOffHand
        ItemStack(Material.TOTEM_OF_UNDYING).let {
            it.itemMeta.let { meta ->
                meta.setCustomModelData(model)
                it.setItemMeta(meta)
            }
            changeSlotItem(player, it)
        }
        playTotem(player)
        changeSlotItem(player, oldItem)
    }

    private fun playTotem(player: Player) = WrapperPlayServerEntityStatus(player.entityId, 35).let {
        PacketEvents.getAPI().playerManager.sendPacket(player, it)
    }

    private fun changeSlotItem(player: Player, item: ItemStack) {
        player.inventory.setItem(40, item)
    }

//    @JvmStatic
//    fun changeItem(player: Player, item: ItemStack, slot: Int) {
//        val packetItem = SpigotConversionUtil.fromBukkitItemStack(item)
//        val s = minOf(maxOf(slot, 0), 40)
//        val packet = WrapperPlayServerSetSlot(-2, 0, s, packetItem)
//        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
//    }
}