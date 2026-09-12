package com.bypass.ai

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.bypass.ai.core.AgentViewModel
import com.bypass.ai.core.PreviewServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AgentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        PreviewServer.port = 8086
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        PreviewServer.stop()
    }

    @Test
    fun testCreateCalculatorApp() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AgentViewModel(app)
        
        viewModel.executePrompt("Create a very simple calculator app index.html, style.css and script.js")
        
        // Wait for the background work
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.messages.forEach {
            println("Message: ${it.text}")
        }
        
        // Assert the status
        assertTrue("Status is ${viewModel.status.value}", viewModel.status.value == "SUCCESS" || viewModel.status.value == "READY")
        
        // Assert files are created
        val projectDir = viewModel.currentProject.value
        assertTrue("Project dir must exist", projectDir != null && projectDir.exists())
        
        val files = projectDir?.listFiles() ?: emptyArray()
        assertTrue("index.html should exist", files.any { it.name == "index.html" })
        
        // Assert Preview server is running
        assertTrue("Server must be running", PreviewServer.isRunning)
        
        // Test preview server
        val url = URL("http://127.0.0.1:8086/index.html")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 2000
        val responseCode = conn.responseCode
        assertTrue("Preview server should return 200 OK", responseCode == 200)
        
        val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
        assertTrue("Response should not be empty", responseBody.isNotEmpty())
    }
}
