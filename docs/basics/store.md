# Store

Firstly in this part, we should learn how to use the basic Store object.

For example:
``` scala
import linden.store.Store

private val name = Store("Alexander")
```

We've declared a simple type of Store, that type is String, and its initialization value is *Alexander*.

!> Stores have a read type and a write type. But their types are the same in this case (Simple store).

If we want to change the stored value, we can use `change(newValue: T): Unit` function or `change(setNewValue: T => T): Unit` function.

In the first case, we are given a new value explicitly that should be stored with this expression.

E.q.
``` scala
name.change("Andrew")
```

In the second case, we need to use the original value to calculate the new value.

E.q.
``` scala
name.change(x => if(x startWith "Alex") "Alexander" else x)
```

Now, we can declare a simple Store and we can change its value, but how can we use its current value? We have two opportunities.

1. to get its current value at the moment with `value` function
2. to give a subscribe function that will be invoked when the current value is modified

So. If you call the `value` function, you will get the current value of the Store.

``` scala
import linden.store.Store

val name = Store("Alexander")
println(name.value) // Alexander

name.change("Andrew")
println(name.value) // Andrew
```

Every Store object is basically an observer. As an observer, we can add some function, that will be invoked when the stored value is changing. We can do it with `subscribe` and `lateSubscribe` function. These functions have one different. The `subscribe` invokes immediately the parameter, when you call it. Let me share two example to demonstrate it.

With subscribe:
``` scala
import linden.store.Store

val name = Store("Alexander")

name.subscribe(it => println(it)) //out: Alexander
name.change("Andrew") //out by subscribe: Andrew
```

With lateSubscribe:
``` scala
import linden.store.Store

val name = Store("Alexander")

name.lateSubscribe( it => println(it))
name.change("Andrew") //out by subscribe: Andrew
```

One important thing! Both of them have an implicit parameter, the `ClearContext`. This implicit parameter is exists when you are in a Component, but nowhere else. This parameter try to ensure that developers cannot do any memory leak or unnecessary calculation. They are not able to forgot to unsubscribe with it.

## Types

 - SimpleStore
 - FetchStore
 - FetchIncrementStore

Every Store type is available as a method in the `linden.store.Store` object.

 - Store.apply -> SimpleStore
 - Store.option -> SimpleStore without initialized value
 - Store.fetch -> FetchStore
 - Store.fetchIncrement -> FetchIncrementStore

### SimpleStore

You can store a value in the SimpleStore. You can get it and you cen change this stored value.

``` scala
import linden.store.Store

val store = Store(3)
name.subscribe(it => println(it)) //out: 3
name.change(5) //out by subscribe: 3
```

Option version:
``` scala
import linden.store.Store

val store = Store.option[Int]
name.subscribe(it => println(it)) //out: None
name.change(5) //out by subscribe: Some(5)
```

### FetchStore

``` scala
def fetch[T](current: T)(update: T => T): FetchStore[T]
```

* * **TODO** * *

### FetchIncrementStore

``` scala
def fetchIncrement[I, T](index: I, current: T)(update: I => T)(using incrementer: Incrementer[I]): FetchIncrementStore[T]
```

* * **TODO** * *


## Binding to Html dom tree

We have to declare a Store object as part of our Component. Store objects can be some representation of the changeable values. Input forms mean the change in the exact values of forms are able to change. Hence we should usually have a store object for every input form.


``` scala
package component

import linden.flowers.Component
import linden.store.Store

class GiveMeYourNameComponent() extends Component:

  private val name = Store("")

  override def render = new Html:
    +"Give me your name please: "
    input(name)
    p {
      +name
    }
```

Generate Html by Store:

``` scala
package example.component

import linden.flowers.Component
import linden.store.Store

class GiveMeYourNameComponent() extends Component:

  private val name = Store("")

  override def render = new Html:
    +"Give me your name please: "
    input(name)
    p {
      generate(name) { name =>
        if(name == "Batman")
          +"Are you sure? Are you Batman?"
        else
          +name
      }
    }
```

!> Never use `generate` immediately after a `generate` invokation! You should use zip command instead of it.
E.g.: `generate(name zip otherName)`

## High order functions

We can represent a Store as a dynamic stream. Hence it as a stream has several high order function. 

The standard high order functions the next:
 - `map`
 - `filter`
 - `zip` (alias `join`)

In addition to the standard, it also has few special functions:
 - `init `
 - `zipWithLastValue`
 - `flowControl`


### Standard: *map*

Definition:
``` scala
def map[G](transform: T => G): ReadStore[G]
```

It is transforming the original stored value to other, but its result is a new `ReadStore`, that uses the original store as an input source.

Example:

``` scala
val store = Store("Alma") // Simple store
val newStore = store.map(it => it.length) // Read store

println(store.value) // out: Alma
println(newStore.value) // out: 4

store.change("Jhones")
println(store.value) // out: Jhones
println(newStore.value) // out: 6

store.change(5) // compile error: ReadStore does not have `change` method.
```

