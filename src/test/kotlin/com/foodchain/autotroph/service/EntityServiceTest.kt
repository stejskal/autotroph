package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.CreateEntityRequest
import com.foodchain.autotroph.model.EntityNode
import com.foodchain.autotroph.model.UpdateEntityRequest
import com.foodchain.autotroph.repository.EntityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EntityServiceTest {

    private val entityRepository = mockk<EntityRepository>()
    private val entityService = EntityService(entityRepository)

    @Test
    fun `should create entity successfully`() = runBlocking {
        // Given
        val request = CreateEntityRequest(
            name = "Test Entity",
            type = "TestType",
            description = "Test Description",
            properties = emptyMap()
        )

        val savedEntity = EntityNode(
            id = 1L,
            name = request.name,
            type = request.type,
            description = request.description,
            properties = request.properties,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        coEvery { entityRepository.save(any<EntityNode>()) } returns savedEntity

        // When
        val result = entityService.createEntity(request)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Entity", result.name)
        assertEquals("TestType", result.type)
        assertEquals("Test Description", result.description)
        assertEquals(emptyMap(), result.properties)

        coVerify { entityRepository.save(any<EntityNode>()) }
    }

    @Test
    fun `should get entity by id successfully`() = runBlocking {
        // Given
        val entityId = 1L
        val entity = EntityNode(
            id = entityId,
            name = "Test Entity",
            type = "TestType",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        coEvery { entityRepository.findById(entityId) } returns entity

        // When
        val result = entityService.getEntityById(entityId)

        // Then
        assertNotNull(result)
        assertEquals(entityId, result?.id)
        assertEquals("Test Entity", result?.name)
        assertEquals("TestType", result?.type)

        coVerify { entityRepository.findById(entityId) }
    }

    @Test
    fun `should return null when entity not found`() = runBlocking {
        // Given
        val entityId = 999L
        coEvery { entityRepository.findById(entityId) } returns null

        // When
        val result = entityService.getEntityById(entityId)

        // Then
        assertNull(result)
        coVerify { entityRepository.findById(entityId) }
    }

    @Test
    fun `should update entity successfully`() = runBlocking {
        // Given
        val entityId = 1L
        val existingEntity = EntityNode(
            id = entityId,
            name = "Old Name",
            type = "OldType",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        val updateRequest = UpdateEntityRequest(
            name = "New Name",
            type = "NewType"
        )

        val updatedEntity = existingEntity.copy(
            name = "New Name",
            type = "NewType",
            updatedAt = Clock.System.now()
        )

        coEvery { entityRepository.findById(entityId) } returns existingEntity
        coEvery { entityRepository.save(any<EntityNode>()) } returns updatedEntity

        // When
        val result = entityService.updateEntity(entityId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals("New Name", result?.name)
        assertEquals("NewType", result?.type)

        coVerify { entityRepository.findById(entityId) }
        coVerify { entityRepository.save(any<EntityNode>()) }
    }

    @Test
    fun `should return correct relatedEntitiesCount`() = runBlocking {
        // Given
        val entityId = 1L
        val relatedEntityIds = listOf(2L, 3L, 4L)
        val entity = EntityNode(
            id = entityId,
            name = "Test Entity",
            type = "TestType",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            relatedEntities = relatedEntityIds
        )

        coEvery { entityRepository.findById(entityId) } returns entity

        // When
        val result = entityService.getEntityById(entityId)

        // Then
        assertNotNull(result)
        assertEquals(entityId, result?.id)
        assertEquals("Test Entity", result?.name)
        assertEquals(3, result?.relatedEntitiesCount) // Should match the size of relatedEntityIds

        coVerify { entityRepository.findById(entityId) }
    }

    @Test
    fun `should return zero relatedEntitiesCount when no related entities`() = runBlocking {
        // Given
        val entityId = 1L
        val entity = EntityNode(
            id = entityId,
            name = "Test Entity",
            type = "TestType",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            relatedEntities = emptyList()
        )

        coEvery { entityRepository.findById(entityId) } returns entity

        // When
        val result = entityService.getEntityById(entityId)

        // Then
        assertNotNull(result)
        assertEquals(entityId, result?.id)
        assertEquals(0, result?.relatedEntitiesCount)

        coVerify { entityRepository.findById(entityId) }
    }
}
