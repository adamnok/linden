# Hello world

<div align="center">
  <img alt="Browser screenshot" src="_media/hello_world/hello_world.jpg"/>
</div>

Our first project is the well-know *Hello, world!* application. We need to write a Component that defines the renderable text, an entry point that can initialize our frontend application, and a HTML frame for a start.

The next four steps:
1. Hello world component
2. Entry point
3. HTML frame
4. Preview site

## Hello world component

The *Hello, world!* component must be a class that extends `Component` abstract class from package `linden.flowers`.

Every Component has a render function that you can define if you create an anonymous class from `Html`. You don't need to import this type because it is defined as a type alias in the Component class.

Html instance contains the html description of our Component. In this case, it means two divs and a printed *Hello, world!* text. You have to use **plus** (*+*) operator to append text to html dom tree. If you forgot to use the plus operator, it wouldn't be part of html dom tree. It is a necessary operator for texts and for others injected Components. Don't forget to use it!

``` scala
package example.component

import linden.flowers.Component

class HelloWorldComponent extends Component:
  override def render = new Html:
    div {
      div {
        +"Hello, world!"
      }
    }
```

## Entry point

Now, we have an independent renderable Component that we'd like to render in the browser. We should declare a main object with the invokable entry point after we can call DOMRenderer.apply function from package`linden.flowers`.

``` scala
package example

import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.DOMRenderer
import example.component.HelloWorldComponent

@JSExportTopLevel("main")
def main(): Unit =
  DOMRenderer(
    containerElementId = "app",
    headComponent = HelloWorldComponent()
  )
```

## HTML frame

Finally, we need to create an HTML frame file. This HTML file will be the index of our website. Hence it has to include our compiled and generated javascript file, and it should call our main() JS entry point function of JS when the page has already been loaded.

More point, no forget create a HTML element (e.q. a `div`) with identity *app*.

``` html
<!DOCTYPE html>
<html>
  <head>
    <title>Hello, world!</title>
    <script src="hello-world-fastopt.js"></script>
  </head>
  <body onload="main()">
    <div id="app"></div>
  </body>
</html>
```

## Preview site

We can use the sbt-site plugin for our HelloWorld application. This plugin creates a simple static file server from the given files.
We should put this plugin dependency into `plugins.sbt` file and add every file with a mapping into site mappings, which should be a public file

<!-- tabs:start -->

#### ** build.sbt **
``` scala
mappings in makeSite ++= Seq(
  file("index.html") -> "index.html",
  file("target/scala-2.13/hello-world-fastopt.js") -> "hello-world-fastopt.js",
  file("target/scala-2.13/hello-world-fastopt.js.map") -> "hello-world-fastopt.js.map"
)
```

#### ** project/plugins.sbt **
``` scala
// SBT Site - https://www.scala-sbt.org/sbt-site/getting-started.html
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.0")
```

<!-- tabs:end -->

After, we can run the `compile`, `fastOptJS` then `previewSite` task.

``` shell
sbt compile fastOptJS previewSite
```
!> In spite of the fact, a dependency is declared between fastOptJS and compile; hence compile invocation is unnecessary in this case. However, `previewSite` doesn't have any dependency. So we cannot run previewSite task without fastOptJS, unless we write this dependency in `build.sbt`.

The `previewSite` task starts a web server on port 4000, and it opens our default browser with *http://localhost:4000* address.

<div align="center">
  <img alt="Browser screenshot" src="_media/hello_world/browser.png"/>
</div>

Congratulation! 😀

Our first ScalaJS~Linden project is done!
