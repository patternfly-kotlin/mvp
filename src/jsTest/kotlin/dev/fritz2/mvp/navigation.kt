package dev.fritz2.mvp

import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationTests {

    @Test
    fun click() = runTest {
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
                    button(id = "banana") {
                        +"banana"
                        clicks.map { PlaceRequest("banana") } handledBy placeManager.router.navTo
                    }
                }
                li {
                    button(id = "pineapple") {
                        +"pineapple"
                        clicks.map { PlaceRequest("pineapple") } handledBy placeManager.router.navTo
                    }
                }
            }
        }.mount(navigationId)

        val appleLink = document.getElementById("apple") as HTMLButtonElement
        val bananaLink = document.getElementById("banana") as HTMLButtonElement
        val pineappleLink = document.getElementById("pineapple") as HTMLButtonElement

        // initial place
        delay(wait)
        assertEquals("🍎", content())

        bananaLink.click()
        delay(wait)
        assertEquals("🍌", content())

        pineappleLink.click()
        delay(wait)
        assertEquals("🍍", content())

        appleLink.click()
        delay(wait)
        assertEquals("🍎", content())
    }

    @Test
    fun link() = runTest {
        initPresenter()
        initDocument()
        initPlaceManager()

        render {
            ul {
                li {
                    a(id = "apple") {
                        +"apple"
                        href = const("#apple")
                    }
                }
                li {
                    a(id = "banana") {
                        +"banana"
                        href = const("#banana")
                    }
                }
                li {
                    a(id = "pineapple") {
                        +"pineapple"
                        href = const("#pineapple")
                    }
                }
            }
        }.mount(navigationId)

        val appleLink = document.getElementById("apple") as HTMLAnchorElement
        val bananaLink = document.getElementById("banana") as HTMLAnchorElement
        val pineappleLink = document.getElementById("pineapple") as HTMLAnchorElement

        // initial place
        delay(wait)
        assertEquals("🍎", content())

        bananaLink.click()
        delay(wait)
        assertEquals("🍌", content())

        pineappleLink.click()
        delay(wait)
        assertEquals("🍍", content())

        appleLink.click()
        delay(wait)
        assertEquals("🍎", content())
    }
}
