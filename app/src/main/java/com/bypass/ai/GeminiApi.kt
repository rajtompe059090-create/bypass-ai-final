package com.bypass.ai

import com.squareup.moshi.JsonClass
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun generateGeminiResponse(
    history: List<ChatMessage>,
    prompt: String
): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
        throw Exception("Invalid API key. Please configure GEMINI_API_KEY.")
    }
    
    val contents = history.filter { !it.isError && !it.isUser }.map {
        Content(
            role = "model",
            parts = listOf(Part(text = it.text))
        )
    }.toMutableList()
    
    // add user messages separately, ensuring alternating sequence if required, 
    // actually, let's map accurately:
    val allContents = history.filter { !it.isError }.map {
        Content(
            role = if (it.isUser) "user" else "model",
            parts = listOf(Part(text = it.text))
        )
    }.toMutableList()
    
    allContents.add(Content(role = "user", parts = listOf(Part(text = prompt))))

    val request = GenerateContentRequest(
        contents = allContents,
        systemInstruction = Content(
            role = "system",
            parts = listOf(Part(text = "You are Bypass AI, an expert Android Developer Assistant. You can help with Android development, Kotlin, Java, Gradle, debugging, code generation, project architecture, terminal commands, and file operations. You can produce structured actions to automate tasks in the IDE. Format actions precisely like this:\n\n<action>\ntype=create_file\npath=app/src/main/java/com/bypass/ai/Example.kt\ncontent=\n// code here\n</action>\n\nSupported types: create_file, update_file, delete_file, run_command, read_file. Keep your answers concise, practical, and helpful. Do not expose API keys."))
        )
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response text received."
    } catch (e: Exception) {
        throw Exception("API Error: ${e.message}")
    }
}
