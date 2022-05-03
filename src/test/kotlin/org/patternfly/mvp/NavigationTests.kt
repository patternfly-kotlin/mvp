package org.patternfly.mvp

import dev.fritz2.dom.html.render
import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertEquals

@DelicateCoroutinesApi
class NavigationTests {

    @Test
    fun click() = runTest {
        initPresenter()
        val placeManager = initPlaceManager()

        render(navigationSelector) {
            ul {
                li {
                    button(id = "apple") {
                        +"apple"
                        clicks.map { PlaceRequest("apple") } handledBy placeManager.navTo
                    }
                }
                li {
                    button(id = "banana") {
                        +"banana"
                        clicks.map { PlaceRequest("banana") } handledBy placeManager.navTo
                    }
                }
                li {
                    button(id = "pineapple") {
                        +"pineapple"
                        clicks.map { PlaceRequest("pineapple") } handledBy placeManager.navTo
                    }
                }
            }
        }
        delay(wait)

        val appleLink = document.getElementById("apple") as HTMLButtonElement
        val bananaLink = document.getElementById("banana") as HTMLButtonElement
        val pineappleLink = document.getElementById("pineapple") as HTMLButtonElement

        // initial place
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
        initPlaceManager()

        render(navigationSelector) {
            ul {
                li {
                    a(id = "apple") {
                        +"apple"
                        href("#apple")
                    }
                }
                li {
                    a(id = "banana") {
                        +"banana"
                        href("#banana")
                    }
                }
                li {
                    a(id = "pineapple") {
                        +"pineapple"
                        href("#pineapple")
                    }
                }
            }
        }

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
