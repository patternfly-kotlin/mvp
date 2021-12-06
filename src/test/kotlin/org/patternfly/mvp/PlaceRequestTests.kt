package org.patternfly.mvp

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceRequestTests {

    @Test
    fun empty() {
        assertPlaceRequest("", PlaceRequest(""))
    }

    @Test
    fun token() {
        assertPlaceRequest("foo", PlaceRequest("foo"))
    }

    @Test
    fun params() {
        assertPlaceRequest("foo;a=1;b=2", PlaceRequest("foo", mapOf("a" to "1", "b" to "2")))
    }

    private fun assertPlaceRequest(hash: String, placeRequest: PlaceRequest) {
        val prr = PlaceRequestRoute(placeRequest)
        assertEquals(hash, prr.serialize(placeRequest))
        assertEquals(placeRequest, prr.deserialize(hash))
    }
}
