package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

@Serializable
data class EntityNode(
    val id: Long? = null,
    val name: String,
    val type: String,
    val description: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val properties: Map<String, JsonElement> = emptyMap(),
    val relatedEntities: List<Long> = emptyList()
)

@Serializable
data class CreateEntityRequest(
    val name: String,
    val type: String,
    val description: String? = null,
    val properties: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class UpdateEntityRequest(
    val name: String? = null,
    val type: String? = null,
    val description: String? = null,
    val properties: Map<String, JsonElement>? = null
)

@Serializable
data class EntityResponse(
    val id: Long,
    val name: String,
    val type: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val properties: Map<String, JsonElement>,
    val relatedEntitiesCount: Int = 0
) {
    companion object {
        fun from(entity: EntityNode): EntityResponse {
            return EntityResponse(
                id = entity.id!!,
                name = entity.name,
                type = entity.type,
                description = entity.description,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                properties = entity.properties,
                relatedEntitiesCount = entity.relatedEntities.size
            )
        }
    }
}

@Serializable
data class CreateRelationshipRequest(
    val fromId: Long,
    val toId: Long
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)
