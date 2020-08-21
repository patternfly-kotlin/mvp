package dev.fritz2.mvp

import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.dom.Tag
import dev.fritz2.routing.Route
import dev.fritz2.routing.Router
import dev.fritz2.routing.decodeURIComponent
import dev.fritz2.routing.encodeURIComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

/** A place request models consists of a token and an optional map of parameters. */
data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

internal class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

    override fun unmarshal(hash: String): PlaceRequest {
        val token = hash.substringBefore(';')
        val params = hash.substringAfter(';', "")
            .split(";")
            .filter { it.isNotEmpty() }
            .associate {
                val (left, right) = it.split("=")
                left to decodeURIComponent(right)
            }
        return PlaceRequest(token, params)
    }

    override fun marshal(route: PlaceRequest): String = buildString {
        append(route.token)
        if (route.params.isNotEmpty()) {
            route.params
                .map { (key, value) -> "$key=${encodeURIComponent(value)}" }
                .joinTo(this, ";", ";")
        }
    }
}

/**
 * Manages the transition between places using a [Router] typed to [PlaceRequest].
 *
 * The place manager takes care of finding the right presenter for a place request and calling the presenter's
 * lifecycle methods.
 *
 * The place manager controls a specific element in the DOM tree. When switching from one presenter to another, the
 * element is cleared and filled with the elements of the new presenter's view.
 */
class PlaceManager(private val default: PlaceRequest, private val notFound: () -> Tag<HTMLElement>) {

    internal var error: Boolean = false
    internal var target: Element? = null
    internal var currentPresenter: Presenter<*>? = null
    private var currentPlaceRequest: PlaceRequest? = null

    val router: Router<PlaceRequest> = Router(PlaceRequestRoute(default))

    /** The current presenter. */
    val presenter: Presenter<*>?
        get() = currentPresenter

    /** The current place request. */
    val placeRequest: PlaceRequest?
        get() = currentPlaceRequest

    /** Specify the tag to use for the presenter's views. */
    fun <E : Element> manage(tag: Tag<E>) {
        manage(tag.domNode)
    }

    /** Specify the element to use for the presenter's views. */
    fun <E : Element> manage(element: E?) {
        target = element
        router.map { place ->
            error = false
            val nonEmptyPlace = if (place.token.isEmpty()) default else place
            val presenter = Presenter.lookup<Presenter<View>>(nonEmptyPlace.token)
            if (presenter != null) {
                if (presenter !== currentPresenter) {
                    currentPresenter?.hide()
                }
                currentPresenter = presenter
                currentPlaceRequest = nonEmptyPlace
                presenter.prepareFromRequest(place)
                presenter.view.elements
            } else {
                error = true
                console.error("No presenter found for $nonEmptyPlace!")
                listOf(notFound())
            }
        }.bind(this)
    }
}

private fun Flow<List<Tag<HTMLElement>>>.bind(placeManager: PlaceManager) =
    PlaceManagerMountPoint(this, placeManager)

private class PlaceManagerMountPoint(upstream: Flow<List<Tag<HTMLElement>>>, private val placeManager: PlaceManager) :
    SingleMountPoint<List<Tag<HTMLElement>>>(upstream) {

    override fun set(value: List<Tag<HTMLElement>>, last: List<Tag<HTMLElement>>?) {
        placeManager.target?.let {
            it.clear()
            value.forEach { tag ->
                it.appendChild(tag.domNode)
            }
        }
        if (!placeManager.error) {
            placeManager.currentPresenter?.show()
        }
    }
}
