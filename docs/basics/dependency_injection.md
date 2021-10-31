# Dependency Injection

The *Linden* library has a composited *half-evaluation* dependency injection system. It means that this system works as a standard dependency injection system for external objects, but its working is different in the html tree definition for Component. An injectable component is able to get explicit parameters in html tree definition.

Firstly, let me share a post for standard working. We need to create an instance from `linden.di.DI` class and after then we can put into the body of our `DI` every injectable object. Useful to define as an anonymous class.

In the next, we add `MyInjectedA` and `MyInjectedB` as a singleton and a factory.

``` scala
new DI:
  ref[MyInjectedA] singleton new MyInjectedA()
  ref[MyInjectedB] factory new MyInjectedB()
```

Don't forget! It is an anonymous class. But if you want you can define a not anonymous class too; however this documentation contains description for anonymous class.

As a defined class in the next example.

``` scala
final class PackagedDI extends DI:
  ref[MyInjectedA] singleton new MyInjectedA()
  ref[MyInjectedB] factory new MyInjectedB()
```

Or as an object.

``` scala
object PackagedDI extends DI:
  ref[MyInjectedA] singleton new MyInjectedA()
  ref[MyInjectedB] factory new MyInjectedB()
```

If a dependency has more dependency from current session **without** custom parameter, we can use `createInstance` function from DI.

Defeinition of this function the next: `def createInstance[T](args: Any*): T`. In spite of fact, you can see type of Any parameters but basically it is a Scala inline macro. So that it will be typesafe in the compile time. The compiler will see explained real types. So, don't worry!

Example code about `createInstance` invokation:
``` scala
class MyInjectedC(a: MyInjectedA)

new DI:
  ref[MyInjectedA] singleton new MyInjectedA()
  ref[MyInjectedB] factory new MyInjectedB()
  ref[MyInjectedC] singleton createInstance[MyInjectedC]()
```
In this case order of dependencies can be important. Not must, just can be.

If a dependency has more dependency from current session **with** custom parameter, we can use `createInstance` function with vararg parameter type.

``` scala
class MyInjectedD(name: String)(a: MyInjectedA)

new DI:
  ref[MyInjectedA] singleton new MyInjectedA()
  ref[MyInjectedB] factory new MyInjectedB()
  ref[MyInjectedD] singleton createInstance[MyInjectedD]("Albert")
```
When our `DI` definition is done, we can use `apply` and `get` function of `DI`.

Example code:
``` scala
val di = new DI:
  ...
val c: MyInjectedC = di[MyInjectedC]
val d: MyInjectedD = di[MyInjectedD]

val cOpt: Option[MyInjectedC] = di.get[MyInjectedC]
val dOpt: Option[MyInjectedD] = di.get[MyInjectedD]
```

## DI for Components

Components can use dependency injection, but he is not needed simply as an injected object. It can be, but not necessary.

We need `factoryBy` function of `Component` from `linden.flowers` package.

``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object MyInjectedC:
  def apply() = factoryBy[MyInjectedC]

class MyInjectedC(a: MyInjectedA) extends Component:
  (...)
```

``` scala
import linden.flowers.Component
import linden.flowers.Component.factoryBy

object MyInjectedD:
  def apply(name) = factoryBy[MyInjectedC](name)

class MyInjectedD(name: String)(a: MyInjectedA) extends Component:
  (...)
```

Finally, we can use in the html definition simple:

``` scala
(...)
new Html:
  div {
    +MyInjectedD("Peter")
  }
(...)
```

## Entry point

When using a customized, own Dependency Injector (*session*), we need to pass it to the DOMRenderer. We can do it quickly.

``` scala
import example.component.MainComponent
import linden.di.DI
import linden.flowers.DOMRenderer

import scala.scalajs.js.annotation.JSExportTopLevel


@JSExportTopLevel("mainEntryPoint")
def main(): Unit =
  val di = new DI:
    (...)
  DOMRenderer(
    containerElementId = "app",
    session = di,
    headComponent = MainComponent()
  )
```

## Factory for main Component

If your head/main Component needs any injectable object, you can define a factory for the main Component and you can use the factory when you pass the *headComponent* into `DOMRenderer`.

``` scala
import linden.di.DI
import linden.flowers.DOMRenderer
import linden.flowers.Component
import linden.flowers.Component.factoryBy

import scala.scalajs.js.annotation.JSExportTopLevel

object MainComponent:
  def apply() = factoryBy[MainComponent]

class MainComponent(injected: Injected) extends Component:
  override def render = new Html:
    (...)

@JSExportTopLevel("mainEntryPoint")
def main(): Unit =
  val di = new DI:
    (...)
  DOMRenderer(
    containerElementId = "app",
    session = di,
    headComponent = MainComponent()
  )
```
