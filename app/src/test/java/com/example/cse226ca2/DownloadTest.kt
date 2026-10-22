package com.example.cse226ca2

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DownloadTest {

    @Test
    fun testCooperativeCancellationHaltsJob() = runTest {
        var downloadedMb = 0
        val totalSizeMb = 100

        val job = launch(Dispatchers.Default) {
            try {
                while (isActive) {
                    for (mb in 1..totalSizeMb) {
                        ensureActive()
                        yield()
                        downloadedMb = mb
                        delay(50)
                    }
                }
            } catch (e: CancellationException) {
                downloadedMb = 0
            }
        }

        delay(120)
        assertTrue("Download should make progress", downloadedMb > 0)

        job.cancelAndJoin()

        assertTrue("Job must be cancelled", job.isCancelled)
        assertEquals("Downloaded MB should reset to 0 after cancel", 0, downloadedMb)
    }
}
