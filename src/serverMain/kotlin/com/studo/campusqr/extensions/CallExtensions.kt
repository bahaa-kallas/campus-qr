package com.studo.campusqr.extensions

import com.studo.campusqr.common.ClientPayload
import com.studo.katerbase.JsonHandler
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*

suspend fun ApplicationCall.respondOk() = respond(HttpStatusCode.OK, "ok")
suspend fun ApplicationCall.respondForbidden() = respond(HttpStatusCode.Forbidden, "forbidden")

// Error responses are still HttpStatusCode.OK because they are handled in the js code
suspend fun ApplicationCall.respondError(message: String) = respond(HttpStatusCode.OK, message)

// Special handling for Enums, we just represent them as Strings
suspend fun <T : Enum<T>> ApplicationCall.respondEnum(enum: T, status: HttpStatusCode? = null) =
    respondText(enum.name, status = status)

// Convert response using Jackson
private suspend fun ApplicationCall.respondAnyObject(payload: Any) {
  val json = JsonHandler.toJsonString(payload)
  respondText(json, ContentType.Application.Json)
}

suspend fun ApplicationCall.respondObject(payload: ClientPayload) = respondAnyObject(payload)
suspend fun ApplicationCall.respondObject(payload: List<ClientPayload>) = respondAnyObject(payload)

// Either "de" or "en"
val ApplicationCall.language: String
  get() {
    val cookieLang = request.cookies["MbLang"]
    return if (cookieLang != null) {
      cookieLang

    } else {
      val acceptLanguages = request.headers["accept-language"]?.toLowerCase()?.split(';', ',') ?: emptyList()
      when {
        "de" in acceptLanguages -> "de"
        else -> "en"
      }
    }
  }

/**
 * Loads the request body, and converts from JSON to a map,
 * This can be only called once per ApplicationCall
 */
suspend fun ApplicationCall.receiveJsonMap(): Map<String, String> = JsonHandler.fromJson(receiveText())