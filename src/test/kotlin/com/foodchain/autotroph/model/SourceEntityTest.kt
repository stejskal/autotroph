package com.foodchain.autotroph.model

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceEntityTest {

    @Test
    fun `should create Source entity with basic properties`() {
        // Given
        val now = Clock.System.now()
        val source = Source(
            id = 1L,
            name = "The Joy of Cooking",
            description = "Classic American cookbook",
            createdAt = now,
            updatedAt = now,
            sources = listOf(2L, 3L)
        )

        // Then
        assertEquals(1L, source.id)
        assertEquals("The Joy of Cooking", source.name)
        assertEquals("Classic American cookbook", source.description)
        assertEquals(now, source.createdAt)
        assertEquals(now, source.updatedAt)
        assertEquals(listOf(2L, 3L), source.sources)
    }

    @Test
    fun `should create Source entity with empty sources list`() {
        // Given
        val source = Source(
            name = "Food Network Website",
            description = "Online recipe collection"
        )

        // Then
        assertEquals("Food Network Website", source.name)
        assertEquals("Online recipe collection", source.description)
        assertTrue(source.sources.isEmpty())
    }

    @Test
    fun `EntityType should include Source`() {
        // Given
        val allTypes = EntityType.getAllStandardTypes()

        // Then
        assertTrue(allTypes.any { it is EntityType.Source })
        assertEquals("Source", EntityType.Source.label)
        assertEquals("Reference or origin of a recipe or another source", EntityType.Source.description)
    }

    @Test
    fun `EntityType fromString should handle source`() {
        // When
        val sourceType = EntityType.fromString("source")

        // Then
        assertTrue(sourceType is EntityType.Source)
    }

    @Test
    fun `RelationshipType should include FROM relationships`() {
        // Given
        val allTypes = RelationshipType.getAllStandardTypes()

        // Then
        assertTrue(allTypes.any { it is RelationshipType.SourceFromSource })
        assertTrue(allTypes.any { it is RelationshipType.RecipeFromSource })
        
        assertEquals("FROM", RelationshipType.SourceFromSource.name)
        assertEquals("FROM", RelationshipType.RecipeFromSource.name)
    }

    @Test
    fun `getRelationshipType should return correct relationships for Source`() {
        // When
        val sourceToSource = RelationshipType.getRelationshipType(EntityType.Source, EntityType.Source)
        val recipeToSource = RelationshipType.getRelationshipType(EntityType.Recipe, EntityType.Source)

        // Then
        assertTrue(sourceToSource is RelationshipType.SourceFromSource)
        assertTrue(recipeToSource is RelationshipType.RecipeFromSource)
    }

    @Test
    fun `FoodChainEntityConverter should handle Source entity`() {
        // Given
        val source = Source(
            id = 1L,
            name = "Test Source",
            description = "Test Description",
            sources = listOf(2L, 3L)
        )

        // When
        val entityNode = FoodChainEntityConverter.toEntityNode(source)

        // Then
        assertEquals(1L, entityNode.id)
        assertEquals("Test Source", entityNode.name)
        assertEquals("Source", entityNode.type)
        assertEquals("Test Description", entityNode.description)
        assertEquals(listOf(2L, 3L), entityNode.relatedEntities)
    }

    @Test
    fun `Recipe entity should include sources field`() {
        // Given
        val recipe = Recipe(
            id = 1L,
            name = "Test Recipe",
            description = "Test Description",
            ingredients = listOf(1L),
            subRecipes = listOf(2L),
            cuisines = listOf(3L),
            sources = listOf(4L, 5L)
        )

        // When
        val entityNode = FoodChainEntityConverter.toEntityNode(recipe)

        // Then
        assertEquals("Recipe", entityNode.type)
        // Related entities should include ingredients + subRecipes + cuisines + sources
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), entityNode.relatedEntities)
    }
}
