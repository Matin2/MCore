package me.matin.core.managers

import me.matin.core.Core.Companion.plugin
import me.matin.core.managers.TextManager.toTicks
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

object TaskManager {

    @JvmStatic
    fun runTask(async: Boolean = false, task: () -> Unit) {
        if (async) Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { task() })
        else Bukkit.getScheduler().runTask(plugin, Runnable { task() })
    }

    @JvmStatic
    fun scheduleTask(
        delay: Duration,
        interval: Duration,
        async: Boolean = false,
        task: () -> Unit
    ): BukkitTask {
        return if (async) Bukkit.getScheduler()
            .runTaskTimerAsynchronously(plugin, Runnable { task() }, delay.toTicks(), interval.toTicks())
        else Bukkit.getScheduler().runTaskTimer(plugin, Runnable { task() }, delay.toTicks(), interval.toTicks())
    }
}