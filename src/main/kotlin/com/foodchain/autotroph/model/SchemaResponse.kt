package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable

/**
 * Response model for the schema endpoint that describes the complete data model
 * including entities, relationships, and their constraints.
 */
@Serializable
data class SchemaResponse(
    val entities: List<EntitySchemaInfo>,
    val relationships: List<RelationshipSchemaInfo>,
    val relationshipMatrix: List<RelationshipMatrixEntry>,
    val enums: Map<String, List<String>>
)

/**
 * Schema information for an entity type
 */
@Serializable
data class EntitySchemaInfo(
    val type: String,
    val label: String,
    val description: String,
    val properties: List<PropertySchemaInfo>
)

/**
 * Schema information for entity properties
 */
@Serializable
data class PropertySchemaInfo(
    val name: String,
    val type: String,
    val required: Boolean,
    val description: String,
    val enumValues: List<String>? = null
)

/**
 * Schema information for a relationship type
 */
@Serializable
data class RelationshipSchemaInfo(
    val name: String,
    val description: String,
    val isDirectional: Boolean
)

/**
 * Entry in the relationship matrix showing valid entity-to-entity relationships
 */
@Serializable
data class RelationshipMatrixEntry(
    val fromEntityType: String,
    val toEntityType: String,
    val relationshipName: String,
    val relationshipDescription: String
)
