package me.matin.core.function

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.function.Predicate

class AttackAnimation {

    companion object {

        private var plugin = Core()

        private fun getOnlinePlayers(): Collection<Player> = HashSet(Bukkit.getOnlinePlayers())

        @JvmStatic
        fun play(player: Player, mainHand: Boolean) {
            if (Plugins.hasPlugin("ProtocolLib")) {
                Bukkit.getScheduler()
                    .runTaskAsynchronously(
                        plugin,
                        Runnable {
                            val playersInRange: Collection<Player> = filterOutOfRange(getOnlinePlayers(), player)
                            if (mainHand) sendMainhandMovement(playersInRange, player)
                            else sendOffhandMovement(playersInRange, player)
                        })
            }
        }

        private fun sendMainhandMovement(players: Collection<Player>, entity: Player) {
            if (!Plugins.hasPlugin("ProtocolLib")) return

            val packet1: PacketContainer =
                Core.protocolManager.createPacket(PacketType.Play.Server.ANIMATION)
            packet1.integers.write(0, entity.entityId)
            packet1.integers.write(1, 0)

            sendPacket(players, entity, packet1)
        }

        private fun sendOffhandMovement(players: Collection<Player>, entity: Player) {
            if (!Plugins.hasPlugin("ProtocolLib")) return

            val packet1: PacketContainer =
                Core.protocolManager.createPacket(PacketType.Play.Server.ANIMATION)
            packet1.integers.write(0, entity.entityId)
            packet1.integers.write(1, 3)

            sendPacket(players, entity, packet1)
        }

        private fun sendPacket(players: Collection<Player>, entity: Player, packet: PacketContainer) {
            if (!plugin.isEnabled) {
                return
            }

            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (entity.isOnline) {
                    Core.protocolManager.sendServerPacket(entity, packet)
                }
                for (player in players) {
                    if (player.isOnline) {
                        Core.protocolManager.sendServerPacket(player, packet)
                    }
                }
            })
        }

        private fun filterOutOfRange(players: Collection<Player>, entity: Entity): Collection<Player> {
            return filterOutOfRange(players, entity.location)
        }

        private fun filterOutOfRange(players: Collection<Player>, location: Location): Collection<Player> {
            return filterOutOfRange(players, location) { _: Player -> true }
        }

        private fun filterOutOfRange(
            players: Collection<Player>,
            location: Location,
            predicate: Predicate<Player>
        ): Collection<Player> {
            val playersInRange: MutableCollection<Player> = java.util.HashSet()
            var range: Int = Core.corePlayerTrackingRange.getOrDefault(location.world, 64)
            range *= range
            for (player in players) {
                val playerLocation: Location = player.location
                if (playerLocation.world == location.world && (playerLocation.distanceSquared(location) <= range) && predicate.test(
                        player
                    )
                ) {
                    playersInRange.add(player)
                }
            }
            return playersInRange
        }
    }
}