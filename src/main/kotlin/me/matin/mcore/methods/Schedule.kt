@file:Suppress("unused")

package me.matin.mcore.methods

import me.matin.mcore.MCore
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import tsp.helperlite.Schedulers.async
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @return [BukkitTask] of the scheduled task.
 */
fun syncedTask(
	plugin: Plugin,
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
): BukkitTask = Bukkit.getScheduler().run {
	when {
		interval != Duration.ZERO -> runTaskTimer(
			plugin,
			{ -> task(MThread) },
			delay.inWholeTicks,
			interval.inWholeTicks
		)
		
		delay != Duration.ZERO -> runTaskLater(plugin, { -> task(MThread) }, delay.inWholeTicks)
		else -> runTask(plugin) { -> task(MThread) }
	}
}

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @return [BukkitTask] of the scheduled task.
 */
fun asyncedTask(
	plugin: Plugin,
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
): BukkitTask = Bukkit.getScheduler().run {
	when {
		interval != Duration.ZERO -> runTaskTimerAsynchronously(
			plugin,
			{ -> task(MThread) },
			delay.inWholeTicks,
			interval.inWholeTicks
		)
		
		delay != Duration.ZERO -> runTaskLaterAsynchronously(plugin, { -> task(MThread) }, delay.inWholeTicks)
		else -> runTaskAsynchronously(plugin) { -> task(MThread) }
	}
}

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 * @param async Whether the task should run asynchronously or not.
 */
fun sync(
	plugin: Plugin,
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
) {
	syncedTask(plugin, delay, interval, task)
}

/**
 * Schedules given task using bukkit's task scheduler.
 *
 * @param plugin Plugin witch the task is scheduled for.
 * @param delay Task will run after this delay.
 * @param interval Task will run repeatedly with this interval.
 * @param task Task witch is scheduled.
 */
fun async(
	plugin: Plugin,
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
) {
	asyncedTask(plugin, delay, interval, task)
}

internal fun syncedTask(
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
): BukkitTask = syncedTask(MCore.instance, delay, interval, task)

internal fun asyncedTask(
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
): BukkitTask = asyncedTask(MCore.instance, delay, interval, task)

internal fun sync(
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
) {
	sync(MCore.instance, delay, interval, task)
}

internal fun async(
	delay: Duration = Duration.ZERO,
	interval: Duration = Duration.ZERO,
	task: (MThread) -> Unit,
) {
	async(MCore.instance, delay, interval, task)
}

object MThread {
	
	fun pause(duration: Duration) {
		runCatching { Thread.sleep(duration.toJavaDuration()) }
	}
	
	fun pauseWhile(condition: Boolean, duration: Duration) {
		while (condition) runCatching { Thread.sleep(duration.toJavaDuration()) }
	}
}