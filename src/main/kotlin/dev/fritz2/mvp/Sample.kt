@file:Suppress("UNUSED_VARIABLE")

package dev.fritz2.mvp

import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render

internal interface PlaceManagerSamples {

    fun typicalSetup() {
        val placeManager = PlaceManager(PlaceRequest("apple"))
        render {
            nav {
                ul {
                    li { a { href("#apple") } }
                    li { a { href("#banana") } }
                    li { a { href("#pineapple") } }
                }
            }
            main {
                managedBy(placeManager)
            }
        }
    }
}

internal interface PlaceRequestSamples {

    fun placeRequests() {
        val home = placeRequest("home")

        val users = placeRequest("users", "page" to "2")

        val params = mapOf<String, String>() // other params
        val johnDoe = placeRequest("user") {
            put("id", "john-doe")
            putAll(params)
        }
    }
}

internal interface PresenterSamples {

    fun presenterView() {
        class AppleView : View {
            override val content: ViewContent = {
                p { +"🍎" }
            }
        }

        class ApplePresenter : Presenter<AppleView> {
            override val view = AppleView()
        }
    }
}

internal interface WithPresenterSamples {

    // object used just for documentation purposes
    object WithPresenterSample {

        class AppleView(override val presenter: ApplePresenter) :
            View, WithPresenter<ApplePresenter> {

            override val content: ViewContent = {
                p { +"🍎" }
            }
        }

        class ApplePresenter : Presenter<AppleView> {
            override val view = AppleView(this)
        }
    }
}
