# Html definition

## Syntacs

``` scala
<standardElemnt>("<class, id and/or name>") {
  o <attribute> <value of attribute>
  o <attribute> <value of attribute>
  ...
  e <event> {
      <body of event invokation>
  }
  ...
  <body>
  ...
}
```

Example:
``` scala
a("is-primary #add-new-store-item") {
  e click addNewStoreItem
  +"Add new item"
}
```

## Classifying

Standard Html elements have an optional classifying parameter, that can contain class, id and name too.

 - The *id* must be start with *#* character
 - The *class* shoudn't start with *.* character. You have not to write dot!
 - And *name* must start with *@* character.

For example; if this is our source code:
``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  e click addNewStoreItem
  +"Add new item"
}
```
And the generated HTML source:
``` html
<a
  class="is-primary is-rounded"
  id="add-new-store-item"
  name="NewStore"
  onclick="addNewStoreItem()">
  Add new item
</a>
```

## Attributes

Every Html element has its own attributes object that we can access through `o` function.

``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  o style "padding-left: 10px;"
  e click addNewStoreItem
  +"Add new item"
}
```

Not to many Html attributes are defined, but we can add more to use `add` function.

``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  o.add("style", "padding-left: 10px;")
  e click addNewStoreItem
  +"Add new item"
}
```

We can define attributes as a dynamic value too to bind to a Store.

``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  o style itemsStore { items =>
    if (items.isEmpty)
      "display: block;"
    else
      ""
  }
  e click addNewStoreItem
  +"Add new item"
}
```

Supported attributes with specific function:

`name`, `clazz`, `style`, `src`, `href`, `target`, `placeholder`, `width`, `height`, `alt`, `title`.

## Events

If you want to listen some event on a Html element, you can use the `e` function. Its functionality looks like the `o` for attributes.

``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  +"Add new item"
  e click {
    println("clicked")
  }
}
```

If you need other event listener, you can use the `add` function.

``` scala
def add(eventKey: String)(callback: PartialFunction[EventData, Any]): Unit
```

Example:

``` scala
a("is-primary is-rounded #add-new-store-item @NewStore") {
  +"Add new item"
  val $over = Store(false)
  e.add("mouseover")(_ => $over.change(true))
  e.add("mouseout")(_ => $over.change(false))
  o clazz $over(Option.when(_)("active"))
}
```

Supported events with specific function:

`click`, `doubleClick`, `keyup`, `enterKeyUp`, `escKeyUp`.

## Components

This is the point, when we can define our HTML blocks, but we need to know the Component object to use the HTML definition. Because HTML definition cannot be independent without a Component. The HTML definition must be part of a Component.

Look for the example!

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
