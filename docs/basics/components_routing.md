# Routing Component

## Prerequire

For usage of the Routing Component, you should use a DI system, and you should use `RoutingPlugin` in the DI System.

`RoutingPlugin` is fount under package `linden.flowers.di`. So..


``` scala
import linden.di.DI
import linden.flowers.di.RoutingPlugin

val di = new DI:
  use(RoutingPlugin)
```

## Usage

### Declare a router

For example, we are given three components, and we'd like to reach that these components will be seen through three different URLs in our application.

We need the `Router` and `Rout` classes from package linden.flowers. The `Router` class is a Component; hence we can put into html definition with operator `+`. The different behaviors can be added to `Router` using the `Rout`s.

The first parameter of Rout is a URL matcher. The Rout will be fire if its url is matching for the current URL of the browser. If not, the system will ignore this Rout, and it is stepping into the next Rout. The second parameter of Rout is a function that creates a pair; as a returned value from a parameter argument. The parameter is created from the URL matcher. The first element of response pair is a unique value, and the second element is a Component to display.

``` scala
import linden.flowers.{Router, Rout}
(...)
+Router(
  Rout("/a") { _ =>
    "A" -> AComponent()
  },
  Rout("/b") { _ =>
    "B" -> BComponent()
  },
  Rout("/c") { _ =>
    "C" -> CComponent()
  }
)
(...)
```

URL matcher can contain parameterized item and splash characters too. Parameterized item (`:<keyname>`) such a text that doesn't contain */* character. Spach item (`**`) can contains all characters.

``` scala
Rout("/profil/:username/edit") { params =>
  val username = params("username")
  s"Profil$username" -> ProfilComponent(username)
}
```

Unique string from the first element of response pair is a crucial point for controlling change. If the URL of the browser changed, but this generated or responded key is the same as the last key, the system doesn't change the dom tree.

For example, if in our sample, the keys of `AComponent` and `CComponent` are the same, and we try to open `CComponent` from `AComponent` or open `AComponent` from `CComponent`, then nothing happens.

``` scala
import linden.flowers.{Router, Rout}
(...)
+Router(
  Rout("/a") { _ =>
    "x" -> AComponent()
  },
  Rout("/b") { _ =>
    "B" -> BComponent()
  },
  Rout("/c") { _ =>
    "x" -> CComponent()
  }
)
(...)
```

### Navigation

When we want to change the URL, we need to use class `linden.flowers.BrowserLocator`. DI is necesary for this object. `BrowserLocator` has three public functions and one read only value.

**Public functions**:

| Name                     | Description                     |
|---|---|
| `to(value: String): Unit` | change URL of browser, go to the value URL | 
| `back(): Unit` | back to previous URL |
| `update(): Unit` | force update request  |

!> The `update()` function is a deep-technical function. You don't need to use this!

**Public value**:

| Name | Description |
|---|---|
| `url: ReadStore[String]` | current handled URL by system |


We have three different Components with different own URL in our example so that we will create a navigation function for each action using the function `to` of `BrowserLocator` for navigation.

``` scala
import linden.flowers.{BrowserLocator, Component, Rout, Router}

class NyNavigationComponent(locator: BrowserLocator) extends Component:
  private def goToA() = locator.to("/a")

  private def goToB() = locator.to("/b")

  private def goToC() = locator.to("/c")
```

Finally, we can add the navigation functions to the click events.

``` scala
(...)
ul {
  li {
    a {
      +"go to A"
      e click goToA
    }
  }
  li {
    a {
      +"go to B"
      e click goToB
    }
  }
  li {
    a {
      +"go to C"
      e click goToC
    }
  }
}
(...)
```

Furthermore, in this case, we can write a shorter form if we use the Scala sequence or list type for a declaration. We can add HTML li element with the use of the body of the `foreach` function. For example:

``` scala
(...)
ul {
  Seq("A" -> goToA, "B" -> goToB, "C" -> goToC) foreach { case (title, clickEvent) =>
    li {
      a {
        +s"go to $title"
        e click clickEvent
      }
    }
  }
}
(...)
```
