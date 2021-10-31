# Component

`Component` is a base object in the Linden. It contains an independent HTML tree, that we can define with HTML definition.

The most easier `Component` looks as follows:

``` scala
import linden.flowers.Component

class HeroComponent extends Component:
  override def render = new Html:
    h1 {
      +"The Hero!"
    }
    p {
      +"Everyone is a hero inside."
    }
```

So, this is just a standard class that extends from `Compoent` class.

!> Important! You can find a `Component` class in the linden.core, but you should use it from package linden.flowers.


## Life cycle

Every Component has two life cycle functions and an initialization opportunity. Life cycle functions are the next: mounted and unmounted.
 
 - The *mounted function* will be invoked when the touched Component was not just one of created but also injected into the HTML DOM tree of the browser. This is the time, when our users can already see it. You can be sure that it is used. Hence you are able to start to download such heavy data (e.q. AJAX), that are necessary for this Component.

 - *Unmounted function*: If a Component can be mounted, of course the system can remove it from HTML DOM tree, when we no longer need it. In most the cases, you has nothing special to do with this state. The system is able to remove / unbind / clear the now unnecessary connections through `ClearContext`.

``` scala
import linden.flowers.Component

class HeroComponent extends Component:

  override def mounted(): Unit =
    // invoked when injected into the HTML DOM tree

  override def unmounted(): Unit =
    // invoked when removed from the HTML DOM tree

  override def render = new Html:
    /* ... */
```

Both of them function (`mounted` and `unmounted`) are optional. If you want to do nothing inside these functions then do not overwrite them.

The initialization opportunity means constructor of Component. When the system are creating an instance of a component, maybe it will be injected into the HTML DOM. However, **maybe not**! You can initialize the Store objects or define Store connections, but never throw some HTTP request!

?> In the spite of the fact, you shouldn't forget this rule, but system is going to inject a Component into HTML DOM tree if it create an instance from it in the current implementation.

``` scala
import linden.flowers.Component
import linden.store.Store

class HeroComponent extends Component:

  private val articleStore = Store("")

  private val isEmptyArticleStore = articleStore.map(_.isEmpty)

  override def mounted(): Unit =
    // invoked when injected into the HTML DOM tree

  override def unmounted(): Unit = 
    // invoked when removed from the HTML DOM tree

  override lazy val render = new Html:
    /* ... */
```

?> You shouldn't afraid about Store object if you do not know yet. You will read about them in the later Chapter.

## Factory

**Every Component must have a factory!** Okey! This is not such a rule, which you must always adhere to, but it is very useful. If you are programming with Dependency Injection technique, you need a factory for each Component. If you don't use this technique (in the current project), factories of Components are not a necessary things, but more easyer to use them of they have it. Hence, I suggest you always write a factory for each Component.

> I admit, sometimes I don't write factory for fews Component and then I always get mad at myself for saving a little time on factories.

Don't worry! Writing a factory of Component is not too hard.

Let me show you the minimal solution!

``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object HeroComponent:
  def apply() = factoryBy[HeroComponent]

class HeroComponent extends Component
```

We need to use `factoryBy` function of `Component` object from `linden.flowers` package.

If we see type of Component's constructor, we can summary four types, that are in the next table.

| Name | Factory invocation example | Description |
|---|---|---|
| **Empty constructor** | `factoryBy[MyComponent]` | when a Component doesn't have contructor parameter <br><br> `class MyComponent extends Component` |
| **Single constructor** | `factoryBy[MyComponent](name, age)` | when a Component has one group constructor parameter <br><br> `class MyComponent(name: String, age: Int) extends Component`
| **Empty DI constructor** | `factoryBy[MyComponent]` | when a Component has one group constructor parameter but these parameters are injectable <br><br> `class MyComponent(locator: BrowserLocator) extends Component`
| **Single DI constructor** | `factoryBy[MyComponent](name, age)` | when a Component has two groups constructor parameter but parameters from last group are injectable <br><br> `class MyComponent(name: String, age: Int)(locator: BrowserLocator) extends Component`

There are the allowed types, never use others.

In the next example, you can see a standard Component with factory.

``` scala
import linden.flowers.{BrowserLocator, Component}
import linden.flowers.Component.factoryBy

