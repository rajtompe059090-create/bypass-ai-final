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

    var fileRefreshTrigger = mutableStateOf(0)

    val currentProject = mutableStateOf<File?>(null)

    val terminalHistory = mutableStateListOf<String>()

    init {
        // Create default workspace if none
        val project = fileManager.createProject("calculator-app")
        currentProject.value = project ?: File(fileManager.workspaceDir, "calculator-app")
        PreviewServer.start(currentProject.value!!)
        
        terminalHistory.addAll(listOf(
            "[SYSTEM] Bypass IDE Root Subsystem Initialized",
            "[WORKSPACE] ${fileManager.workspaceDir.absolutePath}",
            "[PROJECT DIR] ${currentProject.value?.absolutePath}"
        ))
    }

    override fun onCleared() {
        super.onCleared()
        PreviewServer.stop()
    }

    fun executePrompt(prompt: String) {
        messages.add(ChatMessage(prompt, isUser = true))
        status.value = "PLANNING"
        isBuilding.value = true
        terminalHistory.add("[AGENT] Planning...")

        viewModelScope.launch {
            var retries = 0
            var success = false

            while (retries < 3 && !success) {
                try {
                    val response = generateGeminiResponse(messages.toList(), "")
                    val (textBody, actions) = ActionParser.parseResponse(response)
                    
                    if (textBody.isNotBlank()) {
                        messages.add(ChatMessage(textBody, isUser = false))
                    }

                    status.value = "EXECUTING"
                    for (action in actions) {
                        executeAction(action)
                    }
                    
                    // Add success message
                    messages.add(ChatMessage("Execution complete. Preview updated.", isUser = false))
                    status.value = "SUCCESS"
                    success = true
                } catch (e: Exception) {
                    retries++
                    val errorMsg = e.message ?: "Unknown error"
                    terminalHistory.add("[ERROR] $errorMsg")
                    messages.add(ChatMessage("Error: $errorMsg", isUser = false, isError = true))
                    if (retries >= 3) {
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
        val type = action.type.uppercase()
        when (type) {
            "CREATE_FILE", "UPDATE_FILE" -> {
                if (action.path != null && action.content != null) {
                    val file = File(projectDir, action.path)
                    fileManager.writeFile(file, action.content)
                    terminalHistory.add("[FILE] Created ${action.path}")
                    fileRefreshTrigger.value++
                }
            }
            "DELETE_FILE" -> {
                if (action.path != null) {
                    fileManager.deleteFile(File(projectDir, action.path))
                    terminalHistory.add("[FILE] Deleted ${action.path}")
                    fileRefreshTrigger.value++
                }
            }
            "CREATE_FOLDER" -> {
                if (action.path != null) {
                    File(projectDir, action.path).mkdirs()
                    terminalHistory.add("[FILE] Created folder ${action.path}")
                    fileRefreshTrigger.value++
                }
            }
            "RUN_COMMAND" -> {
                if (action.command != null) {
                    terminalHistory.add("[COMMAND] ${action.command}")
                    val output = TerminalRunner.runCommand(action.command, projectDir)
                    terminalHistory.add(output)
                }
            }
            "BUILD_PROJECT" -> {
                status.value = "BUILDING"
                terminalHistory.add("[BUILD] Building project...")
                val isAndroid = File(projectDir, "build.gradle").exists() || File(projectDir, "build.gradle.kts").exists()
                if (isAndroid) {
                    val cmd = if (File(projectDir, "gradlew").exists()) "./gradlew assembleDebug" else "gradle assembleDebug"
                    val output = TerminalRunner.runCommand(cmd, projectDir)
                    terminalHistory.add(output)
                    if (output.contains("FAILED") || output.contains("Exception")) {
                        throw Exception("Build failed: $output")
                    }
                } else {
                    terminalHistory.add("[BUILD] No build required for this project type.")
                }
            }
            "PREVIEW_PROJECT" -> {
                status.value = "PREVIEWING"
                terminalHistory.add("[PREVIEW] Server started")
                PreviewServer.start(projectDir)
            }
            // fallback
            "create_file", "update_file" -> {
                if (action.path != null && action.content != null) {
                    val file = File(projectDir, action.path)
                    fileManager.writeFile(file, action.content)
                    terminalHistory.add("[FILE] Created ${action.path}")
                }
            }
            "delete_file" -> {
                if (action.path != null) {
                    fileManager.deleteFile(File(projectDir, action.path))
                    terminalHistory.add("[FILE] Deleted ${action.path}")
                }
            }
            "run_command" -> {
                if (action.command != null) {
                    terminalHistory.add("[COMMAND] ${action.command}")
                    val output = TerminalRunner.runCommand(action.command, projectDir)
                    terminalHistory.add(output)
                }
            }
        }
    }
}
