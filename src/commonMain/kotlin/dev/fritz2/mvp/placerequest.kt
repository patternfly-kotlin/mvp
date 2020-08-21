package dev.fritz2.mvp

/** A place request models consists of a token and an optional map of parameters. */
data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

internal expect fun decodeURIComponent(encodedURI: String): String
internal expect fun encodeURIComponent(decodedURI: String): String

internal class PlaceRequestMarshalling {

    fun unmarshal(hash: String): PlaceRequest {
        val token = hash.substringBefore(';')
        val params = hash.substringAfter(';', "")
            .split(";")
            .filter { it.isNotEmpty() }
            .associate {
                val (left, right) = it.split("=")
                left to decodeURIComponent(right)
            }
        return PlaceRequest(token, params)
    }

    fun marshal(route: PlaceRequest): String = buildString {
        append(route.token)
        if (route.params.isNotEmpty()) {
            route.params
                .map { (key, value) -> "$key=${encodeURIComponent(value)}" }
                .joinTo(this, ";", ";")
        }
    }
}
