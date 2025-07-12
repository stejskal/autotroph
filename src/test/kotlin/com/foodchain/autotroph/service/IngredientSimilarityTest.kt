package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.repository.EntityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientSimilarityTest {

    private val entityRepository = mockk<EntityRepository>()
    private val entityService = mockk<EntityService>()
    private val embeddingService = mockk<EmbeddingService>()
    
    private val foodChainService = FoodChainService(
        entityRepository = entityRepository,
        entityService = entityService,
        embeddingService = embeddingService
    )

    @Test
    fun `findSimilarIngredients should return similar ingredients sorted by similarity`() = runBlocking {
        // Given
        val searchTerm = "tomato"
        val targetEmbedding = listOf(0.1, 0.2, 0.3)
        
        val mockIngredients = listOf(
            createMockEntityResponse(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)),
            createMockEntityResponse(2L, "potato", listOf(0.8, 0.1, 0.2)),
            createMockEntityResponse(3L, "tomato sauce", listOf(0.12, 0.22, 0.32))
        )

        val mockSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)), 0.95),
            SimilarityResult(createMockIngredient(3L, "tomato sauce", listOf(0.12, 0.22, 0.32)), 0.90),
            SimilarityResult(createMockIngredient(2L, "potato", listOf(0.8, 0.1, 0.2)), 0.30)
        )

        // Mock the embedding service
        coEvery { embeddingService.generateEmbedding(searchTerm) } returns targetEmbedding
        coEvery { 
            embeddingService.findMostSimilar(
                targetEmbedding = targetEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = 10
            ) 
        } returns mockSimilarityResults

        // Mock the entity service
        coEvery { entityService.getEntitiesByType("Ingredient") } returns mockIngredients

        // When
        val results = foodChainService.findSimilarIngredients(searchTerm, 10)

        // Then
        assertEquals(3, results.size)
        assertEquals(1L, results[0].id)
        assertEquals("cherry tomato", results[0].name)
        assertEquals(0.95, results[0].similarity)
        assertEquals(3L, results[1].id)
        assertEquals("tomato sauce", results[1].name)
        assertEquals(0.90, results[1].similarity)
        assertEquals(2L, results[2].id)
        assertEquals("potato", results[2].name)
        assertEquals(0.30, results[2].similarity)

        // Verify service calls
        coVerify { embeddingService.generateEmbedding(searchTerm) }
        coVerify { entityService.getEntitiesByType("Ingredient") }
    }

    @Test
    fun `findSimilarIngredients should handle empty database gracefully`() = runBlocking {
        // Given
        val searchTerm = "tomato"
        val targetEmbedding = listOf(0.1, 0.2, 0.3)

        coEvery { embeddingService.generateEmbedding(searchTerm) } returns targetEmbedding
        coEvery { entityService.getEntitiesByType("Ingredient") } returns emptyList()

        // When
        val results = foodChainService.findSimilarIngredients(searchTerm, 10)

        // Then
        assertTrue(results.isEmpty())
        coVerify { embeddingService.generateEmbedding(searchTerm) }
        coVerify { entityService.getEntitiesByType("Ingredient") }
    }

    @Test
    fun `findSimilarIngredients should filter out ingredients without embeddings`() = runBlocking {
        // Given
        val searchTerm = "tomato"
        val targetEmbedding = listOf(0.1, 0.2, 0.3)
        
        val mockIngredients = listOf(
            createMockEntityResponse(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)),
            createMockEntityResponse(2L, "potato", null), // No embedding
            createMockEntityResponse(3L, "onion", emptyList()) // Empty embedding
        )

        val mockSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)), 0.95)
        )

        coEvery { embeddingService.generateEmbedding(searchTerm) } returns targetEmbedding
        coEvery { entityService.getEntitiesByType("Ingredient") } returns mockIngredients
        coEvery { 
            embeddingService.findMostSimilar(
                targetEmbedding = targetEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = 10
            ) 
        } returns mockSimilarityResults

        // When
        val results = foodChainService.findSimilarIngredients(searchTerm, 10)

        // Then
        assertEquals(1, results.size)
        assertEquals(1L, results[0].id)
        assertEquals("cherry tomato", results[0].name)
    }

    @Test
    fun `findSimilarIngredients should respect topK parameter`() = runBlocking {
        // Given
        val searchTerm = "tomato"
        val targetEmbedding = listOf(0.1, 0.2, 0.3)
        val topK = 2
        
        val mockIngredients = listOf(
            createMockEntityResponse(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)),
            createMockEntityResponse(2L, "potato", listOf(0.8, 0.1, 0.2)),
            createMockEntityResponse(3L, "tomato sauce", listOf(0.12, 0.22, 0.32))
        )

        val mockSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(1L, "cherry tomato", listOf(0.15, 0.25, 0.35)), 0.95),
            SimilarityResult(createMockIngredient(3L, "tomato sauce", listOf(0.12, 0.22, 0.32)), 0.90)
        )

        coEvery { embeddingService.generateEmbedding(searchTerm) } returns targetEmbedding
        coEvery { entityService.getEntitiesByType("Ingredient") } returns mockIngredients
        coEvery { 
            embeddingService.findMostSimilar(
                targetEmbedding = targetEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = topK
            ) 
        } returns mockSimilarityResults

        // When
        val results = foodChainService.findSimilarIngredients(searchTerm, topK)

        // Then
        assertEquals(2, results.size)
        coVerify { 
            embeddingService.findMostSimilar(
                targetEmbedding = targetEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = topK
            ) 
        }
    }

    private fun createMockEntityResponse(id: Long, name: String, embedding: List<Double>?): EntityResponse {
        val properties = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
        
        embedding?.let {
            if (it.isNotEmpty()) {
                properties["embedding"] = JsonArray(it.map { value -> JsonPrimitive(value) })
            }
        }
        
        return EntityResponse(
            id = id,
            name = name,
            type = "Ingredient",
            description = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = properties,
            relatedEntitiesCount = 0
        )
    }

    private fun createMockIngredient(id: Long, name: String, embedding: List<Double>?): Ingredient {
        return Ingredient(
            id = id,
            name = name,
            description = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            purchaseFrequency = null,
            storeLocations = emptyList(),
            embedding = embedding
        )
    }
}
