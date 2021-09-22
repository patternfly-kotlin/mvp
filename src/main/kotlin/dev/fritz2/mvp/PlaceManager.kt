package dev.fritz2.mvp

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.routing.*
import kotlinx.coroutines.flow.Flow
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement

/**
 * Helper function to build [PlaceRequest]s.
 */
public fun placeRequest(token: String): PlaceRequest =
    PlaceRequest(token)

/**
 * Helper function to build [PlaceRequest]s.
 */
public fun placeRequest(token: String, params: Pair<String, String>): PlaceRequest =
    PlaceRequest(token, mapOf(params))

/**
 * Helper function to build [PlaceRequest]s.
 */
public fun placeRequest(token: String, vararg params: Pair<String, String>): PlaceRequest =
    PlaceRequest(token, mapOf(*params))

/**
 * Helper function to build [PlaceRequest]s.
 */
@ExperimentalStdlibApi
public fun placeRequest(token: String, params: MutableMap<String, String>.() -> Unit = {}): PlaceRequest =
    PlaceRequest(token, buildMap(params))

/**
 * A place request consists of a token and an optional map of parameters.
 *
 * You can use one of the builder functions to create place requests.
 *
 * @sample dev.fritz2.sample.PlaceRequestSample.placeRequests
 */
public data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf()) {

    /**
     * returns the [location hash](https://developer.mozilla.org/en-US/docs/Web/API/Location/hash) of this place request.
     */
    public val hash: String
        get() = "#" + PlaceRequestRoute(this).serialize(this)
}

/**
 * [Route] typed to [PlaceRequest]. Contains the functions to [marshal] and [unmarshal] the [PlaceRequest]s.
 *
 * [PlaceRequest]s are marshaled to strings using the following format:
 *
 * ```
 * token[;key=value]
 * ```
 *
 * @sample dev.fritz2.sample.PlaceRequestSample.marshal
 */
public class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

    /**
     * Unmarshals a string into a [PlaceRequest].
     */
    override fun deserialize(hash: String): PlaceRequest {
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

    /**
     * Marshals a [PlaceRequest] into a string.
     */
    override fun serialize(route: PlaceRequest): String = buildString {
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
public fun <E : HTMLElement> Tag<E>.managedBy(placeManager: PlaceManager) {
    placeManager.manage(this)
}

/**
 * Manages the transition between places and presenters using a [Router] typed to [PlaceRequest].
 *
 * The place manager takes care of finding the right presenter for a place request and calling the presenter's
 * lifecycle methods.
 *
 * The place manager controls a specific element in the DOM tree. When switching from one presenter to another, this
 * element is cleared and filled with the elements of the new presenter's view.
 *
 * @param defaultPlaceRequest the default / initial place request
 * @param notFound a function to show content for places which are not bound to a presenter
 *
 * @sample dev.fritz2.sample.PlaceManagerSample.typicalSetup
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

    /**
     * Flow of [PlaceRequest]s.
     */
    public val placeRequests: Flow<PlaceRequest>
        get() = router.data

    /** The current presenter. */
    public val presenter: Presenter<*>?
        get() = currentPresenter

    /** The current place request. Same as [Router.current] */
    public val placeRequest: PlaceRequest
        get() = currentPlaceRequest ?: defaultPlaceRequest

    internal fun <E : HTMLElement> manage(tag: Tag<E>) {
        mountSingle(tag.job, placeRequests) { placeRequest, _ ->
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
                render(tag.domNode) {
                    presenter.view.content(this)
                    presenter.show()
                }
            } else {
                error = true
                console.error("No presenter found for $nonEmptyPlace!")
                render(tag.domNode) {
                    notFound.invoke(this, nonEmptyPlace)
                }
            }
        }
    }
}
