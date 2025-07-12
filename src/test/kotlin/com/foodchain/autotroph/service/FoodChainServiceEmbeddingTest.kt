package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.repository.EntityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FoodChainServiceEmbeddingTest {

    private val entityRepository = mockk<EntityRepository>()
    private val entityService = mockk<EntityService>()
    private val embeddingService = mockk<EmbeddingService>()
    
    private val foodChainService = FoodChainService(
        entityRepository = entityRepository,
        entityService = entityService,
        embeddingService = embeddingService
    )

    @Test
    fun `createIngredient should generate embedding and include it in the created ingredient`() = runBlocking {
        // Given
        val ingredientName = "tomato"
        val mockEmbedding = listOf(0.1, 0.2, 0.3, 0.4, 0.5)
        val request = CreateIngredientRequest(
            name = ingredientName,
            description = "A red fruit",
            purchaseFrequency = PurchaseFrequency.Usually
        )

        val mockCreatedEntity = EntityResponse(
            id = 123L,
            name = ingredientName,
            type = "Ingredient",
            description = "A red fruit",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = mapOf(
                "purchaseFrequency" to JsonPrimitive("Usually"),
                "embedding" to kotlinx.serialization.json.JsonArray(
                    mockEmbedding.map { kotlinx.serialization.json.JsonPrimitive(it) }
                )
            ),
            relatedEntitiesCount = 0
        )

        // Mock the embedding service to return our test embedding
        coEvery { embeddingService.generateEmbedding(ingredientName) } returns mockEmbedding

        // Mock the entity service to return our mock entity
        val createEntityRequestSlot = slot<CreateEntityRequest>()
        coEvery { entityService.createEntity(capture(createEntityRequestSlot)) } returns mockCreatedEntity

        // When
        val result = foodChainService.createIngredient(request)

        // Then
        // Verify that embedding service was called with the ingredient name
        coVerify { embeddingService.generateEmbedding(ingredientName) }

        // Verify that entity service was called with the correct request including embedding
        coVerify { entityService.createEntity(any()) }
        
        val capturedRequest = createEntityRequestSlot.captured
        assertNotNull(capturedRequest.properties)
        assertTrue(capturedRequest.properties.containsKey("embedding"))
        
        // Verify the result contains the expected data
        assertEquals(123L, result.id)
        assertEquals(ingredientName, result.name)
        assertEquals("A red fruit", result.description)
        assertEquals(PurchaseFrequency.Usually, result.purchaseFrequency)
    }

    @Test
    fun `createIngredient should handle empty embedding gracefully`() = runBlocking {
        // Given
        val ingredientName = "carrot"
        val emptyEmbedding = emptyList<Double>()
        val request = CreateIngredientRequest(
            name = ingredientName,
            description = "An orange vegetable"
        )

        val mockCreatedEntity = EntityResponse(
            id = 456L,
            name = ingredientName,
            type = "Ingredient",
            description = "An orange vegetable",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = mapOf(
                "embedding" to kotlinx.serialization.json.JsonArray(emptyList())
            ),
            relatedEntitiesCount = 0
        )

        // Mock the embedding service to return empty embedding
        coEvery { embeddingService.generateEmbedding(ingredientName) } returns emptyEmbedding

        // Mock the entity service
        coEvery { entityService.createEntity(any()) } returns mockCreatedEntity

        // When
        val result = foodChainService.createIngredient(request)

        // Then
        // Verify that embedding service was still called
        coVerify { embeddingService.generateEmbedding(ingredientName) }
        
        // Verify the result is created successfully
        assertEquals(456L, result.id)
        assertEquals(ingredientName, result.name)
    }

    @Test
    fun `createIngredient should call embedding service before entity creation`() = runBlocking {
        // Given
        val ingredientName = "onion"
        val mockEmbedding = listOf(0.7, 0.8, 0.9)
        val request = CreateIngredientRequest(name = ingredientName)

        val mockCreatedEntity = EntityResponse(
            id = 789L,
            name = ingredientName,
            type = "Ingredient",
            description = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = emptyMap(),
            relatedEntitiesCount = 0
        )

        // Mock services
        coEvery { embeddingService.generateEmbedding(ingredientName) } returns mockEmbedding
        coEvery { entityService.createEntity(any()) } returns mockCreatedEntity

        // When
        foodChainService.createIngredient(request)

        // Then
        // Verify the order of calls - embedding should be generated before entity creation
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            embeddingService.generateEmbedding(ingredientName)
            entityService.createEntity(any())
        }
    }
}
