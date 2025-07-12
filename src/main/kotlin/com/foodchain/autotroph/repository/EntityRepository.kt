package com.foodchain.autotroph.repository

import com.foodchain.autotroph.model.EntityNode
import com.foodchain.autotroph.model.EntityType
import com.foodchain.autotroph.model.RelationshipType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging
import org.neo4j.driver.Driver
import org.neo4j.driver.Values

private val logger = KotlinLogging.logger {}

class EntityRepository(private val driver: Driver) {

    suspend fun findAll(): List<EntityNode> {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity)
                OPTIONAL MATCH (e)-[r]->(related:Entity)
                RETURN e, collect(ID(related)) as relatedIds
            """.trimIndent())
            result.list { record ->
                val node = record.get("e").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            }
        }
    }

    suspend fun findById(id: Long): EntityNode? {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity) WHERE ID(e) = ${'$'}id
                OPTIONAL MATCH (e)-[r]->(related:Entity)
                RETURN e, collect(ID(related)) as relatedIds
            """.trimIndent(), Values.parameters("id", id))
            if (result.hasNext()) {
                val record = result.single()
                val node = record.get("e").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            } else null
        }
    }

    suspend fun findByName(name: String): List<EntityNode> {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity) WHERE e.name = ${'$'}name
                OPTIONAL MATCH (e)-[r]->(related:Entity)
                RETURN e, collect(ID(related)) as relatedIds
            """.trimIndent(), Values.parameters("name", name))
            result.list { record ->
                val node = record.get("e").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            }
        }
    }

    suspend fun findByType(type: String): List<EntityNode> {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity) WHERE e.type = ${'$'}type
                OPTIONAL MATCH (e)-[r]->(related:Entity)
                RETURN e, collect(ID(related)) as relatedIds
            """.trimIndent(), Values.parameters("type", type))
            result.list { record ->
                val node = record.get("e").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            }
        }
    }

    suspend fun findByNameContaining(nameFragment: String): List<EntityNode> {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity) WHERE toLower(e.name) CONTAINS toLower(${'$'}nameFragment)
                OPTIONAL MATCH (e)-[r]->(related:Entity)
                RETURN e, collect(ID(related)) as relatedIds
            """.trimIndent(), Values.parameters("nameFragment", nameFragment))
            result.list { record ->
                val node = record.get("e").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            }
        }
    }

    suspend fun save(entity: EntityNode): EntityNode {
        return driver.session().use { session ->
            val now = Clock.System.now()

            val result = if (entity.id == null) {
                // Create new entity
                val params = Values.parameters(
                    "name", entity.name,
                    "type", entity.type,
                    "description", entity.description,
                    "createdAt", now.toString(),
                    "updatedAt", now.toString(),
                    "properties", kotlinx.serialization.json.JsonObject(entity.properties).toString()
                )
                session.run(
                    """
                    CREATE (e:Entity {
                        name: ${'$'}name,
                        type: ${'$'}type,
                        description: ${'$'}description,
                        created_at: ${'$'}createdAt,
                        updated_at: ${'$'}updatedAt,
                        properties: ${'$'}properties
                    })
                    RETURN e, ID(e) as id
                    """.trimIndent(),
                    params
                )
            } else {
                // Update existing entity
                val params = Values.parameters(
                    "id", entity.id,
                    "name", entity.name,
                    "type", entity.type,
                    "description", entity.description,
                    "updatedAt", now.toString(),
                    "properties", entity.properties.toString()
                )
                session.run(
                    """
                    MATCH (e:Entity) WHERE ID(e) = ${'$'}id
                    SET e.name = ${'$'}name,
                        e.type = ${'$'}type,
                        e.description = ${'$'}description,
                        e.updated_at = ${'$'}updatedAt,
                        e.properties = ${'$'}properties
                    RETURN e, ID(e) as id
                    """.trimIndent(),
                    params
                )
            }

            val record = result.single()
            val node = record.get("e").asNode()
            val id = record.get("id").asLong()

            // For newly created/updated entities, we need to fetch related entities separately
            val relatedEntitiesResult = session.run("""
                MATCH (e:Entity)-[r]->(related:Entity) WHERE ID(e) = ${'$'}id
                RETURN collect(ID(related)) as relatedIds
            """.trimIndent(), Values.parameters("id", id))

            val relatedIds = if (relatedEntitiesResult.hasNext()) {
                relatedEntitiesResult.single().get("relatedIds").asList { it.asLong() }.filterNotNull()
            } else {
                emptyList()
            }

            mapNodeToEntity(node, id, relatedIds)
        }
    }

    suspend fun deleteById(id: Long): Boolean {
        return driver.session().use { session ->
            val result = session.run(
                "MATCH (e:Entity) WHERE ID(e) = \$id DELETE e RETURN count(e) as deleted",
                Values.parameters("id", id)
            )
            result.single().get("deleted").asInt() > 0
        }
    }

    suspend fun existsById(id: Long): Boolean {
        return driver.session().use { session ->
            val result = session.run(
                "MATCH (e:Entity) WHERE ID(e) = \$id RETURN count(e) as count",
                Values.parameters("id", id)
            )
            result.single().get("count").asInt() > 0
        }
    }

    suspend fun findRelatedEntities(entityId: Long): List<EntityNode> {
        return driver.session().use { session ->
            val result = session.run("""
                MATCH (e:Entity)-[r]->(related:Entity) WHERE ID(e) = ${'$'}entityId
                OPTIONAL MATCH (related)-[r2]->(relatedToRelated:Entity)
                RETURN related, collect(ID(relatedToRelated)) as relatedIds
            """.trimIndent(), Values.parameters("entityId", entityId))
            result.list { record ->
                val node = record.get("related").asNode()
                val relatedIds = record.get("relatedIds").asList { it.asLong() }.filterNotNull()
                mapNodeToEntity(node, relatedEntities = relatedIds)
            }
        }
    }

    suspend fun createRelationship(fromId: Long, toId: Long): Boolean {
        return try {
            driver.session().use { session ->
                // First, get the entity types to determine the correct relationship type
                val entitiesResult = session.run(
                    """
                    MATCH (from:Entity), (to:Entity)
                    WHERE ID(from) = ${'$'}fromId AND ID(to) = ${'$'}toId
                    RETURN from.type as fromType, to.type as toType
                    """.trimIndent(),
                    Values.parameters("fromId", fromId, "toId", toId)
                )

                if (!entitiesResult.hasNext()) {
                    logger.error { "One or both entities not found: fromId=$fromId, toId=$toId" }
                    return false
                }

                val record = entitiesResult.single()
                val fromTypeStr = record.get("fromType").asString()
                val toTypeStr = record.get("toType").asString()

                // Convert string types to EntityType objects
                val fromType = EntityType.fromString(fromTypeStr)
                val toType = EntityType.fromString(toTypeStr)

                // Get the appropriate relationship type
                val relationshipType = RelationshipType.getRelationshipType(fromType, toType)
                if (relationshipType == null) {
                    logger.error { "No valid relationship type found for $fromType -> $toType" }
                    return false
                }

                logger.info { "Creating relationship of type ${relationshipType.name} from $fromType($fromId) to $toType($toId)" }

                // Create the relationship with the correct type
                val cypherQuery = """
                    MATCH (from:Entity), (to:Entity)
                    WHERE ID(from) = ${'$'}fromId AND ID(to) = ${'$'}toId
                    CREATE (from)-[:${relationshipType.name}]->(to)
                    """.trimIndent()

                logger.debug { "Executing Cypher query: $cypherQuery" }

                session.run(
                    cypherQuery,
                    Values.parameters("fromId", fromId, "toId", toId)
                )
                true
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to create relationship from $fromId to $toId" }
            false
        }
    }

    suspend fun deleteRelationship(fromId: Long, toId: Long) {
        driver.session().use { session ->
            session.run(
                """
                MATCH (from:Entity)-[r]->(to:Entity)
                WHERE ID(from) = ${'$'}fromId AND ID(to) = ${'$'}toId
                DELETE r
                """.trimIndent(),
                Values.parameters("fromId", fromId, "toId", toId)
            )
        }
    }

    private fun mapNodeToEntity(node: org.neo4j.driver.types.Node, id: Long? = null, relatedEntities: List<Long> = emptyList()): EntityNode {
        // Parse properties from JSON string
        val properties: Map<String, JsonElement> = if (node.containsKey("properties")) {
            try {
                val propertiesString = node.get("properties").asString()
                if (propertiesString.isNotBlank() && propertiesString != "{}") {
                    kotlinx.serialization.json.Json.parseToJsonElement(propertiesString).jsonObject
                } else {
                    emptyMap<String, JsonElement>()
                }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to parse properties JSON for entity ${node.get("name").asString()}: ${node.get("properties").asString()}" }
                emptyMap<String, JsonElement>()
            }
        } else {
            emptyMap<String, JsonElement>()
        }

        return EntityNode(
            id = id ?: node.id(),
            name = node.get("name").asString(),
            type = node.get("type").asString(),
            description = if (node.containsKey("description")) node.get("description").asString() else null,
            createdAt = kotlinx.datetime.Instant.parse(node.get("created_at").asString()),
            updatedAt = kotlinx.datetime.Instant.parse(node.get("updated_at").asString()),
            properties = properties,
            relatedEntities = relatedEntities
        )
    }
}
