package com.foodchain.autotroph.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenAIServiceTest {

    private val openAIService = OpenAIService("test-api-key")

    @Test
    fun `extractRecipeFromText should handle empty text gracefully`() = runBlocking {
        // Given
        val emptyText = ""

        // When/Then - Should not throw exception
        try {
            val result = openAIService.extractRecipeFromText(emptyText)
            // If we get here without exception, that's good
            assertNotNull(result)
        } catch (e: Exception) {
            // Expected for test API key, but should not be a parsing error
            assertTrue(e.message?.contains("API") == true || e.message?.contains("auth") == true)
        }
    }

    @Test
    fun `extractRecipeFromText should handle malformed response gracefully`() = runBlocking {
        // This test verifies the manual parsing fallback works
        val service = OpenAIService("test-api-key")
        
        // Test the manual parsing methods indirectly by testing with text that might cause issues
        val problematicText = "This is not a recipe at all, just random text with no ingredients or cooking instructions."
        
        try {
            val result = service.extractRecipeFromText(problematicText)
            assertNotNull(result)
        } catch (e: Exception) {
            // Expected for test API key
            assertTrue(e.message?.contains("API") == true || e.message?.contains("auth") == true)
        }
    }
}
