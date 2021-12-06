package org.patternfly.mvp

import dev.fritz2.dom.html.render
import kotlinx.browser.document
import org.patternfly.mvp.PresenterState.BIND
import org.patternfly.mvp.PresenterState.HIDE
import org.patternfly.mvp.PresenterState.PREPARE_FROM_REQUEST
import org.patternfly.mvp.PresenterState.SHOW

// ------------------------------------------------------ constants

const val navigationId = "navigation"
const val navigationSelector = "#$navigationId"
const val contentId = "content"
const val wait = 200L

// ------------------------------------------------------ html

fun content(): String? = document.getElementById(contentId)?.textContent

// ------------------------------------------------------ mvp

fun initPresenter() {
    Presenter.registry.clear()
    Presenter.instances.clear()
    Presenter.register("apple", ::ApplePresenter)
    Presenter.register("banana", ::BananaPresenter)
    Presenter.register("pineapple", ::PineapplePresenter)
}

fun initPlaceManager(): PlaceManager {
    val placeManager = PlaceManager(PlaceRequest("apple")) {
        p(id = contentId) { +"💣" }
    }

    document.clear()
    render {
        nav(id = navigationId) {}
        main {
            managedBy(placeManager)
        }
    }
    return placeManager
}

enum class PresenterState {
    BIND, PREPARE_FROM_REQUEST, SHOW, HIDE
}

abstract class RecordingPresenter<V : View> : Presenter<V> {
    internal val state: MutableList<PresenterState> = mutableListOf()

    override fun bind() {
        state += BIND
    }

    override fun prepareFromRequest(place: PlaceRequest) {
        state += PREPARE_FROM_REQUEST
    }

    override fun show() {
        state += SHOW
    }

    override fun hide() {
        state += HIDE
    }
}

class ApplePresenter : RecordingPresenter<AppleView>() {
    override val view: AppleView = AppleView()
}

class AppleView : View {
    override val content: ViewContent = {
        p(id = contentId) { +"🍎" }
    }
}

class BananaPresenter : RecordingPresenter<BananaView>() {
    override val view: BananaView = BananaView()
}

class BananaView : View {
    override val content: ViewContent = {
        p(id = contentId) { +"🍌" }
    }
}

class PineapplePresenter : RecordingPresenter<PineappleView>() {
    override val view: PineappleView = PineappleView()
}

class PineappleView : View {
    override val content: ViewContent = {
        p(id = contentId) { +"🍍" }
    }
}
