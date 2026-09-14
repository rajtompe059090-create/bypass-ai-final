package com.bypass.ai

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

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
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.6-flash:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
        
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun generateGeminiResponse(
    history: List<ChatMessage>,
    prompt: String,
    onStatusUpdate: ((String) -> Unit)? = null
): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY.trim()
    if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
        throw Exception("Invalid API key. Please configure GEMINI_API_KEY.")
    }
    
    val allContents = history.map {
        val role = if (it.isUser || it.isError) "user" else "model"
        val text = if (it.isError) "System Error: ${it.text}\nPlease fix this." else it.text
        Content(
            role = role,
            parts = listOf(Part(text = text))
        )
    }.toMutableList()
    
    if (prompt.isNotBlank()) {
        allContents.add(Content(role = "user", parts = listOf(Part(text = prompt))))
    }
    
    val request = GenerateContentRequest(
        contents = allContents,
        systemInstruction = Content(
            role = "system",
            parts = listOf(Part(text = """You are Bypass AI, an expert AI Coding Agent. You build, run, and preview complete applications (HTML/JS, Android, Python, Node, etc.).You must execute actions to fulfill the user's request. Do not just reply with code. Format actions in XML blocks:
<action>
type=CREATE_FILE
path=index.html
content=<!DOCTYPE html><html>...</html>
</action>
<action>
type=RUN_COMMAND
command=npm install
</action>
<action>
type=BUILD_PROJECT
</action>
<action>
type=PREVIEW_PROJECT
</action>

Supported types: 
- CREATE_FILE (requires path, content)
- UPDATE_FILE (requires path, content)
- DELETE_FILE (requires path)
- CREATE_FOLDER (requires path)
- READ_FILE (requires path)
- LIST_FILES (requires path)
- OPEN_FILE (requires path)
- RUN_COMMAND (requires command)
- BUILD_PROJECT
- ANALYZE_ERROR
- PREVIEW_PROJECT
- SEARCH_PROJECT (requires command as query)

Analyze the request, decide the stack (e.g. HTML/CSS/JS for basic web apps), create all necessary files, build if needed, and preview. Provide complete working code in the files.""".trimIndent()))
        )
    )
    
    var lastError: Exception? = null
    for (attempt in 1..3) {
        try {
            if (attempt == 1) {
                onStatusUpdate?.invoke("Connecting to Gemini...")
            } else {
                onStatusUpdate?.invoke("[AI] Retry $attempt/3")
                onStatusUpdate?.invoke("[AI] Retrying...")
            }
            onStatusUpdate?.invoke("Gemini thinking...")
            
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response text received."
            onStatusUpdate?.invoke("Gemini response received")
            return@withContext text
        } catch (e: Exception) {
            lastError = e
            val errorMsg = e.message?.lowercase() ?: "unknown"
            onStatusUpdate?.invoke("[AI] Request failed")
            onStatusUpdate?.invoke("[AI] Error: $errorMsg")
            if (attempt == 3) {
                onStatusUpdate?.invoke("[AI] FAILED after 3 attempts")
            } else {
                delay((1000L * attempt * attempt)) // bounded exponential backoff
            }
        }
    }
    throw Exception("API Error: timeout/network failure after 3 attempts. Last error: ${lastError?.message}")
}
