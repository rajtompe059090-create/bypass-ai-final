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
        val prompt = "Respond exactly with the string 'HELLO_WORLD'"
        
        try {
            val response = generateGeminiResponse(history, prompt)
            assertTrue("Response must contain 'HELLO_WORLD' but was: $response", response.contains("HELLO_WORLD"))
        } catch(e: Exception) {
            println("Test Failed: ${e.message}")
            throw e
        }
    }
}
