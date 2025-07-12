package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.*

/**
 * Service for generating schema information about the food-chain data model
 */
class SchemaService {

    /**
     * Generate complete schema information including entities, relationships, and constraints
     */
    fun getSchema(): SchemaResponse {
        return SchemaResponse(
            entities = generateEntitySchemas(),
            relationships = generateRelationshipSchemas(),
            relationshipMatrix = generateRelationshipMatrix(),
            enums = generateEnumSchemas()
        )
    }

    private fun generateEntitySchemas(): List<EntitySchemaInfo> {
        return EntityType.getAllStandardTypes().map { entityType ->
            EntitySchemaInfo(
                type = entityType.label,
                label = entityType.label,
                description = entityType.description,
                properties = generateEntityProperties(entityType)
            )
        }
    }

    private fun generateEntityProperties(entityType: EntityType): List<PropertySchemaInfo> {
        val baseProperties = listOf(
            PropertySchemaInfo("id", "Long", false, "Unique identifier for the entity"),
            PropertySchemaInfo("name", "String", true, "Name of the entity"),
            PropertySchemaInfo("description", "String", false, "Optional description of the entity"),
            PropertySchemaInfo("createdAt", "Instant", true, "Timestamp when the entity was created"),
            PropertySchemaInfo("updatedAt", "Instant", true, "Timestamp when the entity was last updated")
        )

        val specificProperties = when (entityType) {
            is EntityType.Ingredient -> listOf(
                PropertySchemaInfo(
                    "purchaseFrequency", 
                    "PurchaseFrequency", 
                    false, 
                    "How frequently this ingredient is purchased",
                    enumValues = PurchaseFrequency.values().map { it.name }
                )
            )
            else -> emptyList()
        }

        return baseProperties + specificProperties
    }

    private fun generateRelationshipSchemas(): List<RelationshipSchemaInfo> {
        return RelationshipType.getAllStandardTypes().map { relationshipType ->
            RelationshipSchemaInfo(
                name = relationshipType.name,
                description = relationshipType.description,
                isDirectional = relationshipType.isDirectional
            )
        }
    }

    private fun generateRelationshipMatrix(): List<RelationshipMatrixEntry> {
        val matrix = mutableListOf<RelationshipMatrixEntry>()
        
        // Generate all valid entity type combinations
        val entityTypes = EntityType.getAllStandardTypes()
        
        for (fromType in entityTypes) {
            for (toType in entityTypes) {
                val relationshipType = RelationshipType.getRelationshipType(fromType, toType)
                if (relationshipType != null) {
                    matrix.add(
                        RelationshipMatrixEntry(
                            fromEntityType = fromType.label,
                            toEntityType = toType.label,
                            relationshipName = relationshipType.name,
                            relationshipDescription = relationshipType.description
                        )
                    )
                }
            }
        }
        
        return matrix
    }

    private fun generateEnumSchemas(): Map<String, List<String>> {
        return mapOf(
            "PurchaseFrequency" to PurchaseFrequency.values().map { it.name }
        )
    }
}
