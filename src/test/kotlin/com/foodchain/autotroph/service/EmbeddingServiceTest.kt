package com.foodchain.autotroph.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmbeddingServiceTest {

    private val embeddingService = EmbeddingService("test-api-key")

    @Test
    fun `generateEmbedding should return non-null embedding`() = runBlocking {
        // Given
        val text = "tomato"

        // When
        val embedding = embeddingService.generateEmbedding(text)

        // Then
        assertNotNull(embedding)
        assertTrue(embedding.isNotEmpty())
        assertEquals(384, embedding.size) // Expected dimension
    }

    @Test
    fun `generateEmbedding should return deterministic results`() = runBlocking {
        // Given
        val text = "carrot"

        // When
        val embedding1 = embeddingService.generateEmbedding(text)
        val embedding2 = embeddingService.generateEmbedding(text)

        // Then
        assertEquals(embedding1.size, embedding2.size)
        embedding1.forEachIndexed { index, value ->
            assertEquals(value, embedding2[index], 1e-10)
        }
    }

    @Test
    fun `generateEmbedding should return different embeddings for different texts`() = runBlocking {
        // Given
        val text1 = "apple"
        val text2 = "banana"

        // When
        val embedding1 = embeddingService.generateEmbedding(text1)
        val embedding2 = embeddingService.generateEmbedding(text2)

        // Then
        assertEquals(embedding1.size, embedding2.size)
        
        // Embeddings should be different
        val areDifferent = embedding1.zip(embedding2).any { (a, b) -> 
            kotlin.math.abs(a - b) > 1e-10 
        }
        assertTrue(areDifferent, "Embeddings for different texts should be different")
    }

    @Test
    fun `generateEmbedding should handle empty string`() = runBlocking {
        // Given
        val text = ""

        // When
        val embedding = embeddingService.generateEmbedding(text)

        // Then
        assertNotNull(embedding)
        assertEquals(384, embedding.size)
    }

    @Test
    fun `generateEmbedding should handle case insensitive input`() = runBlocking {
        // Given
        val text1 = "Potato"
        val text2 = "POTATO"
        val text3 = "potato"

        // When
        val embedding1 = embeddingService.generateEmbedding(text1)
        val embedding2 = embeddingService.generateEmbedding(text2)
        val embedding3 = embeddingService.generateEmbedding(text3)

        // Then
        // All should be the same since the service normalizes to lowercase
        embedding1.forEachIndexed { index, value ->
            assertEquals(value, embedding2[index], 1e-10)
            assertEquals(value, embedding3[index], 1e-10)
        }
    }

    @Test
    fun `generateEmbedding should return normalized vectors`() = runBlocking {
        // Given
        val text = "onion"

        // When
        val embedding = embeddingService.generateEmbedding(text)

        // Then
        // Calculate magnitude (should be close to 1 for normalized vectors)
        val magnitude = kotlin.math.sqrt(embedding.sumOf { it * it })
        assertEquals(1.0, magnitude, 1e-10)
    }
}
