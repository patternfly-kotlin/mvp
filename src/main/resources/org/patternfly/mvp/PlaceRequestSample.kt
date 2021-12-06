@file:Suppress("UNUSED_VARIABLE")

package dev.fritz2.sample

import org.patternfly.mvp.placeRequest

internal interface PlaceRequestSample {

    fun placeRequests() {
        val home = placeRequest("home")

        val users = placeRequest("users", "page" to "2")

        val params = mapOf("foo" to "bar", "bd" to "29")
        val johnDoe = placeRequest("settings") {
            put("id", "0815")
            putAll(params)
        }
    }

    fun marshal() {
        placeRequest("home") // "home"
        placeRequest("users", "page" to "2") // "users;page=2"

        // settings;id=john-doe;foo=bar;bd=29
        val params = mapOf("foo" to "bar", "bd" to "29")
        val johnDoe = placeRequest("settings") {
            put("id", "john-doe")
            putAll(params)
        }
    }
}