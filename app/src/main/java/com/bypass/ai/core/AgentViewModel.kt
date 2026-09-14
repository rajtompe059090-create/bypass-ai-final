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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.io.File

class AgentViewModel(application: Application) : AndroidViewModel(application) {
    val fileManager = FileManager(application)
    val messages = mutableStateListOf<ChatMessage>()
    var status = mutableStateOf("READY")
    var isBuilding = mutableStateOf(false)
    var fileRefreshTrigger = mutableStateOf(0)
    val currentProject = mutableStateOf<File?>(null)
    val terminalHistory = mutableStateListOf<String>()
    
    private var agentJob: Job? = null
    var autoRepairCount = 0

    init {
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
        agentJob?.cancel()
    }
    
    fun cancelAgent() {
        agentJob?.cancel()
        isBuilding.value = false
        status.value = "CANCELLED"
        terminalHistory.add("[AGENT] Execution cancelled by user.")
        messages.add(ChatMessage("Execution cancelled.", isUser = false, isError = true))
    }

    fun executePrompt(prompt: String, isAutoRepair: Boolean = false) {
        if (isBuilding.value) return // Prevent concurrency
        
        if (!isAutoRepair) {
            autoRepairCount = 0
        }
        
        messages.add(ChatMessage(prompt, isUser = true))
        status.value = "PLANNING"
        isBuilding.value = true
        terminalHistory.add("[AGENT] Planning project...")
        
        agentJob = viewModelScope.launch {
            var retries = 0
            var success = false
            while (retries < 3 && !success) {
                try {
                    status.value = if (retries == 0) "AI_REQUEST" else "REPAIRING"
                    
                    val response = generateGeminiResponse(
                        history = messages.toList(),
                        prompt = ""
                    ) { statusUpdate ->
                        // Only add Gemini specific updates if it's the network retries or key updates
                        terminalHistory.add(statusUpdate)
                    }
                    
                    val (textBody, actions) = ActionParser.parseResponse(response)
                    
                    if (textBody.isNotBlank()) {
                        messages.add(ChatMessage(textBody, isUser = false))
                    }
                    
                    status.value = "EXECUTING"
                    for (action in actions) {
                        executeAction(action)
                    }
                    
                    messages.add(ChatMessage("Execution complete. Preview updated.", isUser = false))
                    status.value = "SUCCESS"
                    success = true
                } catch (e: CancellationException) {
                    throw e // Let it propagate for clean cancellation
                } catch (e: Exception) {
                    retries++
                    val errorMsg = e.message ?: "Unknown error"
                    terminalHistory.add("[ERROR] $errorMsg")
                    messages.add(ChatMessage("Error: $errorMsg", isUser = false, isError = true))
                    if (retries >= 3) {
                        status.value = "ERROR"
                        terminalHistory.add("[AGENT] FAILED after 3 repair attempts")
                    } else {
                        terminalHistory.add("[AGENT] Attempting self-repair (Attempt $retries/3)")
                    }
                }
            }
            isBuilding.value = false
        }
    }
    
    fun sendError(error: String) {
        if (autoRepairCount < 3) {
            autoRepairCount++
            messages.add(ChatMessage("Runtime Error: $error\nPlease provide a fix.", isUser = true))
            executePrompt("Runtime Error: $error\nPlease provide a fix.", isAutoRepair = true)
        } else {
            terminalHistory.add("[AGENT] FAILED: Maximum auto-repair attempts reached.")
        }
    }

    private suspend fun executeAction(action: AiAction) {
        val projectDir = currentProject.value ?: return
        val type = action.type.uppercase()
        when (type) {
            "CREATE_FILE", "UPDATE_FILE" -> {
                if (action.path != null && action.content != null) {
                    val file = File(projectDir, action.path)
                    fileManager.writeFile(file, action.content)
                    terminalHistory.add("[FILE] ${if (type == "CREATE_FILE") "Creating" else "Updating"} ${action.path}")
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
                    if (output.contains("Error:", ignoreCase = true) || output.contains("FAILED", ignoreCase = true)) {
                        throw Exception("Command failed: $output")
                    }
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
                    terminalHistory.add("[FILE] Created/Updated ${action.path}")
                }
            }
        }
    }
}
