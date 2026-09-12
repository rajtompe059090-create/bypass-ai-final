package com.bypass.ai.core

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bypass.ai.ChatMessage
import com.bypass.ai.ai.ActionParser
import com.bypass.ai.ai.AiAction
import com.bypass.ai.generateGeminiResponse
import kotlinx.coroutines.launch
import java.io.File

class AgentViewModel(application: Application) : AndroidViewModel(application) {
    val fileManager = FileManager(application)
    val messages = mutableStateListOf<ChatMessage>()
    var status = mutableStateOf("READY")
    var isBuilding = mutableStateOf(false)

    val currentProject = mutableStateOf<File?>(null)

    init {
        // Create default workspace if none
        val project = fileManager.createProject("calculator-app")
        currentProject.value = project ?: File(fileManager.workspaceDir, "calculator-app")
        PreviewServer.start(currentProject.value!!)
    }

    fun executePrompt(prompt: String) {
        messages.add(ChatMessage(prompt, isUser = true))
        status.value = "BUILDING"
        isBuilding.value = true

        viewModelScope.launch {
            var retries = 0
            var success = false

            while (retries < 2 && !success) {
                try {
                    val response = generateGeminiResponse(messages.toList(), prompt)
                    val (textBody, actions) = ActionParser.parseResponse(response)
                    
                    if (textBody.isNotBlank()) {
                        messages.add(ChatMessage(textBody, isUser = false))
                    }

                    for (action in actions) {
                        executeAction(action)
                    }
                    
                    // Add success message
                    messages.add(ChatMessage("Execution complete. Preview updated.", isUser = false))
                    status.value = "SUCCESS"
                    success = true
                } catch (e: Exception) {
                    retries++
                    messages.add(ChatMessage("Error: ${e.message}", isUser = false, isError = true))
                    if (retries >= 2) {
                        status.value = "ERROR"
                    }
                }
            }
            isBuilding.value = false
            if (status.value != "ERROR") {
                // status.value = "READY"
            }
        }
    }
    
    fun sendError(error: String) {
         messages.add(ChatMessage("Runtime Error: $error\nPlease provide a fix.", isUser = true))
         executePrompt("Runtime Error: $error\nPlease provide a fix.")
    }

    private suspend fun executeAction(action: AiAction) {
        val projectDir = currentProject.value ?: return
        when (action.type) {
            "create_file", "update_file" -> {
                if (action.path != null && action.content != null) {
                    val file = File(projectDir, action.path)
                    fileManager.writeFile(file, action.content)
                }
            }
            "delete_file" -> {
                if (action.path != null) {
                    fileManager.deleteFile(File(projectDir, action.path))
                }
            }
            "run_command" -> {
                if (action.command != null) {
                    TerminalRunner.runCommand(action.command, projectDir)
                }
            }
        }
    }
}
