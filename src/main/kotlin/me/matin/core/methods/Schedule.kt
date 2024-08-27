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
fun schedule(
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
 * Schedules given task for [Core] plugin using bukkit's task scheduler.
 *
 * @param async Whether the task should run asynchronously or not.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @return [BukkitTask] of the scheduled task.
 */
internal fun schedule(
    async: Boolean = false,
    delay: Duration = Duration.ZERO,
    interval: Duration = Duration.ZERO,
    task: () -> Unit
): BukkitTask = schedule(Core.instance, async, delay, interval, task)

/** Returns a readable text representation of this duration. */
fun Duration.text(separator: String = " "): String = buildString {
    this@text.toComponents { days, hours, minutes, seconds, nanos ->
        val millis = nanos / 1_000_000
        append("${separator + days}d")
        addTime(days, "d", separator)
        addTime(hours, "h", separator)
        addTime(minutes, "m", separator)
        addTime(seconds, "s", separator)
        addTime(millis, "ms", separator)
        if (toString().isBlank()) append("0ms")
    }
}.removeSuffix(separator)

private fun <T: Number> StringBuilder.addTime(
    time: T,
    suffix: String,
    separator: String
): StringBuilder? = time.takeIf { it.toInt() > 0 }?.let {
    this.append(time.toString() + suffix + separator)
}