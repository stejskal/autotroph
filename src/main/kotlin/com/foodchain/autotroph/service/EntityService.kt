package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.CreateEntityRequest
import com.foodchain.autotroph.model.EntityNode
import com.foodchain.autotroph.model.EntityResponse
import com.foodchain.autotroph.model.UpdateEntityRequest
import com.foodchain.autotroph.repository.EntityRepository
import kotlinx.datetime.Clock
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EntityService(private val entityRepository: EntityRepository) {

    suspend fun getAllEntities(): List<EntityResponse> {
        logger.info { "Fetching all entities" }
        return entityRepository.findAll().map { EntityResponse.from(it) }
    }

    suspend fun getEntityById(id: Long): EntityResponse? {
        logger.info { "Fetching entity with id: $id" }
        return entityRepository.findById(id)?.let { EntityResponse.from(it) }
    }

    suspend fun getEntitiesByName(name: String): List<EntityResponse> {
        logger.info { "Fetching entities with name: $name" }
        return entityRepository.findByName(name).map { EntityResponse.from(it) }
    }

    suspend fun getEntitiesByType(type: String): List<EntityResponse> {
        logger.info { "Fetching entities with type: $type" }
        return entityRepository.findByType(type).map { EntityResponse.from(it) }
    }

    suspend fun searchEntitiesByName(nameFragment: String): List<EntityResponse> {
        logger.info { "Searching entities with name containing: $nameFragment" }
        return entityRepository.findByNameContaining(nameFragment).map { EntityResponse.from(it) }
    }

    suspend fun createEntity(request: CreateEntityRequest): EntityResponse {
        logger.info { "Creating new entity: ${request.name}" }
        val entity = EntityNode(
            name = request.name,
            type = request.type,
            description = request.description,
            properties = request.properties,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val savedEntity = entityRepository.save(entity)
        logger.info { "Created entity with id: ${savedEntity.id}" }
        return EntityResponse.from(savedEntity)
    }

    suspend fun updateEntity(id: Long, request: UpdateEntityRequest): EntityResponse? {
        logger.info { "Updating entity with id: $id" }
        val existingEntity = entityRepository.findById(id) ?: return null

        val updatedEntity = existingEntity.copy(
            name = request.name ?: existingEntity.name,
            type = request.type ?: existingEntity.type,
            description = request.description ?: existingEntity.description,
            properties = request.properties ?: existingEntity.properties,
            updatedAt = Clock.System.now()
        )

        val savedEntity = entityRepository.save(updatedEntity)
        logger.info { "Updated entity with id: $id" }
        return EntityResponse.from(savedEntity)
    }

    suspend fun deleteEntity(id: Long): Boolean {
        logger.info { "Deleting entity with id: $id" }
        return if (entityRepository.existsById(id)) {
            entityRepository.deleteById(id)
            logger.info { "Deleted entity with id: $id" }
            true
        } else {
            logger.warn { "Entity with id $id not found for deletion" }
            false
        }
    }

    suspend fun getRelatedEntities(entityId: Long): List<EntityResponse> {
        logger.info { "Fetching related entities for entity id: $entityId" }
        return entityRepository.findRelatedEntities(entityId).map { EntityResponse.from(it) }
    }

    suspend fun createRelationship(fromId: Long, toId: Long): Boolean {
        logger.info { "Creating relationship from entity $fromId to entity $toId" }
        return entityRepository.createRelationship(fromId, toId)
    }

    suspend fun deleteRelationship(fromId: Long, toId: Long) {
        logger.info { "Deleting relationship from entity $fromId to entity $toId" }
        entityRepository.deleteRelationship(fromId, toId)
        logger.info { "Deleted relationship from $fromId to $toId" }
    }
}
