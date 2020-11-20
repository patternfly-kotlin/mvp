package dev.fritz2.mvp

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mountDomNodeList
import dev.fritz2.routing.Route
import dev.fritz2.routing.Router
import dev.fritz2.routing.decodeURIComponent
import dev.fritz2.routing.encodeURIComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.Element

/** A place request models consists of a token and an optional map of parameters. */
public data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

internal class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

    override fun unmarshal(hash: String): PlaceRequest {
        val token = hash.substringBefore(';')
        val params = hash.substringAfter(';', "")
            .split(";")
            .filter { it.isNotEmpty() }
            .associate {
                val (key, value) = it.split("=")
                key to decodeURIComponent(value)
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
public class PlaceManager(
    private val default: PlaceRequest,
    private val notFound: RenderContext.(PlaceRequest) -> Unit = {
        h1 {
            +"404"
        }
        p {
            +"${it.token} not found"
        }
    }
) {
    internal var error: Boolean = false
    internal var currentPresenter: Presenter<*>? = null
    private var currentPlaceRequest: PlaceRequest? = null

    public val router: Router<PlaceRequest> = Router(PlaceRequestRoute(default))

    /** The current presenter. */
    public val presenter: Presenter<*>?
        get() = currentPresenter

    /** The current place request. */
    public val placeRequest: PlaceRequest?
        get() = currentPlaceRequest

    /** Specify the tag to use for the presenter's views. */
    public fun <E : Element> manage(tag: Tag<E>) {
        manage(tag.domNode)
    }

    /** Specify the element to use for the presenter's views. */
    @OptIn(ExperimentalCoroutinesApi::class)
    public fun <E : Element> manage(element: E?) {
        if (element != null) {
            mountDomNodeList(Job(), element, router.map { place ->
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
                    render {
                        presenter.view.content(this)
                    }
                } else {
                    error = true
                    console.error("No presenter found for $nonEmptyPlace!")
                    render {
                        notFound(nonEmptyPlace)
                    }
                }
            }.onEach {
                if (!error) {
                    currentPresenter?.show()
                }
            })
        }
    }
}
