package dev.fritz2.mvp

import org.w3c.dom.Element

/**
 * A presenter should contain the business logic for a specific use case. It should not contain any view related code
 * like (web) components or DOM elements. Instead, it should focus on the actual use case, work on the model, listen to
 * events and update its view.
 *
 * Presenters are singletons which are created lazily and which are then reused. They're bound to a specific string
 * token (aka place). They need to be registered using a token, and a function to create the presenter.
 * Use the presenter's companion object to register presenters:
 *
 * ```
 * class ApplePresenter : Presenter<AppleView> {
 *     override val view = AppleView()
 * }
 *
 * class AppleView : View {
 *     override val elements = listOf(
 *         render {
 *             p { +"🍎" }
 *         })
 * }
 *
 * Presenter.register("apple", ::ApplePresenter)
 * ```
 *
 * @param V the type of the presenter's view
 */
public interface Presenter<out V : View> {
    public val view: V

    /** Called once, after the presenter has been created. Override this method to implement one-time setup code. */
    public fun bind() {}

    /**
     * Called each time before the presenter is shown (before [show] is called).
     *
     * Override this method if you want to use the data in [PlaceRequest].
     */
    public fun prepareFromRequest(place: PlaceRequest) {}

    /** Called each time after the view has been attached to the DOM (after [prepareFromRequest]). */
    public fun show() {}

    /** Called each time before the view is removed from the DOM. */
    public fun hide() {}

    /** Registry for all presenters. Used to register and find presenters. */
    public companion object {
        @PublishedApi
        internal val registry: MutableMap<String, () -> Presenter<View>> = mutableMapOf()

        @PublishedApi
        internal val instances: MutableMap<String, Presenter<View>> = mutableMapOf()

        /** Registers the function to create a presenter to a specific token. */
        public fun register(token: String, presenter: () -> Presenter<View>) {
            registry[token] = presenter
        }

        /** Checks if the token has been registered. */
        public operator fun contains(token: String): Boolean = token in registry

        /**
         * Returns the presenter instance for the given token.
         *
         * If the presenter has been registered, but has not yet been created, the presenter is created by calling
         * the function given at [register]. After that [Presenter.bind] is called and the instance is returned.
         *
         * If the presenter has already been created, its instance is returned.
         *
         * If no presenter is found for [token], `null` is returned.
         */
        public inline fun <reified P : Presenter<View>> lookup(token: String): P? {
            return if (token in instances) {
                val presenter = instances[token]
                if (presenter is P) {
                    presenter
                } else {
                    null
                }
            } else {
                if (token in registry) {
                    registry[token]?.invoke()?.let {
                        if (it is P) {
                            instances[token] = it
                            it.bind()
                            it
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }
        }
    }
}

/**
 * A view should just define the visual representation and should not contain business logic. A view is always bound
 * to a specific [Presenter].
 */
public interface View {

    /** A list of elements defining the visual representation of the view. */
    public val elements: List<Element>
}
