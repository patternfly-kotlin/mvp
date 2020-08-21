package dev.fritz2.mvp

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceRequestMarshallingTest {

    private val marshalling = PlaceRequestMarshalling()

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
        assertEquals(hash, marshalling.marshal(placeRequest))
        assertEquals(placeRequest, marshalling.unmarshal(hash))
    }
}
