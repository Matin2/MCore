package me.matin.core.managers.menu.utils

import me.matin.core.Core
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class MenuUtils {

    private val runningTasks = mutableSetOf<BukkitTask>()
    private val tasksToRun: MutableList<Triple<Boolean, Pair<Duration, Duration>, () -> Unit>> = mutableListOf()
    var open = false

    fun scheduleOnOpen() {
        for ((async, delayInterval, action) in tasksToRun) {
            val task = Core.scheduleTask(async, delayInterval.first, delayInterval.second, action)
            runningTasks.add(task)
        }
        tasksToRun.clear()
    }

    fun removeTasks() {
        runningTasks.forEach {
            it.cancel()
            runningTasks.remove(it)
        }
    }

    fun scheduleTask(
        async: Boolean = false, delay: Duration = Duration.ZERO, interval: Duration = Duration.ZERO, task: () -> Unit
    ) {
        if (open) {
            tasksToRun.add(Triple(async, delay to interval, task))
            return
        }
        val bukkitTask = Core.scheduleTask(async, delay, interval, task)
        runningTasks.add(bukkitTask)
    }
}