# Kotlin/JS MVP

[![GitHub Super-Linter](https://github.com/patternfly-kotlin/patternfly-kotlin/workflows/lint/badge.svg)](https://github.com/marketplace/actions/super-linter)
[![Detekt](https://github.com/patternfly-kotlin/patternfly-kotlin/workflows/detekt/badge.svg)](https://detekt.github.io/detekt/index.html)
[![Build Passing](https://github.com/patternfly-kotlin/patternfly-kotlin/workflows/build/badge.svg)](https://github.com/patternfly-kotlin/mvp/actions)
[![API Docs](https://img.shields.io/badge/api-docs-brightgreen)](https://patternfly-kotlin.github.io/mvp/mvp/)
[![IR](https://img.shields.io/badge/Kotlin%2FJS-IR%20supported-yellow)](https://kotl.in/jsirsupported)
[![Download](https://img.shields.io/maven-central/v/org.patternfly/patternfly-kotlin)](https://search.maven.org/search?q=g:org.patternfly%20AND%20a:mvp)

Kotlin MVP implementation based on [fritz2](https://www.fritz2.dev/).

## Building Blocks

MVP comes with just a few classes and provides a very lightweight and straight forward MVP implementation:

### Model

You are free to use any kind of model you want to. There are no restrictions in the API. Usually you'd use some kind 
of store or data classes which you create or fetch in the presenter. 

### View

The view is a simple interface with just a single property you need to implement:

```kotlin
interface View {
    val content: ViewContent
}
``` 

`ViewContent` is a type alias for `RenderContext.() -> Unit`.

A view should just define the visual representation and should not contain business logic. A view is always bound to a specific presenter. If you need a reference to the presenter in the view, you can implement an additional interface:

```kotlin
interface WithPresenter<P : Presenter<View>> {
    val presenter: P
}
```

Here's an example how to use it:

```kotlin
class AppleView(override val presenter: ApplePresenter) :
    View, WithPresenter<ApplePresenter> {

    override val content: ViewContent = {
        p { +"🍎" }
    }
}

class ApplePresenter : Presenter<AppleView> {
    override val view = AppleView(this)
}
```

### Presenter

The presenter is a simple interface with one property you need to implement. Besides, the presenter provides methods 
which you can override to take part in the presenter's lifecycle.  

```kotlin
interface Presenter<out V : View> {
    val view: V

    fun bind() {}
    fun prepareFromRequest(place: PlaceRequest) {}
    fun show() {}
    fun hide() {}
}
``` 

A presenter should contain the business logic for a specific use case. It should not contain any view related code
like (web) components or DOM elements. Instead, it should focus on the actual use case, work on the model, listen to
events and update its view.

Presenters are singletons which are created lazily and which are then reused. They're bound to a specific token 
(aka place). They need to be registered using that token, and a function to create the presenter.

```kotlin
class AppleView : View {
    override val content: ViewContent = {
        p { +"🍎" }
    }
}

class ApplePresenter : Presenter<AppleView> {
    override val view = AppleView()
}

Presenter.register("apple", ::ApplePresenter)
```

#### Lifecycle

Presenters are managed by a place manager (see below). Override one of the presenter methods to take part 
in the lifecycle of a presenter: 

1. `bind()`  
    Called once, after the presenter has been created. Override this method to execute one-time setup code.
    
1. `prepareFromRequest(request: PlaceRequest)`  
    Called each time, before the presenter is shown. Override this method if you want to use the data in the 
    place request.

1. `show()`  
    Called each time *after* the view has been attached to the DOM.

1. `hide()`  
    Called each time *before* the view is removed from the DOM.

### Place Management

To navigate between presenters and its views use place requests, and the place manager which is based on 
fritz2 [routing](https://docs.fritz2.dev/Routing.html).

A place request is a simple data class with a token and an optional map of parameters:

```kotlin
data class PlaceRequest(val token: String, val params: Map<String, String> = mapOf())
```

Place requests can be created using factory functions and are (de)serialized to URL fragments:

```kotlin
// #apple
placeRequest("apple")

// #apple;type=red-delicious
placeRequest("apple", "type" to "red-delicious")

// #apple;type=granny-smith;size=xxl
placeRequest("apple") {
    put("type", "granny-smith")
    put("size", "xxl")
} 
``` 

Place requests are handled by the place manager. There should be only one place manager per application. It is created by specifying a default place, and a function which is used if no presenter could be found for the requested place:

```kotlin
val placeManager = PlaceManager(placeRequest("apple")) {
    h1 { +"No fruits here" }
    p { +"I don't know ${it.token}!" }
}
```

The place manager contains a `Router<PlaceRequest>` which you can use to navigate to places:

```kotlin
val placeManager = PlaceManager(placeRequest("apple"))

render {
    button {
        +"apple"
        clicks.map { placeRequest("apple") } handledBy placeManager.router.navTo
    }
}
```

Finally, you have to specify a tag which is used by the place manager to show the elements of the views:

```kotlin
val placeManager = PlaceManager(placeRequest("apple"))

render {
    nav {
        ul {
            li { a { href("#apple") } }
            li { a { href("#banana") } }
            li { a { href("#pineapple") } }
        }
    }
    main {
        managedBy(placeManager)
    }
}
```  
  
When a place request is handled by the place manager, 

1. the place manager tries to find the presenter which matches the place request's token
1. creates and binds the presenter (if necessary)
1. calls `Presenter.hide()` for the current presenter (if any)
1. calls `Presenter.prepareFromRequest()` for the new presenter using the current place request
1. clears the element managed by the place manager
1. attaches the elements of the new presenter's view
1. calls `Presenter.show()` for the new presenter

## Sample

The following code snippet contains a small example with three presenter / view tuples:

```kotlin
class AppleView : View {
    override val content: ViewContent = {
        p { +"🍎" }
    }
}

class ApplePresenter : Presenter<AppleView> {
    override val view = AppleView()
}

class BananaView : View {
    override val content: ViewContent = {
        p { +"🍌" }
    }
}

class BananaPresenter : Presenter<BananaView> {
    override val view = BananaView()
}

class PineappleView : View {
    override val content: ViewContent = {
        p { +"🍍" }
    }
}

class PineapplePresenter : Presenter<PineappleView> {
    override val view = PineappleView()
}

fun main() {
    Presenter.register("apple", ::ApplePresenter)
    Presenter.register("banana", ::BananaPresenter)
    Presenter.register("pineapple", ::PineapplePresenter)

    val placeManager = PlaceManager(PlaceRequest("apple")) {
        p { +"💣" }
    }

    render {
        main {
            nav {
                arrayOf("apple", "banana", "pineapple").forEach {
                    button {
                        +it
                        clicks.map { placeRequest(it) } handledBy placeManager.router.navTo
                    }
                }
            }
            section {
                managedBy(placeManager)
            }
        }
    }
}
```

## Credits

Most of the concepts are taken from [GWTP](https://github.com/ArcBees/GWTP), a great MVP implementation for [GWT](http://www.gwtproject.org/). Kudos to the GWTP team!
