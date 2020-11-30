package dev.fritz2.mvp

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.routing.Route
import dev.fritz2.routing.Router
import dev.fritz2.routing.decodeURIComponent
import dev.fritz2.routing.encodeURIComponent
import dev.fritz2.routing.router
import kotlinx.dom.clear
import org.w3c.dom.Element

/** A place request consists of a token and an optional map of parameters. */
public data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

public class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

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
 * Specifies the tag to use for the presenter's views.
 *
 * @receiver The [tag][WithDomNode] which is managed by the [PlaceManager].
 */
public fun <E : Element> Tag<E>.managedBy(placeManager: PlaceManager) {
    placeManager.manage(this)
}

/**
 * Manages the transition between places and bound presenters using a [Router] typed to [PlaceRequest].
 *
 * The place manager takes care of finding the right presenter for a place request and calling the presenter's
 * lifecycle methods.
 *
 * The place manager controls a specific element in the DOM tree. When switching from one presenter to another, this
 * element is cleared and filled with the elements of the new presenter's view.
 *
 * A typical setup might look like this
 *
 * @sample PlaceManagerSamples.typicalSetup
 */
public class PlaceManager(
    private val defaultPlaceRequest: PlaceRequest,
    private val notFound: RenderContext.(PlaceRequest) -> Unit = {
        h1 { +"404" }
        p { +"${it.token} not found" }
    }
) {

    private var error: Boolean = false
    private var currentPresenter: Presenter<*>? = null
    private var currentPlaceRequest: PlaceRequest? = null

    /** Provides access to the router. */
    public val router: Router<PlaceRequest> = router(PlaceRequestRoute(defaultPlaceRequest))

    /** The current presenter. */
    public val presenter: Presenter<*>?
        get() = currentPresenter

    /** The current place request. Same as [Router.current] */
    public val placeRequest: PlaceRequest
        get() = currentPlaceRequest ?: defaultPlaceRequest

    internal fun <E : Element> manage(tag: Tag<E>) {
        mountSingle(tag.job, router.data) { placeRequest, _ ->
            error = false
            tag.domNode.clear()

            val nonEmptyPlace = if (placeRequest.token.isEmpty()) defaultPlaceRequest else placeRequest
            val presenter = Presenter.lookup<Presenter<View>>(nonEmptyPlace.token)
            if (presenter != null) {
                if (presenter !== currentPresenter) {
                    currentPresenter?.hide()
                }
                currentPresenter = presenter
                currentPlaceRequest = nonEmptyPlace
                presenter.prepareFromRequest(placeRequest)
                render {
                    presenter.view.content.invoke(tag)
                    presenter.show()
                }
            } else {
                error = true
                console.error("No presenter found for $nonEmptyPlace!")
                render {
                    notFound.invoke(tag, nonEmptyPlace)
                }
            }
        }
    }
}
