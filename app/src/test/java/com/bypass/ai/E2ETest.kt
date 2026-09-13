package com.bypass.ai

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.bypass.ai.core.AgentViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.junit.Assert.assertTrue
import java.io.File
import com.bypass.ai.core.PreviewServer

@RunWith(RobolectricTestRunner::class)
class E2ETest {
    @Test
    fun testCalculatorAppCreation() {
        PreviewServer.port = 8085 // use a different port to avoid BindException
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AgentViewModel(app)
        
        println("Sending prompt to agent...")
        viewModel.executePrompt("Create a basic calculator web app using HTML, CSS and Javascript.")
        
        var retries = 0
        while (viewModel.isBuilding.value && retries < 120) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(500)
            retries++
            println("Status: ${viewModel.status.value}")
        }
        
        println("Final Status: ${viewModel.status.value}")
        for (log in viewModel.terminalHistory) {
            println(log)
        }
        
        assertTrue("Status should be SUCCESS", viewModel.status.value == "SUCCESS")
        
        val files = viewModel.fileManager.listFiles(viewModel.currentProject.value!!)
        println("Generated files:")
        for (f in files) {
            println("- ${f.name}")
        }
        assertTrue("Should have created files", files.isNotEmpty())
    }
}
