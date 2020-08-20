package dev.fritz2

import dev.fritz2.dom.Tag
import org.w3c.dom.HTMLElement

/**
 * A presenter should contains the
 *
 * @param V the type of the presenter's view
 */
interface Presenter<V : View> {
    val token: String
    val view: V

    /** Called once the presenter is created. */
    fun bind() {}

    /**
     * Called each time before the presenter is shown.
     *
     * Override this method if you want to use the data in the [PlaceRequest]
     */
    fun prepareFromRequest(place: PlaceRequest) {}

    /** Called each time after the view has been attached to the DOM. */
    fun show() {}

    /** Called each time before the view is removed from the DOM. */
    fun hide() {}

    companion object {
        @PublishedApi
        internal val registry: MutableMap<String, () -> Presenter<out View>> = mutableMapOf()

        @PublishedApi
        internal val instances: MutableMap<String, Presenter<out View>> = mutableMapOf()

        /** Registers the token to the function creating the presenter (e.g. the presenter constructor) */
        fun register(token: String, presenter: () -> Presenter<out View>) {
            registry[token] = presenter
        }

        /** Checks if the token has been registered. */
        operator fun contains(token: String): Boolean = token in registry

        /**
         * Returns the presenter instance for the given token.
         *
         * If the presenter has been registered, but has not yet been created, the presenter is created by calling
         * the function given at [register]. After that [bind] is called and the instance is returned.
         *
         * If the presenter has already been created, its instance is returned.
         *
         * If no presenter is found for [token], `null` is returned.
         */
        inline fun <reified P : Presenter<out View>> lookup(token: String): P? {
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

interface View {
    val elements: List<Tag<HTMLElement>>
}
