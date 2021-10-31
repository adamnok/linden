# Dependency Injection Graph

<div align="center">
  <img alt="Example hierarchy for DI Graph" src="_media/di_graph_example.png"/>
</div>

A Dependency Injection object basically is such a big container that contains one of instance from our every object. Most of case it is enough. However sometimes not. Sometime we would like to replace some object in the container.

The Linden DI offers a hierarchy solution for this *sometimes* case. We are able to create a `DIGraph` instance from `DI` and each `DIGraph` has a parent `DI`. The `DIGraph` cannot replace any object in its parent, but it can uses and can override them. The `DIGraph` extends from `DI`, hence we can use everywhere we want to a instance of `DI`.

For example, we are creating such a hierarchy if DI, that you can see above.

``` scala
import linden.di.{DI, DIGraph}

val diA = new DI:
  // ...

val diB = new DIGraph(diA):
  // ...

val diC = new DIGraph(diA):
  // ...

val diE = new DIGraph(diB):
  // ...

val diF = new DIGraph(diB):
  // ...
```

Other example:


``` scala
import linden.di.{DI, DIGraph}

case class A(name: String)
case class B(name: String)

val diA = new DI:
  ref[String] singleton "Alma"
  ref[A] factory createInstance[A]
  ref[B] factory createInstance[B]

val diB = new DIGraph(diA):
  ref[String] singleton "Endraw"
  ref[A] factory createInstance[A]


println(diA[A].name) // out: Alma
println(diA[B].name) // out: Alma
println(diB[A].name) // out: Endraw
println(diB[B].name) // out: Alma
```

## DI Graph in the Html tree

We are able to define or change DIGraph in the Html definition with use `session` method.

Definition of this session method:

``` scala
def session(session: S => S)(content: Context ?=> Unit)(using context: Context, renderContext: RenderContext): Unit
```

Example:

``` scala
private def createSession(data: Data)(parent: DI) =
  new DIGraph(parent):
    ref[Data] singleton(data)

override def render = new Html:
  session(createSession(data)) {
    +OtherComponentComponent()
  }
```
