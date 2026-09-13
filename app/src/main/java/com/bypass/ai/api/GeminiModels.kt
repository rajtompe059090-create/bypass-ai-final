package com.bypass.ai.api

import com.squareup.moshi.Json
import java.io.IOException

// ============================================================
// GEMINI API REQUEST/RESPONSE MODELS
// ============================================================

data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)

data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
    @Json(name = "safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)

data class SafetyRating(
    val category: String? = null,
    val probability: String? = null
)

data class ApiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// ============================================================
// STRUCTURED RESULT TYPES
// ============================================================

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Error(
        val type: ErrorType,
        val message: String,
        val httpStatus: Int? = null,
        val retryCount: Int = 0,
        val retryable: Boolean = false,
        val cause: Throwable? = null
    ) : GeminiResult()
}

enum class ErrorType {
    INVALID_API_KEY,
    NETWORK_ERROR,
    TIMEOUT,
    HTTP_ERROR,
    EMPTY_RESPONSE,
    MALFORMED_RESPONSE,
    CANDIDATES_MISSING,
    CONTENT_MISSING,
    PARTS_MISSING,
    TEXT_MISSING,
    JSON_PARSE_ERROR,
    UNKNOWN
}

// ============================================================
// STRUCTURED ACTION PROTOCOL
// ============================================================

data class AgentActionResponse(
    val message: String,
    val actions: List<AgentAction> = emptyList()
)

sealed class AgentAction {
    abstract val type: String

    data class CreateFile(
        override val type: String = "CREATE_FILE",
        val path: String,
        val content: String
    ) : AgentAction()

    data class UpdateFile(
        override val type: String = "UPDATE_FILE",
        val path: String,
        val content: String
    ) : AgentAction()

    data class DeleteFile(
        override val type: String = "DELETE_FILE",
        val path: String
    ) : AgentAction()

    data class CreateFolder(
        override val type: String = "CREATE_FOLDER",
        val path: String
    ) : AgentAction()

    data class ReadFile(
        override val type: String = "READ_FILE",
        val path: String
    ) : AgentAction()

    data class ListFiles(
        override val type: String = "LIST_FILES",
        val path: String
    ) : AgentAction()

    data class OpenFile(
        override val type: String = "OPEN_FILE",
        val path: String
    ) : AgentAction()

    data class RunCommand(
        override val type: String = "RUN_COMMAND",
        val command: String
    ) : AgentAction()

    data class BuildProject(
        override val type: String = "BUILD_PROJECT"
    ) : AgentAction()

    data class AnalyzeError(
        override val type: String = "ANALYZE_ERROR",
        val error: String
    ) : AgentAction()

    data class PreviewProject(
        override val type: String = "PREVIEW_PROJECT"
    ) : AgentAction()

    data class SearchProject(
        override val type: String = "SEARCH_PROJECT",
        val query: String
    ) : AgentAction()
}

// ============================================================
// ACTION EXECUTION RESULT
// ============================================================

sealed class ActionResult {
    data class Success(
        val action: String,
        val path: String? = null,
        val output: String? = null
    ) : ActionResult()

    data class Failure(
        val action: String,
        val path: String? = null,
        val exitCode: Int? = null,
        val stdout: String? = null,
        val stderr: String? = null,
        val error: String
    ) : ActionResult()
}
