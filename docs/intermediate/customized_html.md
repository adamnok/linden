# Customized Html elements

!> DEPRECATED 
## Extend with other html elements

``` scala
import linden.definition.{Context, Html}

trait MyExtendedHtml { _: Html[_] =>

  def rel(content: => Any)(implicit context: Context): Unit = {
    element("rel", content, context)
  }

  def rel(identity: String)(content: => Any)(implicit context: Context): Unit = {
    element("rel", identity, content, context)
  }
  
}
```

## Extend with the own elements

``` scala
import linden.definition.{Context, Html}

trait MyExtendedHtml { _: Html[_] =>

  def bsHeader(content: => Any)(implicit context: Context): Unit = {
    div("bs-header") {
      div("center") {
        content()
      }
    }
  }

  def bsBody(content: => Any)(implicit context: Context): Unit = {
    div("bs-body") {
      div("full") {
        content()
      }
    }
  }
}
```


``` scala
override lazy val render = new Html with MyExtendedHtml {
    div {
        bsHeader {
        ul {
            li {
            +"Menu 1"
            }
            li {
            +"Menu 2"
            }
        }
        }
        bsBody {
        div {
            a {
            +text
            e click onChangeLocalStore
            }
        }
        div {
            generate(localStoreTest) { implicit context =>
            value =>
                div {
                +value
                }
            }
        }
        }
    }
}
```