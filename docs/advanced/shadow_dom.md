# Shadow DOM

``` scala
import scala.scalajs.js.annotation.JSExportTopLevel
import linden.flowers.{Component, DOMRenderer}

class HelloWorldComponent extends Component:
  override def render = new Html:
    div {
      shadow {
        div {
          +"Hello, world!"
        }
      }
    }

@JSExportTopLevel("main")
def main() =
  DOMRenderer(
    containerElementId = "app",
    headComponent = HelloWorldComponent()
  )
```

Generated HTML DOM tree:

``` scala
<div>
  <div> <!-- shadow -->
    #shadow-root(open)
      <div>
        Hello, world!
      </div>
  </div>
</div>
```
