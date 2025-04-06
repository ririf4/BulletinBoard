package net.ririfa.bulletinboard

import org.bukkit.Bukkit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

object BExecutor {
    private val isShutdown = AtomicBoolean(false)

    fun <T> execute(task: () -> T): T {
        shutdownCheck()
        val resultHolder = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)

        Bukkit.getScheduler().runTask(Plugin, Runnable {
            try {
                resultHolder[0] = task()
            } finally {
                latch.countDown()
            }
        })

        latch.await()
        @Suppress("UNCHECKED_CAST")
        return resultHolder[0] as T
    }

    fun shutdownCheck() {
        if (isShutdown.get()) {
            throw IllegalStateException("S1Executor is shut down and cannot accept new tasks.")
        }
    }
}