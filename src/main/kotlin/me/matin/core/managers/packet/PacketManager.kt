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
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

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

    private fun playTotem(player: Player) = WrapperPlayServerEntityStatus(player.entityId, 35).let {
        PacketEvents.getAPI().playerManager.sendPacket(player, it)
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
        val key = NamespacedKey("mcore", "left_handed")
        val container = player.persistentDataContainer
        val leftHanded = if (container.has(key)) player.persistentDataContainer.get(key, PersistentDataType.BOOLEAN)!! else false
        return if (leftHanded) HumanoidArm.LEFT else HumanoidArm.RIGHT
    }
}