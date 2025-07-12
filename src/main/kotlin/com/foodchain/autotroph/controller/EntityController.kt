package com.foodchain.autotroph.controller

import com.foodchain.autotroph.model.CreateEntityRequest
import com.foodchain.autotroph.model.EntityResponse
import com.foodchain.autotroph.model.UpdateEntityRequest
import com.foodchain.autotroph.model.ErrorResponse
import com.foodchain.autotroph.service.EntityService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

private val logger = KotlinLogging.logger {}

fun Route.entityRoutes() {
    val entityService by closestDI().instance<EntityService>()

    route("/api/v1/entities") {

        // GET /api/v1/entities
        get {
            logger.info { "GET /api/v1/entities - Fetching all entities" }
            try {
                val entities = entityService.getAllEntities()
                call.respond(HttpStatusCode.OK, entities)
            } catch (e: Exception) {
                logger.error(e) { "Error fetching all entities" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // GET /api/v1/entities/{id}
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity ID"))
                return@get
            }

            logger.info { "GET /api/v1/entities/$id - Fetching entity by id" }
            try {
                val entity = entityService.getEntityById(id)
                if (entity != null) {
                    call.respond(HttpStatusCode.OK, entity)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Entity not found"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error fetching entity $id" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // GET /api/v1/entities/search
        get("/search") {
            logger.info { "GET /api/v1/entities/search - Searching entities" }
            val name = call.request.queryParameters["name"]
            val type = call.request.queryParameters["type"]
            val nameFragment = call.request.queryParameters["nameFragment"]

            try {
                val entities = when {
                    name != null -> entityService.getEntitiesByName(name)
                    type != null -> entityService.getEntitiesByType(type)
                    nameFragment != null -> entityService.searchEntitiesByName(nameFragment)
                    else -> entityService.getAllEntities()
                }
                call.respond(HttpStatusCode.OK, entities)
            } catch (e: Exception) {
                logger.error(e) { "Error searching entities" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // POST /api/v1/entities
        post {
            logger.info { "POST /api/v1/entities - Creating new entity" }
            try {
                val request = call.receive<CreateEntityRequest>()
                logger.info { "Creating entity: ${request.name}" }
                val entity = entityService.createEntity(request)
                call.respond(HttpStatusCode.Created, entity)
            } catch (e: Exception) {
                logger.error(e) { "Error creating entity" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // PUT /api/v1/entities/{id}
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity ID"))
                return@put
            }

            logger.info { "PUT /api/v1/entities/$id - Updating entity" }
            try {
                val request = call.receive<UpdateEntityRequest>()
                val entity = entityService.updateEntity(id, request)
                if (entity != null) {
                    call.respond(HttpStatusCode.OK, entity)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Entity not found"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error updating entity $id" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // DELETE /api/v1/entities/{id}
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity ID"))
                return@delete
            }

            logger.info { "DELETE /api/v1/entities/$id - Deleting entity" }
            try {
                val deleted = entityService.deleteEntity(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Entity not found"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error deleting entity $id" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // GET /api/v1/entities/{id}/related
        get("/{id}/related") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity ID"))
                return@get
            }

            logger.info { "GET /api/v1/entities/$id/related - Fetching related entities" }
            try {
                val relatedEntities = entityService.getRelatedEntities(id)
                call.respond(HttpStatusCode.OK, relatedEntities)
            } catch (e: Exception) {
                logger.error(e) { "Error fetching related entities for $id" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // POST /api/v1/entities/{fromId}/relationships/{toId}
        post("/{fromId}/relationships/{toId}") {
            val fromId = call.parameters["fromId"]?.toLongOrNull()
            val toId = call.parameters["toId"]?.toLongOrNull()

            if (fromId == null || toId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity IDs"))
                return@post
            }

            logger.info { "POST /api/v1/entities/$fromId/relationships/$toId - Creating relationship" }
            try {
                val created = entityService.createRelationship(fromId, toId)
                if (created) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_ERROR", "Failed to create relationship"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error creating relationship from $fromId to $toId" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // DELETE /api/v1/entities/{fromId}/relationships/{toId}
        delete("/{fromId}/relationships/{toId}") {
            val fromId = call.parameters["fromId"]?.toLongOrNull()
            val toId = call.parameters["toId"]?.toLongOrNull()

            if (fromId == null || toId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid entity IDs"))
                return@delete
            }

            logger.info { "DELETE /api/v1/entities/$fromId/relationships/$toId - Deleting relationship" }
            try {
                entityService.deleteRelationship(fromId, toId)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                logger.error(e) { "Error deleting relationship from $fromId to $toId" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }
    }
}
