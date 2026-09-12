package com.bypass.ai

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import org.junit.Before

@RunWith(RobolectricTestRunner::class)
class GeminiApiTest {
    @Before
    fun setup() {
        ShadowLog.stream = System.out
    }
    
    @Test
    fun testGenerateGeminiResponse() = runBlocking {
        val history = emptyList<ChatMessage>()
        val prompt = "Reply with exactly: GEMINI_AUTH_OK"
        
        try {
            val response = generateGeminiResponse(history, prompt)
            println("Response: $response")
            assertTrue("Response must contain 'GEMINI_AUTH_OK' but was: $response", response.contains("GEMINI_AUTH_OK"))
        } catch(e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
