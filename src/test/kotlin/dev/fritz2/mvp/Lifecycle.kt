package dev.fritz2.mvp

import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.mvp.PresenterState.BIND
import dev.fritz2.mvp.PresenterState.HIDE
import dev.fritz2.mvp.PresenterState.PREPARE_FROM_REQUEST
import dev.fritz2.mvp.PresenterState.SHOW
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLButtonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class LifecycleTests {

    @Test
    fun lifecycle() = runTest {
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
                    button(id = "gala") {
                        +"gala"
                        clicks.map {
                            PlaceRequest(
                                "apple",
                                mapOf("type" to "gala")
                            )
                        } handledBy placeManager.router.navTo
                    }
                }
                li {
                    button(id = "banana") {
                        +"banana"
                        clicks.map { PlaceRequest("banana") } handledBy placeManager.router.navTo
                    }
                }
            }
        }.mount(navigationId)

        val appleLink = document.getElementById("apple") as HTMLButtonElement
        val galaLink = document.getElementById("gala") as HTMLButtonElement
        val bananaLink = document.getElementById("banana") as HTMLButtonElement
        val applePresenter = Presenter.lookup<ApplePresenter>("apple")!!
        val bananaPresenter = Presenter.lookup<BananaPresenter>("banana")!!
        val appleState = mutableListOf<PresenterState>()
        val bananaState = mutableListOf<PresenterState>()

        // initial place
        delay(wait)
        assertState(applePresenter, appleState, BIND, PREPARE_FROM_REQUEST, SHOW)

        // same presenter, but different place request
        galaLink.click()
        delay(wait)
        assertState(applePresenter, appleState, PREPARE_FROM_REQUEST, SHOW)

        // switch to banana
        bananaLink.click()
        delay(wait)
        assertState(applePresenter, appleState, HIDE)
        assertState(bananaPresenter, bananaState, BIND, PREPARE_FROM_REQUEST, SHOW)

        // back to apple
        appleLink.click()
        delay(wait)
        assertState(applePresenter, appleState, PREPARE_FROM_REQUEST, SHOW)
        assertState(bananaPresenter, bananaState, HIDE)
    }

    private fun assertState(
        presenter: RecordingPresenter<*>,
        state: MutableList<PresenterState>,
        vararg push: PresenterState
    ) {
        state.addAll(listOf(*push))
        assertEquals(state, presenter.state)
    }
}
