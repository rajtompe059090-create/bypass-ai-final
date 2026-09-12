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
        
        viewModel.executePrompt("Create a very simple index.html and style.css file with h1 hello")
        
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
    }
}
