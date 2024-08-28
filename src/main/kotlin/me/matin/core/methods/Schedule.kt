@file:Suppress("unused")

package me.matin.core.methods

import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param async Whether the task should run asynchronously or not.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @return [BukkitTask] of the scheduled task.
 */
fun scheduleTask(
    plugin: Plugin,
    async: Boolean = false,
    delay: Duration = Duration.ZERO,
    interval: Duration = Duration.ZERO,
    task: () -> Unit
): BukkitTask = Bukkit.getScheduler().run {
    when (async) {
        true -> when {
            interval != Duration.ZERO -> runTaskTimerAsynchronously(
                plugin,
                task,
                delay.inWholeTicks,
                interval.inWholeTicks
            )

            delay != Duration.ZERO -> runTaskLaterAsynchronously(plugin, task, delay.inWholeTicks)
            else -> runTaskAsynchronously(plugin, task)
        }

        false -> when {
            interval != Duration.ZERO -> runTaskTimer(plugin, task, delay.inWholeTicks, interval.inWholeTicks)
            delay != Duration.ZERO -> runTaskLater(plugin, task, delay.inWholeTicks)
            else -> runTask(plugin, task)
        }
    }
}

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param async Whether the task should run asynchronously or not.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 */
fun schedule(
    plugin: Plugin,
    async: Boolean = false,
    delay: Duration = Duration.ZERO,
    interval: Duration = Duration.ZERO,
    task: () -> Unit
) {
    scheduleTask(plugin, async, delay, interval, task)
}

/**
 * Schedules given task for [Core] plugin using bukkit's task scheduler.
 *
 * @param async Whether the task should run asynchronously or not.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @return [BukkitTask] of the scheduled task.
 */
internal fun scheduleTask(
    async: Boolean = false,
    delay: Duration = Duration.ZERO,
    interval: Duration = Duration.ZERO,
    task: () -> Unit
): BukkitTask = scheduleTask(Core.instance, async, delay, interval, task)

/**
 * Schedules given task for [Core] plugin using bukkit's task scheduler.
 *
 * @param async Whether the task should run asynchronously or not.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 */
internal fun schedule(
    async: Boolean = false,
    delay: Duration = Duration.ZERO,
    interval: Duration = Duration.ZERO,
    task: () -> Unit
) {
    scheduleTask(Core.instance, async, delay, interval, task)
}