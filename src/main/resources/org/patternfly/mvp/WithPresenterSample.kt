@file:Suppress("UNUSED_VARIABLE")

package dev.fritz2.sample

import org.patternfly.mvp.Presenter
import org.patternfly.mvp.View
import org.patternfly.mvp.ViewContent
import org.patternfly.mvp.WithPresenter


internal interface WithPresenterSample {

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
