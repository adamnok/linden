# Functional Store

Functional Store provides you a functional state management. It is not a mandatory thing. You have to make the decision which programming FE style is well-fitting for your project. If your project is small, maybe you don't need independent state management. But if you want to separate the functional, logical part from the appearance, it's an excellent opportunity for you.

These classes ensure that developers can only perform status changes in separate so-called action objects. Nowhere else.

## Types

All functional types of stores are found under linden.store.functional.types. Currently, it supports the next:

 - `Simple`
 - `Fetch`
 - `FetchIncrement`

It is just a type, dont forget! You can find the implementation in the linden.store.functional.Store objest as functions. For example:

- `apply` (it equals with simple)
- `simple`
- `fetch`
- `fetchIncrement`

In the next example we are creating a Hero functional store. Our Hero has a name, nothing else.

``` scala
import linden.store.functional.types._
import linden.store.functional.Store

final class HeroState:
  val name: Simple[Option[String]] = Store(None)
```
This example is presenting well the real situation. And if you try to invoke `change` or `update` function of Store, you will see a compiler error. These functions don't exist.

## Action

When you want to change the value of a Store, you must use such an Action object that can be handling your selected Store. `change` and `update` functions exist within the Action object.

``` scala
import linden.store.functional.Action

final class HeroStateActions(heroStore: HeroState) extends Action:
  def fillWithHero(heroName: String): Unit =
    heroStore.name change Some(heroName)
```
It can yield excellent results if you mark the Action objects as injectable objects. But if you don't want to or don't need to do this separation, you can create a funtion within original Store, that creates an Action object. It's your choice.

So... for an example a full separated solution.

``` scala
final class HeroState:
  val name: Simple[Option[String]] = Store(None)

final class HeroStateActions(heroStore: HeroState) extends Action:
  def fillWithHero(heroName: String): Unit =
    heroStore.name change Some(heroName)

...
private lazy val di = new DI:
  ref[HeroState] singleton HeroState()
  ref[HeroStateActions] singleton createInstance[HeroStateActions]()
...
```

The slightly another approach is the next.

``` scala
final class HeroState:
  val name: Simple[Option[String]] = Store(None)
  lazy val actions = HeroStateActions(this)

final class HeroStateActions(heroStore: HeroState) extends Action:
  def fillWithHero(heroName: String): Unit =
    heroStore.name change Some(heroName)

...
private lazy val di = new DI:
  ref[HeroState] singleton HeroState()
...
```

# Fluent API

You are able to use the Functional Fluent API, if you need it. When do you need this API? You can see the touched cases below when it will help for you:

 - You need to do any setup on a (mutable) Store before you define as a Functional Store. Eg. you would like to call the wrap function on a simple Store. 
 - You want to use as a Functional Store such a (mutable) Store that is developed by you. It is a customized Store but based on one from `linden.store.mutable` package.

``` scala
import linden.store.Store
import linden.store.functional.State

final class HeroState extends State:
  val name = Store(Option.empty[String]).asFunctional
```