object HeroComponent:
  def apply(name: String, age: Int) = factoryBy[HeroComponent](name, age)

class HeroComponent(name: String, age: Int)(locator: BrowserLocator) extends Component
```

## Renderable html

When you create a Html definition in a Component's render function, you can use other Component too inside the Html definition. Of course. This is one of the essence of the Components. Just, you need a factory reference for the component you want to use, then you can use `+` operator as like as for String.

For example, the referenced Component, that we want to use inside a other Component will be the next.

``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object HeroComponent:
  def apply(name: String, age: Int) = factoryBy[HeroComponent](name, age)

class HeroComponent(name: String, age: Int) extends Component:
  override def render = new Html:
    h1 {
      +"Hero"
    }
    p("card") {
      span {
        +s"name is $name"
      }
      span {
        +s"age is $age"
      }
    }
```

Finally, the main Component.

``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object MainComponent:
  def apply() = factoryBy[MainComponent]

class MainComponent extends Component:
  private val heros = Seq(
    "Spiderman" -> 17,
    "Echo" -> 41
  )
  override def render = new Html:
    section("main") {
      heroes.foreach { case (name, age) =>
        +HeroComponent(name, age)
      }
    }
```
The **key pont** is the `+` operator. This is a prefix operator that will inject our referenced Component into the Html definition. I know, every developer forget to write this operator the first few times and can cause a few annoying minutes but that is my experence, everyone learn this rule fast. If you forget write the `+` operator before a injectable Component, it will be skipped from HTML DOM tree.

The generated HTML DOM tree will be light by system. It doesn't contain any unnecessary boilerplate. The next code show the generated HTML.

``` html
<section class="main">
  <h1>Hero</h1>
  <p class="card">
    <span>name is Spiderman</span>
    <span>age is 17</span>
  </p>
  <h1>Hero</h1>
  <p class="card">
    <span>name is Echo</span>
    <span>age is 41</span>
  </p>
</section>
```

## Main Component

In your webapplication, you need to create such a Component, that will be the top of the generated HTML tree. This is the Main Component. This Component will be parameter of DomRenderer as the headComponent. You can create a factory for this Component but it is not necessary. However I hope you remeber the rule: Every Component must be a factory. This rule is true for in this case too. A factory is a good thing for the Main Component too.

Given a Component here:
``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object MainComponent:
  def apply() = factoryBy[MainComponent]

class MainComponent extends Component:
  private val heros = Seq(
    "Spiderman" -> 17,
    "Echo" -> 41
  )
  override def render = new Html:
    section("main") {
      heroes.foreach { case (name, age) =>
        +HeroComponent(name, age)
      }
    }
  }
```

This is our Main Component now. This Component contains a Html definition, invokes other Components, others. However top of them, we are going to use this Component as a Main Component in the Entry point. Entry point means such a function, that will be invoked firstly from HTML or from Browser.

``` scala
import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.DOMRenderer

@JSExportTopLevel("main")
def main(): Unit =
  DOMRenderer(
    containerElementId = "app",
    headComponent = MainComponent()
  )
```
If the Main Component has a factory, we can use this factory for the DOMRenderer and the Component can use some injectable dependency. If the Main Component doesn't use any injectable dependency, its factory is basically not necessary.

?> You can read about Dependency Injection in the later chapter.

DomRenderer invokation without factory in the next example code.

``` scala
import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.DOMRenderer

@JSExportTopLevel("main")
def main(): Unit =
  DOMRenderer(
    containerElementId = "app",
    headComponent = new MainComponent()
  )
```

In the spite of the fact, the difference is not too much, just the `new` keyword, but its meaning is very different. I offer the factory to you, again. :)

