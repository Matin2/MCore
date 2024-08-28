package me.matin.core.managers.menu.utils

import me.matin.core.methods.scheduleTask
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class MenuScheduler {

    private val runningTasks = mutableSetOf<BukkitTask>()
    private val tasksToRun: MutableList<Task> = mutableListOf()
    private var open = false

    fun onOpen() {
        open = true
        for ((async, delay, interval, action) in tasksToRun) {
            val task = scheduleTask(async, delay, interval, action)
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
            tasksToRun.add(Task(async, delay, interval, task))
            return
        }
        val bukkitTask = scheduleTask(async, delay, interval, task)
        runningTasks.add(bukkitTask)
    }

    private data class Task(val async: Boolean, val delay: Duration, val interval: Duration, val task: () -> Unit)
}