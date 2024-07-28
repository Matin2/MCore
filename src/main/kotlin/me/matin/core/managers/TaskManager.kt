package me.matin.core.managers

import me.matin.core.Core.Companion.instance
import me.matin.core.managers.TextManager.toTicks
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

object TaskManager {

    @JvmStatic
    fun runTask(async: Boolean = false, task: () -> Unit) {
        if (async) Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable { task() })
        else Bukkit.getScheduler().runTask(instance, Runnable { task() })
    }

    @JvmStatic
    fun scheduleTask(
        delay: Duration,
        interval: Duration,
        async: Boolean = false,
        task: () -> Unit
    ): BukkitTask {
        return if (async) Bukkit.getScheduler()
            .runTaskTimerAsynchronously(instance, Runnable { task() }, delay.toTicks(), interval.toTicks())
        else Bukkit.getScheduler().runTaskTimer(instance, Runnable { task() }, delay.toTicks(), interval.toTicks())
    }
}