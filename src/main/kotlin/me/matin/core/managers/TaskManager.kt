package me.matin.core.managers

import me.matin.core.managers.TextManager.toTicks
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class TaskManager(private val plugin: Plugin) {

    operator fun invoke(
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ): BukkitTask = when (async) {
        true -> Bukkit.getScheduler().run {
            when {
                interval != Duration.ZERO -> runTaskTimerAsynchronously(
                    plugin,
                    task,
                    delay.toTicks(),
                    interval.toTicks()
                )

                delay != Duration.ZERO -> runTaskLaterAsynchronously(plugin, task, delay.toTicks())
                else -> runTaskAsynchronously(plugin, task)
            }
        }

        false -> Bukkit.getScheduler().run {
            when {
                interval != Duration.ZERO -> runTaskTimer(plugin, task, delay.toTicks(), interval.toTicks())
                delay != Duration.ZERO -> runTaskLater(plugin, task, delay.toTicks())
                else -> runTask(plugin, task)
            }
        }
    }
}