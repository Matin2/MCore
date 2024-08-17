@file:Suppress("unused")

package me.matin.core.managers

import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit

/** Returns an [Optional] representation of the object. */
val <T: Any> T?.optional get() = Optional.ofNullable(this)

/** Returns an [Optional] representation of the object. */
val <T: Any> T?.opt get() = Optional.ofNullable(this)

/** Converts this [Duration] value to server ticks. */
val Duration.ticks: Long
    get() {
        val seconds = this.toDouble(DurationUnit.SECONDS)
        return (seconds * 20).roundToLong()
    }

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