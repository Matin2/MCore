package me.matin.core.managers.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.player.Equipment
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashSet

@Suppress("unused")
object PacketManager {

    @JvmStatic
    fun swingHand(player: Player, mainHand: Boolean) {
        val animationType =
            if (mainHand) WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
            else WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND
        val animation = WrapperPlayServerEntityAnimation(player.entityId, animationType)
        for (target in getNearbyPlayers(player)) {
            PacketEvents.getAPI().playerManager.sendPacket(target, animation)
        }
    }

    @JvmStatic
    private fun getNearbyPlayers(player: Player): Collection<Player> {
        val players: MutableCollection<Player> = HashSet()
        players.add(player)
        var range: Int = Core.corePlayerTrackingRange.getOrDefault(player.location.world, 64)
        range *= range
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.location.world == player.location.world && (p.location.distanceSquared(player.location) <= range))
                players.add(p)
        }
        return players
    }

    @JvmStatic
    fun showTotem(player: Player, model: Optional<Int>) {
        if (!model.isPresent) playTotem(player).also { return }
        val oldItem = player.inventory.itemInOffHand
        ItemStack(Material.TOTEM_OF_UNDYING).let {
            it.itemMeta.let { meta ->
                meta.setCustomModelData(model.get())
                it.setItemMeta(meta)
            }
            changeItem(player, it, EquipmentSlot.OFF_HAND)
        }
        playTotem(player)
        changeItem(player, oldItem, EquipmentSlot.OFF_HAND)
    }

    private fun playTotem(player: Player) {
        val packet = WrapperPlayServerEntityStatus(player.entityId, 35)
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }

    @JvmStatic
    fun changeItem(player: Player, item: ItemStack, slot: EquipmentSlot) {
        val packetItem = SpigotConversionUtil.fromBukkitItemStack(item)
        val equipment = Equipment(slot, packetItem)
        val packet = WrapperPlayServerEntityEquipment(player.entityId, listOf(equipment))
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }

    @JvmStatic
    fun getClientHand(player: Player): HumanoidArm {
        return PacketListener.clientHandMap[player] ?: HumanoidArm.RIGHT
    }
}