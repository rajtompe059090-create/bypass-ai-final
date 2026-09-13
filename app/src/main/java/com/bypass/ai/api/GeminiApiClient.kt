package com.bypass.ai.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.pow

/**
 * Retrofit service interface for Gemini API.
 * Uses x-goog-api-key header for authentication.
 */
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.6-flash:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

/**
 * Singleton for managing Retrofit client and OkHttp configuration.
 * Provides timeouts and connection pooling.
 */
object RetrofitClientFactory {
    private const val CONNECTION_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 60L
    private const val WRITE_TIMEOUT_SECONDS = 60L
    private const val CALL_TIMEOUT_SECONDS = 120L

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
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

/**
 * API key provider interface for isolating key access.
 * Allows secure backend migration without rewriting agent logic.
 */
interface ApiKeyProvider {
    fun getApiKey(): String
}

/**
 * Default implementation: reads from BuildConfig.
 * Can be replaced with backend-based provider later.
 */
class BuildConfigApiKeyProvider : ApiKeyProvider {
    override fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * Main Gemini API client with comprehensive error handling and retry logic.
 */
object GeminiApiClient {
    private const val MAX_RETRIES = 3
    private const val INITIAL_BACKOFF_MS = 100L
    private const val MAX_BACKOFF_MS = 5000L

    private var apiKeyProvider: ApiKeyProvider = BuildConfigApiKeyProvider()

    /**
     * Inject a custom API key provider (e.g., for backend-based keys).
     */
    fun setApiKeyProvider(provider: ApiKeyProvider) {
        apiKeyProvider = provider
    }

    /**
     * Generate content from Gemini API with retry and error handling.
     *
     * @param request The Gemini API request
     * @return GeminiResult.Success or GeminiResult.Error
     */
    suspend fun generateContent(request: GenerateContentRequest): GeminiResult =
        withContext(Dispatchers.IO) {
            val apiKey = apiKeyProvider.getApiKey()

            // Validate API key before making requests
            val keyValidation = validateApiKey(apiKey)
            if (keyValidation is GeminiResult.Error) {
                return@withContext keyValidation
            }

            // Attempt with retries and exponential backoff
            var lastError: GeminiResult.Error? = null
            for (attempt in 0 until MAX_RETRIES) {
                val result = attemptGenerateContent(apiKey, request)

                when (result) {
                    is GeminiResult.Success -> return@withContext result
                    is GeminiResult.Error -> {
                        lastError = result.copy(retryCount = attempt)
                        if (!result.retryable || attempt >= MAX_RETRIES - 1) {
                            return@withContext result.copy(retryCount = attempt + 1)
                        }
                        // Apply exponential backoff before retry
                        val backoffMs = calculateBackoff(attempt)
                        kotlinx.coroutines.delay(backoffMs)
                    }
                }
            }

            // Should not reach here, but return last error if it does
            lastError ?: GeminiResult.Error(
                type = ErrorType.UNKNOWN,
                message = "Unexpected error after retries",
                retryCount = MAX_RETRIES,
                retryable = false
            )
        }

    /**
     * Single attempt to generate content.
     */
    private suspend fun attemptGenerateContent(
        apiKey: String,
        request: GenerateContentRequest
    ): GeminiResult = try {
        val response = RetrofitClientFactory.service.generateContent(apiKey, request)
        parseResponse(response)
    } catch (e: SocketTimeoutException) {
        GeminiResult.Error(
            type = ErrorType.TIMEOUT,
            message = "Request timeout: ${e.message}",
            retryable = true,
            cause = e
        )
    } catch (e: IOException) {
        GeminiResult.Error(
            type = ErrorType.NETWORK_ERROR,
            message = "Network error: ${e.message}",
            retryable = true,
            cause = e
        )
    } catch (e: HttpException) {
        val httpError = parseHttpError(e)
        httpError
    } catch (e: Exception) {
        val errorType = when {
            e.message?.contains("JSON") == true -> ErrorType.JSON_PARSE_ERROR
            else -> ErrorType.UNKNOWN
        }
        GeminiResult.Error(
            type = errorType,
            message = "Unexpected error: ${e.message}",
            retryable = false,
            cause = e
        )
    }

    /**
     * Parse a successful Gemini API response.
     */
    private fun parseResponse(response: GenerateContentResponse): GeminiResult {
        // Check for API error in response
        response.error?.let {
            return GeminiResult.Error(
                type = ErrorType.HTTP_ERROR,
                message = it.message ?: "API returned error",
                httpStatus = it.code,
                retryable = (it.code ?: 0) >= 500 // Retry on 5xx
            )
        }

        // Validate structure
        val candidates = response.candidates
        if (candidates == null || candidates.isEmpty()) {
            return GeminiResult.Error(
                type = ErrorType.CANDIDATES_MISSING,
                message = "Response contains no candidates",
                retryable = false
            )
        }

        val content = candidates.firstOrNull()?.content
        if (content == null) {
            return GeminiResult.Error(
                type = ErrorType.CONTENT_MISSING,
                message = "Candidate content is missing",
                retryable = false
            )
        }

        val parts = content.parts
        if (parts == null || parts.isEmpty()) {
            return GeminiResult.Error(
                type = ErrorType.PARTS_MISSING,
                message = "Content contains no parts",
                retryable = false
            )
        }

        val text = parts.firstOrNull()?.text
        if (text == null || text.isBlank()) {
            return GeminiResult.Error(
                type = ErrorType.TEXT_MISSING,
                message = "Text part is missing or empty",
                retryable = false
            )
        }

        return GeminiResult.Success(text)
    }

    /**
     * Parse HTTP exceptions and map to GeminiResult.Error.
     */
    private fun parseHttpError(e: HttpException): GeminiResult.Error {
        val statusCode = e.code()
        val errorType = when (statusCode) {
            400 -> ErrorType.HTTP_ERROR
            401 -> ErrorType.INVALID_API_KEY
            429, 503 -> ErrorType.NETWORK_ERROR // Rate limit or service unavailable
            else -> ErrorType.HTTP_ERROR
        }
        val retryable = statusCode >= 500 || statusCode == 429
        return GeminiResult.Error(
            type = errorType,
            message = "HTTP $statusCode: ${e.message}",
            httpStatus = statusCode,
            retryable = retryable
        )
    }

    /**
     * Validate API key format and presence.
     */
    private fun validateApiKey(apiKey: String): GeminiResult? {
        return when {
            apiKey.isBlank() -> GeminiResult.Error(
                type = ErrorType.INVALID_API_KEY,
                message = "API key is not configured",
                retryable = false
            )
            apiKey == "YOUR_GEMINI_API_KEY" || apiKey.contains("GEMINI_API_KEY") -> {
                GeminiResult.Error(
                    type = ErrorType.INVALID_API_KEY,
                    message = "API key is not set. Configure GEMINI_API_KEY environment variable.",
                    retryable = false
                )
            }
            else -> null // Valid
        }
    }

    /**
     * Calculate exponential backoff with jitter.
     */
    private fun calculateBackoff(attempt: Int): Long {
        val exponentialMs = INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong()
        val cappedMs = minOf(exponentialMs, MAX_BACKOFF_MS)
        val jitter = (Math.random() * 0.1 * cappedMs).toLong()
        return cappedMs + jitter
    }
}
