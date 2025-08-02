package com.foodchain.autotroph.model

import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntityResponseTest {

    @Test
    fun `EntityResponse should filter out embedding data from properties`() {
        // Create an EntityNode with embedding data in properties
        val embeddingData = listOf(0.1, 0.2, 0.3, 0.4, 0.5)
        val properties = mapOf<String, JsonElement>(
            "embedding" to JsonArray(embeddingData.map { JsonPrimitive(it) }),
            "purchaseFrequency" to JsonPrimitive("Always"),
            "someOtherProperty" to JsonPrimitive("value")
        )
        
        val entityNode = EntityNode(
            id = 1L,
            name = "Test Ingredient",
            type = "Ingredient",
            description = "A test ingredient",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = properties,
            relatedEntities = emptyList()
        )
        
        // Convert to EntityResponse
        val response = EntityResponse.from(entityNode)
        
        // Verify embedding data is filtered out
        assertFalse(response.properties.containsKey("embedding"), "Embedding data should be filtered out")
        
        // Verify other properties are preserved
        assertTrue(response.properties.containsKey("purchaseFrequency"), "Other properties should be preserved")
        assertTrue(response.properties.containsKey("someOtherProperty"), "Other properties should be preserved")
        
        // Verify basic entity data is preserved
        assertEquals(1L, response.id)
        assertEquals("Test Ingredient", response.name)
        assertEquals("Ingredient", response.type)
        assertEquals("A test ingredient", response.description)
    }
    
    @Test
    fun `EntityResponse should filter out various embedding property names`() {
        val properties = mapOf<String, JsonElement>(
            "embedding" to JsonArray(listOf(JsonPrimitive(0.1))),
            "embeddings" to JsonArray(listOf(JsonPrimitive(0.2))),
            "vector" to JsonArray(listOf(JsonPrimitive(0.3))),
            "vectors" to JsonArray(listOf(JsonPrimitive(0.4))),
            "EMBEDDING" to JsonArray(listOf(JsonPrimitive(0.5))), // Test case insensitive
            "normalProperty" to JsonPrimitive("keep this")
        )
        
        val entityNode = EntityNode(
            id = 2L,
            name = "Test Entity",
            type = "Test",
            properties = properties
        )
        
        val response = EntityResponse.from(entityNode)
        
        // All embedding-related properties should be filtered out
        assertFalse(response.properties.containsKey("embedding"))
        assertFalse(response.properties.containsKey("embeddings"))
        assertFalse(response.properties.containsKey("vector"))
        assertFalse(response.properties.containsKey("vectors"))
        assertFalse(response.properties.containsKey("EMBEDDING"))
        
        // Normal properties should be preserved
        assertTrue(response.properties.containsKey("normalProperty"))
        assertEquals(JsonPrimitive("keep this"), response.properties["normalProperty"])
    }
    
    @Test
    fun `EntityResponse should work with empty properties`() {
        val entityNode = EntityNode(
            id = 3L,
            name = "Empty Properties Entity",
            type = "Test",
            properties = emptyMap()
        )
        
        val response = EntityResponse.from(entityNode)
        
        assertTrue(response.properties.isEmpty())
        assertEquals(3L, response.id)
        assertEquals("Empty Properties Entity", response.name)
    }
}
