package com.bypass.ai.ai

/**
 * Validates actions for security and correctness.
 */
object ActionValidator {
    private val FORBIDDEN_PATHS = setOf(
        "..",
        "/",
        "/system",
        "/data",
        "/proc",
        "/sys",
        "/root",
        "/etc",
        "/boot"
    )

    private val FORBIDDEN_COMMANDS = setOf(
        "sudo",
        "su",
        "chmod",
        "chown",
        "rm -rf",
        "dd",
        "mkfs",
        "shutdown",
        "reboot",
        "halt",
        "poweroff"
    )

    /**
     * Validate a path for security and correctness.
     * Returns null if valid, or an error message if invalid.
     */
    fun validatePath(path: String?): String? {
        if (path == null || path.isBlank()) {
            return "Path is required"
        }

        // Check for path traversal
        if (path.contains("..")) {
            return "Path traversal detected: $path"
        }

        // Check for absolute paths outside project
        if (path.startsWith("/")) {
            return "Absolute paths are not allowed: $path"
        }

        // Check for forbidden system paths
        for (forbidden in FORBIDDEN_PATHS) {
            if (path.contains(forbidden)) {
                return "Access to system path is forbidden: $path"
            }
        }

        return null // Valid
    }

    /**
     * Validate a command for safety.
     * Returns null if valid, or an error message if invalid.
     */
    fun validateCommand(command: String?): String? {
        if (command == null || command.isBlank()) {
            return "Command is required"
        }

        val lowerCommand = command.lowercase()

        // Check for forbidden commands
        for (forbidden in FORBIDDEN_COMMANDS) {
            if (lowerCommand.contains(forbidden.lowercase())) {
                return "Command is not allowed: $forbidden"
            }
        }

        // Check for credential theft attempts
        if (lowerCommand.contains("env") && (
            lowerCommand.contains("api") ||
            lowerCommand.contains("key") ||
            lowerCommand.contains("secret") ||
            lowerCommand.contains("password")
        )) {
            return "Credential access is not allowed"
        }

        return null // Valid
    }
}

/**
 * Result of parsing actions from agent response.
 */
sealed class ActionParseResult {
    data class Success(
        val message: String,
        val actions: List<com.bypass.ai.api.AgentAction>
    ) : ActionParseResult()

    data class Error(
        val message: String,
        val parseErrors: List<String> = emptyList()
    ) : ActionParseResult()
}

/**
 * Parses structured JSON actions from Gemini responses.
 */
object ActionParser {
    /**
     * Parse a JSON response containing message and actions.
     * Handles both JSON and markdown code fence formats.
     */
    fun parseResponse(response: String): ActionParseResult {
        // Clean markdown code fences if present
        val cleaned = removeMarkdownFences(response)

        // Try to parse JSON
        val json = try {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(com.bypass.ai.api.AgentActionResponse::class.java)
            adapter.fromJson(cleaned)
        } catch (e: Exception) {
            return ActionParseResult.Error(
                message = "Failed to parse JSON: ${e.message}",
                parseErrors = listOf(e.message ?: "Unknown error")
            )
        }

        if (json == null) {
            return ActionParseResult.Error("Response JSON is null")
        }

        // Validate and convert actions
        val validationErrors = mutableListOf<String>()
        val validActions = mutableListOf<com.bypass.ai.api.AgentAction>()

        for ((index, action) in json.actions.withIndex()) {
            val error = validateAction(action, index)
            if (error != null) {
                validationErrors.add(error)
            } else {
                validActions.add(action)
            }
        }

        return if (validationErrors.isNotEmpty() && validActions.isEmpty()) {
            ActionParseResult.Error(
                message = "All actions failed validation",
                parseErrors = validationErrors
            )
        } else {
            ActionParseResult.Success(
                message = json.message,
                actions = validActions
            )
        }
    }

    /**
     * Remove markdown code fences from response.
     */
    private fun removeMarkdownFences(text: String): String {
        return text
            .replace(Regex("^\\s*```(?:json)?\\s*"), "")
            .replace(Regex("\\s*```\\s*$"), "")
    }

    /**
     * Validate a single action.
     * Returns error message if invalid, null if valid.
     */
    private fun validateAction(action: com.bypass.ai.api.AgentAction, index: Int): String? {
        return when (action) {
            is com.bypass.ai.api.AgentAction.CreateFile -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                if (action.content.isBlank()) return "Action $index: content cannot be empty"
                null
            }
            is com.bypass.ai.api.AgentAction.UpdateFile -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                if (action.content.isBlank()) return "Action $index: content cannot be empty"
                null
            }
            is com.bypass.ai.api.AgentAction.DeleteFile -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                null
            }
            is com.bypass.ai.api.AgentAction.CreateFolder -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                null
            }
            is com.bypass.ai.api.AgentAction.ReadFile -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                null
            }
            is com.bypass.ai.api.AgentAction.ListFiles -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                null
            }
            is com.bypass.ai.api.AgentAction.OpenFile -> {
                val pathError = ActionValidator.validatePath(action.path)
                if (pathError != null) return "Action $index: $pathError"
                null
            }
            is com.bypass.ai.api.AgentAction.RunCommand -> {
                val cmdError = ActionValidator.validateCommand(action.command)
                if (cmdError != null) return "Action $index: $cmdError"
                null
            }
            is com.bypass.ai.api.AgentAction.AnalyzeError -> {
                if (action.error.isBlank()) return "Action $index: error description required"
                null
            }
            is com.bypass.ai.api.AgentAction.SearchProject -> {
                if (action.query.isBlank()) return "Action $index: search query required"
                null
            }
            else -> null // BuildProject and PreviewProject have no validation
        }
    }
}
