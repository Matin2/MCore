package me.matin.core.managers.menu.utils

import me.matin.core.Core
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class MenuScheduler {

    private val runningTasks = mutableSetOf<BukkitTask>()
    private val tasksToRun: MutableList<Triple<Boolean, Pair<Duration, Duration>, () -> Unit>> = mutableListOf()
    var open = false

    fun onOpen() {
        open = true
        for ((async, delayInterval, action) in tasksToRun) {
            val task = Core.scheduleTask(async, delayInterval.first, delayInterval.second, action)
            runningTasks.add(task)
        }
        tasksToRun.clear()
    }

    fun onClose() {
        open = false
        runningTasks.forEach {
            it.cancel()
            runningTasks.remove(it)
        }
    }

    fun schedule(async: Boolean, delay: Duration, interval: Duration, task: () -> Unit) {
        if (open) {
            tasksToRun.add(Triple(async, delay to interval, task))
            return
        }
        val bukkitTask = Core.scheduleTask(async, delay, interval, task)
        runningTasks.add(bukkitTask)
    }
}