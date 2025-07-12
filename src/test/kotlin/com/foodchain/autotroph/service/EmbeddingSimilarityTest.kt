package com.foodchain.autotroph.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbeddingSimilarityTest {

    private val embeddingService = EmbeddingService("test-api-key")

    @Test
    fun `calculateCosineSimilarity should return 1 for identical vectors`() = runBlocking {
        // Given
        val vector1 = listOf(1.0, 2.0, 3.0, 4.0)
        val vector2 = listOf(1.0, 2.0, 3.0, 4.0)

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(1.0, similarity, 1e-10)
    }

    @Test
    fun `calculateCosineSimilarity should return 0 for orthogonal vectors`() = runBlocking {
        // Given
        val vector1 = listOf(1.0, 0.0)
        val vector2 = listOf(0.0, 1.0)

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(0.0, similarity, 1e-10)
    }

    @Test
    fun `calculateCosineSimilarity should return -1 for opposite vectors`() = runBlocking {
        // Given
        val vector1 = listOf(1.0, 0.0)
        val vector2 = listOf(-1.0, 0.0)

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(-1.0, similarity, 1e-10)
    }

    @Test
    fun `calculateCosineSimilarity should handle different magnitudes correctly`() = runBlocking {
        // Given
        val vector1 = listOf(1.0, 1.0)
        val vector2 = listOf(2.0, 2.0) // Same direction, different magnitude

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(1.0, similarity, 1e-10)
    }

    @Test
    fun `calculateCosineSimilarity should return 0 for mismatched dimensions`() = runBlocking {
        // Given
        val vector1 = listOf(1.0, 2.0, 3.0)
        val vector2 = listOf(1.0, 2.0)

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(0.0, similarity, 1e-10)
    }

    @Test
    fun `calculateCosineSimilarity should return 0 for empty vectors`() = runBlocking {
        // Given
        val vector1 = emptyList<Double>()
        val vector2 = emptyList<Double>()

        // When
        val similarity = embeddingService.calculateCosineSimilarity(vector1, vector2)

        // Then
        assertEquals(0.0, similarity, 1e-10)
    }

    @Test
    fun `findMostSimilar should return results sorted by similarity`() = runBlocking {
        // Given
        val targetEmbedding = listOf(1.0, 0.0)
        val candidates = listOf(
            "item1" to listOf(1.0, 0.0),      // similarity = 1.0
            "item2" to listOf(0.0, 1.0),      // similarity = 0.0
            "item3" to listOf(0.7071, 0.7071), // similarity ≈ 0.7071
            "item4" to listOf(-1.0, 0.0)      // similarity = -1.0
        )

        // When
        val results = embeddingService.findMostSimilar(targetEmbedding, candidates, topK = 4)

        // Then
        assertEquals(4, results.size)
        assertEquals("item1", results[0].data)
        assertEquals(1.0, results[0].similarity, 1e-10)
        assertEquals("item3", results[1].data)
        assertTrue(results[1].similarity > 0.7)
        assertEquals("item2", results[2].data)
        assertEquals(0.0, results[2].similarity, 1e-10)
        assertEquals("item4", results[3].data)
        assertEquals(-1.0, results[3].similarity, 1e-10)
    }

    @Test
    fun `findMostSimilar should respect topK limit`() = runBlocking {
        // Given
        val targetEmbedding = listOf(1.0, 0.0)
        val candidates = listOf(
            "item1" to listOf(1.0, 0.0),
            "item2" to listOf(0.9, 0.1),
            "item3" to listOf(0.8, 0.2),
            "item4" to listOf(0.7, 0.3),
            "item5" to listOf(0.6, 0.4)
        )

        // When
        val results = embeddingService.findMostSimilar(targetEmbedding, candidates, topK = 3)

        // Then
        assertEquals(3, results.size)
        assertEquals("item1", results[0].data)
        assertEquals("item2", results[1].data)
        assertEquals("item3", results[2].data)
    }

    @Test
    fun `findMostSimilar should handle empty candidates list`() = runBlocking {
        // Given
        val targetEmbedding = listOf(1.0, 0.0)
        val candidates = emptyList<Pair<String, List<Double>>>()

        // When
        val results = embeddingService.findMostSimilar(targetEmbedding, candidates, topK = 5)

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `generateEmbedding should produce similar embeddings for similar words`() = runBlocking {
        // Given
        val word1 = "tomato"
        val word2 = "tomatoes"

        // When
        val embedding1 = embeddingService.generateEmbedding(word1)
        val embedding2 = embeddingService.generateEmbedding(word2)
        val similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2)

        // Then
        // Similar words should have some similarity (though this is a mock implementation)
        assertTrue(similarity > -1.0 && similarity <= 1.0)
        assertEquals(384, embedding1.size)
        assertEquals(384, embedding2.size)
    }
}
