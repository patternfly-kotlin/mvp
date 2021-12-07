package org.patternfly.mvp.sample

import org.patternfly.mvp.Presenter
import org.patternfly.mvp.View
import org.patternfly.mvp.ViewContent

internal class PresenterSample {

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