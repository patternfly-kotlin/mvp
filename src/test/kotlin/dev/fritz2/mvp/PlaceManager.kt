package dev.fritz2.mvp

import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import kotlinx.browser.document
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertEquals

@InternalCoroutinesApi
class PlaceManagerTests {

    @Test
    fun placeRequest() = runTest {

        initPresenter()
        val placeManager = initPlaceManager()

        val applePlaceRequest = PlaceRequest("apple")
        val redDeliciousPlaceRequest = PlaceRequest("apple", mapOf("type" to "red-delicious"))
        val galaPlaceRequest = PlaceRequest("apple", mapOf("type" to "gala"))
        val grannySmithPlaceRequest = PlaceRequest("apple", mapOf("type" to "granny-smith", "size" to "xxl"))

        render {
            ul {
                li {
                    button(id = "red-delicious") {
                        +"red delicious"
                        clicks.map { redDeliciousPlaceRequest } handledBy placeManager.router.navTo
                    }
                }
                li {
                    button(id = "gala") {
                        +"gala"
                        clicks.map { galaPlaceRequest } handledBy placeManager.router.navTo
                    }
                }
                li {
                    button(id = "granny-smith") {
                        +"granny smith"
                        clicks.map { grannySmithPlaceRequest } handledBy placeManager.router.navTo
                    }
                }
            }
        }.mount(navigationId)
        delay(wait)

        val redDeliciousLink = document.getElementById("red-delicious") as HTMLButtonElement
        val galaLink = document.getElementById("gala") as HTMLButtonElement
        val grannySmithLink = document.getElementById("granny-smith") as HTMLButtonElement
        val applePresenter: ApplePresenter? = Presenter.lookup("apple")

        // initial place
        assertEquals(applePlaceRequest, placeManager.placeRequest)
        assertEquals(applePresenter, placeManager.presenter)
        assertEquals("#apple", document.location?.hash)

        redDeliciousLink.click()
        delay(wait)
        assertEquals(redDeliciousPlaceRequest, placeManager.placeRequest)
        assertEquals(applePresenter, placeManager.presenter)
        assertEquals("#apple;type=red-delicious", document.location?.hash)

        galaLink.click()
        delay(wait)
        assertEquals(galaPlaceRequest, placeManager.placeRequest)
        assertEquals(applePresenter, placeManager.presenter)
        assertEquals("#apple;type=gala", document.location?.hash)

        grannySmithLink.click()
        delay(wait)
        assertEquals(grannySmithPlaceRequest, placeManager.placeRequest)
        assertEquals(applePresenter, placeManager.presenter)
        assertEquals("#apple;type=granny-smith;size=xxl", document.location?.hash)
    }
}
