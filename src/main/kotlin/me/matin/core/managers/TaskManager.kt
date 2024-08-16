package me.matin.core.managers

import me.matin.core.Core
import me.matin.core.managers.Extras.ticks
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

@Suppress("UnusedReceiverParameter")
object TaskManager {

    @JvmStatic
    fun Any.schedule(
        plugin: Plugin,
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ): BukkitTask = Bukkit.getScheduler().run {
        when (async) {
            true -> when {
                interval != Duration.ZERO -> runTaskTimerAsynchronously(plugin, task, delay.ticks, interval.ticks)
                delay != Duration.ZERO -> runTaskLaterAsynchronously(plugin, task, delay.ticks)
                else -> runTaskAsynchronously(plugin, task)
            }

            false -> when {
                interval != Duration.ZERO -> runTaskTimer(plugin, task, delay.ticks, interval.ticks)
                delay != Duration.ZERO -> runTaskLater(plugin, task, delay.ticks)
                else -> runTask(plugin, task)
            }
        }
    }

    internal fun Any.schedule(
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ): BukkitTask = schedule(Core.instance, async, delay, interval, task)
}