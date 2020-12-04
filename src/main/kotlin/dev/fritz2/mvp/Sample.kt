@file:Suppress("UNUSED_VARIABLE")

package dev.fritz2.mvp

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

        Presenter.register("apple", ::ApplePresenter)
    }
}

internal interface WithPresenterSamples {

    // object used for documentation purposes only
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
