package dev.fritz2.sample

import dev.fritz2.dom.html.render
import org.patternfly.mvp.PlaceManager
import org.patternfly.mvp.managedBy
import org.patternfly.mvp.placeRequest

internal interface PlaceManagerSample {

    fun typicalSetup() {
        val placeManager = PlaceManager(placeRequest("apple")) {
            h1 { +"No fruits here" }
            p { +"I don't know ${it.token}!" }
        }
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