package dev.fritz2

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

data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())

internal class PlaceRequestRoute(override val default: PlaceRequest) : Route<PlaceRequest> {

    override fun marshal(route: PlaceRequest): String = buildString {
        append(route.token)
        if (route.params.isNotEmpty()) {
            route.params
                .map { (key, value) -> "$key=${encodeURIComponent(value)}" }
                .joinTo(this, ";", ";")
        }
    }

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
}

class PlaceManager(private val default: PlaceRequest, private val notFound: () -> Tag<HTMLElement>) {

    internal var error: Boolean = false
    internal var target: Element? = null
    internal var currentPresenter: Presenter<*>? = null
    private var currentPlaceRequest: PlaceRequest? = null

    val router: Router<PlaceRequest> = Router(PlaceRequestRoute(default))

    val presenter: Presenter<*>?
        get() = currentPresenter

    val placeRequest: PlaceRequest?
        get() = currentPlaceRequest

    fun <E : Element> manage(tag: Tag<E>) {
        manage(tag.domNode)
    }

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

internal fun Flow<List<Tag<HTMLElement>>>.bind(placeManager: PlaceManager) =
    PlaceManagerMountPoint(this, placeManager)

internal class PlaceManagerMountPoint(upstream: Flow<List<Tag<HTMLElement>>>, private val placeManager: PlaceManager) :
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