### Standard: *filter*

Definition:
``` scala
def filter(condition: T => Boolean): ReadStore[T]
```

We need to memorize an important think about this filter function: the *value* function does not use your condition. Your given condition is used by `subscribe` and `lateSubscribe` functions.

``` scala
val store = Store("Alma")
val newStore = store.filter(it => it.startsWith("J"))

println(store.value) // out: Alma
println(newStore.value) // out: Alma

// But!
newStore.subscribe(println) // out: nothing
store.change("Jhones") // out by subscribe: Jhones

println(newStore.value) // out: Jhones
```

### Special: *init*

Definition:
``` scala
def init(initializeValue: T): ReadStore[T]
```

When we invoke `init` function on a Store, we are creating a new Store in imagining that ignore the current value of the original Store, and given value will be the present value of the built Store.

Demonstration of function:

``` scala
import linden.store.Store

val name = Store("Alma")
val initName = name.init("Peter")

println(initname.value) // Peter

name.change("Edit")

println(initname.value) // Edit
```

### Special: *zipWithLastValue*

Definition:
``` scala
def zipWithLastValue: ReadStore[(Option[T], T)]
def zipWithLastValue(init: T): ReadStore[(T, T)]
```
We are creating a new Store with this function that has a pair as a current value. The first element of the couple is the previous value; the second element is the current real value.

``` scala
import linden.store.Store

val name = Store("Alma")
val zippedName = name.zipWithLastValue("Peter")

println(initname.value) // (None, Peter)

name.change("Edit")

println(initname.value) // (Peter, Edit)

name.change("Eva")

println(initname.value) // (Edit, Eva)
```

### Special: *flowControl*

Definition:
``` scala
def flowControl(comparator: (T, T) => Boolean): ReadStore[T]
def flowControl[G](extractor: T => G): ReadStore[T]
```

We can control the data stream with `flowControl` functions. We add a comparator or an extractor function that will be used for the check method. The store would ignore the new incoming value if the comparator or extractor said that incoming data not a different value.

The `comparator` has to result true if the given parameters are the same. The `extractor` has to result in such a value, that the system can use for comparison.

## Chaining reduction

When using a high-order function, we must not forget that there are no evaluated Store objects. There are just a lot of operations in a long chain. This chain of actions will be assessed when we invoke its `value`,` subscribe`, or `lateSubscribe` function. **AND!** Not just one time! Every time we invoke one of them, or we put new data into the stream.

We can use chaining reduction functions to resolve this problem:
  - `chainingReduction`
  - `chainingDynamicReduction`


Let me share a concrete example of this problem. Let's look at how it works as the next code:

``` scala
val myStore = Store(0)
val transformedStore = store
  .map(x => x * 10)
  .map(x => x.toString)
  .map(x => s"Number: $x")
new Html {
  div {
    +transformedStore
  }
  div {
    +transformedStore
  }
}
```

The program code is simple; we map the value of a Store three times and then print its contents in two places in the Html DOM tree. On the other hand, we can see a surprising stream at first.

<div align="center">
  <img alt="Browser screenshot" src="_media/basics/store/chaning_reduction_problem_representation.png"/>
</div>

Both DOM inserts (subscribers) perform the transformations separately. If these transformations also include an HTTP request or some complicated calculation, we will not be happy with these duplications.

In the following code, we reduce this chain using the `chainingReduction` function. The code:

``` scala
val myStore = Store(0)
val transformedStore = store
  .map(x => x * 10)
  .map(x => x.toString)
  .map(x => s"Number: $x")
  .chainingReduction
new Html {
  div {
    +transformedStore
  }
  div {
    +transformedStore
  }
}

```

The stream generated using the chainingReduction function is as follows:

<div align="center">
  <img alt="Browser screenshot" src="_media/basics/store/chaning_reduction_problem_representation_o.png"/>
</div>

The reason for this operation lies in the lazy evaluation and the existence of a clear context.

### Reduction: *chainingReduction*

Definition:

``` scala
def chainingReduction(using context: ClearContext): ReadStore[T]
```
The chainingReduction operation is considered a point where we call collected and issued transformation steps and a new Store object, which is a transformed value. Subsequent subscriptions will be made to this new Store object.

This function subscribes to the original Store object. For this reason, the transformations can be executed without no more subscriptions are made in addition to this function.

!>If we want to invoke this function, we need such a clear context object whose scope is not straiter than clear context, which is available at the place of subscription.

### Reduction: *chainingDynamicReduction*

Definition:
``` scala
def chainingDynamicReduction: ReadStore[T]
```

It performs the same operation as the `chainingReduction` function without subscribing to the Store itself. Because of this, you don't need a clear context object, but it also runs a little slower at runtime.

Collected transformations are performed by one of our subscriptions. If the selected subscription's clean environment disappears, it chooses another subscription to perform the collected conversion operation.

Because calling this function does not require a clear context object, it does not subscribe, so the collected transformations will not be executed if you do not subscribe.
