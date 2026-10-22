package com.example.cse226ca2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloadSimulatorScreen()
                }
            }
        }
    }
}

@Composable
fun DownloadSimulatorScreen() {
    val totalSizeMb = 100
    var downloadedMb by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Idle") }

    val scope = rememberCoroutineScope()
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        isDownloading = false
        downloadedMb = 0
        statusText = "Download Cancelled - Bandwidth Freed"
    }

    fun startDownload() {
        downloadJob?.cancel()
        isDownloading = true
        statusText = "Downloading 100MB file..."

        downloadJob = scope.launch(Dispatchers.Default) {
            try {
                while (isActive) {
                    for (mb in 1..totalSizeMb) {
                        ensureActive()
                        yield()

                        downloadedMb = mb
                        delay(50)
                    }
                    downloadedMb = 0
                }
            } catch (e: CancellationException) {
                withContext(NonCancellable + Dispatchers.Main) {
                    downloadedMb = 0
                    isDownloading = false
                    statusText = "Download Cancelled - Bandwidth Freed"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cloud Storage Downloader", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(statusText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))

        val progress = downloadedMb.toFloat() / totalSizeMb
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("$downloadedMb MB / $totalSizeMb MB (${(progress * 100).toInt()}%)")
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { startDownload() },
                enabled = !isDownloading
            ) {
                Text("Start Download")
            }

            Button(
                onClick = { cancelDownload() },
                enabled = isDownloading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel Download")
            }
        }
    }
}
