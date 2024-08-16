package me.matin.core.managers

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow
import me.matin.core.Core
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("unused")
object PacketManager {

    /**
     * Plays the hand swinging animation for the selected player.
     *
     * @param player Selected player
     * @param mainHand If true the player's mainhand, otherwise the offhand
     *    will swing.
     */
    @JvmStatic
    fun swingHand(player: Player, mainHand: Boolean) {
        schedule(true) {
            val animationType =
                if (mainHand) WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
                else WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND
            WrapperPlayServerEntityAnimation(player.entityId, animationType).also { packet ->
                getNearbyPlayers(player).forEach { PacketEvents.getAPI().playerManager.sendPacket(it, packet) }
            }
        }
    }

    private fun getNearbyPlayers(player: Player): Collection<Player> {
        val players: ArrayList<Player> = arrayListOf(player)
        val range = Core.corePlayerTrackingRange.getOrDefault(player.location.world, 64).let { it * it }
        val location = player.location
        Bukkit.getOnlinePlayers().filterNotNull().filter {
            it.world == location.world && it.location.distanceSquared(location) <= range
        }.forEach { players.add(it) }
        return players
    }

    /**
     * Shows totem animation to the selected player.
     *
     * @param player Selected player
     * @param model (Optional) CustomModelData for the totem
     */
    @JvmStatic
    fun showTotem(player: Player, model: Int = -1) {
        schedule(true) {
            if (model < 0) playTotem(player).also { return@schedule }
            val oldItem = player.inventory.itemInOffHand
            val item = ItemStack(Material.TOTEM_OF_UNDYING)
            item.itemMeta.also { meta ->
                meta.setCustomModelData(model)
                item.itemMeta = meta
            }
            var isItemSet = false
            schedule {
                player.inventory.setItem(40, item)
                isItemSet = true
            }
            while (!isItemSet) runCatching { Thread.sleep(10) }.onFailure { return@schedule }
            playTotem(player)
            schedule {
                player.inventory.setItem(40, oldItem)
            }
        }
    }

    private fun playTotem(player: Player) = WrapperPlayServerEntityStatus(player.entityId, 35).let {
        PacketEvents.getAPI().playerManager.sendPacket(player, it)
    }

    /**
     * Changes the title of the inventory witch the selected player is
     * currently viewing.
     *
     * @param player Selected player
     * @param title New title
     */
    @JvmStatic
    fun changeInvTitle(player: Player, title: Component) {
        val (containerId, type) = Core.packetInvTitle.idType[player] ?: return
        val wrapper = WrapperPlayServerOpenWindow(containerId, type, title)
        PacketEvents.getAPI().playerManager.sendPacket(player, wrapper)
    }

    class InventoryTitle: PacketListenerAbstract(PacketListenerPriority.NORMAL) {

        val idType = mutableMapOf<Player, Pair<Int, Int>>()

        override fun onPacketSend(event: PacketSendEvent?) {
            event ?: return
            if (event.packetType != PacketType.Play.Server.OPEN_WINDOW) return
            val wrapper = WrapperPlayServerOpenWindow(event)
            val player = event.player as? Player ?: return
            idType[player] = wrapper.containerId to wrapper.type
        }
    }
}