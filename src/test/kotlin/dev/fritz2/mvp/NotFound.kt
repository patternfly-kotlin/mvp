package dev.fritz2.mvp

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class NotFoundTests {

    @Test
    fun notfound() = runTest {
        initPresenter()
        initDocument()
        val placeManager = initPlaceManager()

        render {
            ul {
                li {
                    button(id = "apple") {
                        +"apple"
                        clicks.map { PlaceRequest("apple") } handledBy placeManager.router.navTo
                    }
                }
                li {
                    button(id = "404") {
                        +"404"
                        clicks.map { PlaceRequest("404") } handledBy placeManager.router.navTo
                    }
                }
            }
        }.mount(navigationId)

        val undefinedLink = document.getElementById("404") as HTMLButtonElement
        val appleLink = document.getElementById("apple") as HTMLButtonElement

        // initial place
        delay(wait)
        assertEquals("🍎", content())

        undefinedLink.click()
        delay(wait)
        assertEquals("💣", content())

        appleLink.click()
        delay(wait)
        assertEquals("🍎", content())
    }
}
