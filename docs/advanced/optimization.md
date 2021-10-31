# Optimization

The generated Html DOM by a Store will be rerendered each time when value in the Store is changed. In the most of case it is the expected; however if the value in the Store is a List and we know how os ordered this list, we are able to use an optimization algorythm. This algorythm can be `appendable` and `indexable`.

## Appendable

<div align="center">
  <img alt="Appendable example" src="_media/advanced/Appendable.png"/>
</div>

For this algorythm we have to restriction:
 -  When we insert a new element into the list, we have to do it to top of them or bottom of them.
 -  We never delete item from the list, but we can delete each element from the list.

### How to use

We are given an entity, `Tweet`.

``` scala
case class Tweet(id: Int, name: String)
```

Store for list of Tweets:

``` scala
val store = Store(Seq.empty[Tweet])
```

Allowed actions:

``` scala
def insertToTop(tweet: Tweet): Unit = store.change(it => tweet :+ it)
def insertToBottom(tweet: Tweet): Unit =  store.change(it => it +: tweet)
def clean(): Unit =  store.change(Seq())
```

Set optimization:

``` scala
val optimizedStore = store.optimize(RenderStrategy.appendable(), _.id)
```

Create Html definition:

``` scala
...
generate(optimizedStore) { tweet =>
  +tweet.name
}
...
```

## Indexable

For this algorythm we do not have any restriction.

### How to use

We are given an entity, `Tweet`.

``` scala
case class Tweet(id: Int, name: String)
```

Store for list of Tweets:

``` scala
val store = Store(Seq.empty[Tweet])
```

Set optimization:

``` scala
val optimizedStore = store.optimize(RenderStrategy.indexable(), _.id)
```

Create Html definition:

``` scala
...
generate(optimizedStore) { tweet =>
  +tweet.name
}
...
```
