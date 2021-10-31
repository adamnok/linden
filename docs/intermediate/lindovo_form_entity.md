# Lindovo

Lindovo is an extension for Linden; hence we need to put it into build.sbt file as a dependency:

<!-- tabs:start -->

#### ** build.sbt for frontend **
``` scala
resolvers +=
  "DongoTeam repository" at "https://public.repository.dongoteam.hu"
libraryDependencies +=
  "org.dongoteam.linden" %%% "lindovo" % "<last version>"
```

#### ** build.sbt for shared project **
``` scala
resolvers +=
  "DongoTeam repository" at "https://public.repository.dongoteam.hu"
libraryDependencies +=
  "org.dongoteam.linden" %% "lindovo" % "<last version>"
```

<!-- tabs:end -->

With this extension you can create such a shared objects that are exactly a validated form request. The approach is the next:

1. Create an entity in your shared project
    - This entity contains the request form data. E.g. a login form has email and password field.
2. Create an validator builder (and work template) in shared project
    - The validator builder contains such the validators, that are work on backend and frontend too.
    - Thw work template is a neccessary class for every entity.
3. Use it on the frontend side and on backend side.


## Shared project

For a shared project, you need the [sbt-scalajs-crossproject](https://github.com/portable-scala/sbt-crossproject) plugin. There are four steps to using this plugin.

**First step**, import it in your *plugins.sbt* file:

``` scala
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "<last version>")
```

**Second step**, define the shared sub-modul (sub-project) in your build.sbt:

``` scala
lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
```

The defined `shared` project will use *shared* folder as a source of the project and furthermore this project will be compiled into *JS* and *JVM* platform.


**Third step**, define a shared virtual project form JVM and one for JS in your build.sbt:

``` scala
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js
```

**Fourth step**, add the virtuals project as a dependency to JVM project and to JS project on your build.sbt:

``` scala
lazy val myJVMProject = (project in file("server"))
   /* ... */
  .dependsOn(sharedJvm)
lazy val myJSProject = (project in file("client"))
   /* ... */
  .dependsOn(sharedJs)
```

## Create the entity

We have to define our entities in the shared project, because we want to use them on the frontend side and backend side too. This is a communication object between frontend to backend side. Direction of the communication is just one-direction. We can use the *request* word too instead of entity. Basicily this is a typed and 
validated request object.

In out example we will create such a form, when we can register the heros. Every hero will be four argument: *name* and *e-mail*. *Name* should be required field, however *e-mail* is optional.

The definition is the next, this is a **case class**.

``` scala
case class HeroRegistration(
  name: String,
  eMail: Option[String]
)
```
It is just a simple *case class*.

## Create the work template

Work template is the same like the entity. However work template has a higher kinds generic parameter. See the example.

``` scala
case class HeroRegistrationW[T[_]](
  name: T[String],
  eMail: T[Option[String]]
)
```

Work template helps to us define vaditator, checking the result, getting the errors.



## Create a validator builder

Each validator builder is such a function that get any `Template[FieldValidator]` as a parameter and the function will be return a `Template[FieldValidator]` value. Type of input and output is the same, but its value is different.

Type of validator: `HeroRegistrationW[FieldValidator] => HeroRegistrationW[FieldValidator]`

See below our validator builder.

``` scala
import lindovo.FieldValidator
import lindovo.FieldValidators.*

def validatorBuilder(m: HeroRegistrationW[FieldValidator]): HeroRegistrationW[FieldValidator] =
  import m.*
  copy(
    name = name.register(isRequire),
    eMail = eMail.register(isEmail)
  )
```

Definition of this function is not too short, so if you want, you can let to calculate the result type. I mean just ignore it.

``` scala
import lindovo.ValidatorBuilder
import lindovo.Validators.*

def validatorBuilder(m: HeroRegistrationW[FieldValidator]) =
  import m.*
  copy(
    name = name.register(isRequire),
    eMail = eMail.register(isEmail)
  )
```

## Entity and work template

In this part we are going to define the connection between `HeroRegistration` and `HeroRegistrationT`. After then we try to define a basic validator builder for our entity.

The connection is an *one to one* conenction that is represented by the `FormMapping` from `lindovo` package. We need do nothing just create a **given* object for the `FormMapping`. This code looks like the next.

``` scala
import lindovo.FormMapping

given FormMapping[HeroRegistration, HeroRegistrationW]()
```

If we are creating such an object thats name is `HeroRegistration` and we will put this given definition then we will never see it again in our code. Let's do it! Create a companion object for `HeroRegistration`!

``` scala
import lindovo.form.FormMapping

object HeroRegistration:
  given FormMapping[HeroRegistration, HeroRegistrationW]()
```

We are done! The next opportunity to add one or more default validator builder into this `FormMapping`. It will be means, that every time when we try to validation one of the instance of `HeroRegistration`, these default validator builders will be used. Later, we can define and use other customized validator builder but defaults contain the *have to* condition to the entity validation.

We can use that validator, that we have defined before. We are able to pass it into the `FormMapping` as a parameter.

``` scala
import lindovo.FieldValidator
import lindovo.FieldValidators.*
import lindovo.FormMapping

object HeroRegistration:
  given FormMapping[HeroRegistration, HeroRegistrationW](validatorBuilder)

  def validatorBuilder(m: HeroRegistrationW[FieldValidator]) =
    import m.*
    copy(
      name = name.register(isRequire),
      eMail = eMail.register(isEmail)
    )
```

If you want to put two or more validator builder into FormMapping, you can do it easily. It is a *vararg* parameter.

``` scala
import lindovo.FieldValidator
import lindovo.FormMapping

object HeroRegistration:
  given FormMapping[HeroRegistration, HeroRegistrationW](
    validatorBuilder1,
    validatorBuilder2,
    validatorBuilder3
  )

  def validatorBuilder1(m: HeroRegistrationW[FieldValidator]) = ...
  def validatorBuilder2(m: HeroRegistrationW[FieldValidator]) = ...
  def validatorBuilder3(m: HeroRegistrationW[FieldValidator]) = ...
```

## LindoForm 

Finally, we will use our validator builders, we will do a validating. 

``` scala
import lindovo.LindovoForm

// entity that we want to validatong
val hero = HeroRegistration(name = "One Punch Man", eMail = None)
// Create a Lindovo form
val form = LindovoForm[HeroRegistration](hero)
```

If we want to put more validator into form, we can do it as a 2nd parameter of LindovoForm.

``` scala
import lindovo.FieldValidator
import lindovo.FieldValidators.*
import lindovo.LindovoForm

def validatorBuilder2(m: HeroRegistrationW[FieldValidator]) =
  m.copy(
    name = m.name.register(startTextWithUpperCase(), minTextLength(3))
  )

// entity that we want to validatong
val hero = HeroRegistration(name = "One Punch Man", eMail = None)
// Create a Lindovo form
val form = LindovoForm[HeroRegistration](hero)(validatorBuilder2)
```

Now, we can create a Lindovo Form. Next step, how can we use it. 

``` scala
val a: Future[Option[HeroRegistration]] = form.validated
val b: Future[Either[HeroRegistration, HeroRegistrationW[Failed]]] = form.getValidated
```

 - The *a* option result `None` when some value in our instance of HeroRegistration is invalid. If it is valid, the result equals with original `hero`, just it will be in a `Some` object. 
 - The *b* option uses `Left` and `Right` type to get result for us. Important! The meaning in this case is different from the standard approach. We will get `Left` if our instance of HeroRegistration is valid. And result is Right within the errors if somethin is invalid in the `hero`.

Definition of `Failed` is the next:

``` scala
case class Failed[T](value: T, errors: Seq[FieldError])
```
 - `value` is such a value that we tried to validate. It is independent of result of validating. It will be always this value.
 - `errors` contains each error that is connected and declared for the current field. If content does not have any error, it means everything is okay with this field. So, if it is empty, the valut is valid.

The last object in this part that we need to know, the `FieldError`. You can see the definition above.

 ``` scala
 trait FieldError:
  def message: String
 ```

It has just one method, `message` that can response the error message. You can use this message for i18n, however do not forget, every error is a specified class. Just matching them!


## LindovoStoreForm

With this object, you can generate such few Store that helps to define the validated Html inputs.

!> This object **only** be used **in a ScalaJs *(frontend)*** project!

Shortly, the command is the next.

``` scala
import lindovo.LindovoStoreForm

val storeForm = LindovoStoreForm[HeroRegistration]()
```

Type of `storeForm` is `lindovo.StoreCollectForForm` with few useful generated Store.

| Field | Type | Example result type|
|---|---|---|
| **store** | `E[SimpleStore[_, Option[_]]]` | `HeroRegistrationW[SimpleStore[_, Option[_]]]` |
| **formValidatedStore** | `E[ReadStore[Validated[_]]]` | `HeroRegistrationW[ReadStore[Validated[_]]]` |
| **validatedStore** | `ReadStore[Option[S]]` | `ReadStore[Option[HeroRegistration]]` |
| **validated** | `() => Future[Option[S]]` | `Future[Option[HeroRegistration]]` |
| **validatedOrError** | `Future[S]` | `Future[HeroRegistration]` |


Definition of `Validated` class is the next:
``` scala
final case class Validated[T](value: Option[T], errors: Seq[FieldError])
```



## Predefined field validators

There are in ` lindovo.FieldValidators` object.

### For String type
| Name | Error class | Error message |
|---|---|---|
| isRequire |StringFieldValidators.IsRequire <br /> extends StringFieldError | lindovo.field(:string).IsRequire |
| isEmail | StringFieldValidators.IsEmail <br /> extends StringFieldError | lindovo.field(:string).IsEmail |
| minTextLength(length: Int) | StringFieldValidators.MinTextLength(length: Int) <br /> extends StringFieldError | lindovo.field(:string).MinTextLength($length) |
| maxTextLength(length: Int) | StringFieldValidators.MaxTextLength(length: Int) <br /> extends StringFieldError | lindovo.field(:string).MaxTextLength($length) |
| onlyEnglishCharacters | StringFieldValidators.OnlyEnglishCharacters <br /> extends StringFieldError | lindovo.field(:string).OnlyEnglishCharacters |
| startTextWithUpperCase | StringFieldValidators.StartTextWithUpperCase <br /> extends StringFieldError | lindovo.field(:string).StartTextWithUpperCase |
| notAllowedTexts(notAllowed: Set[String]) | StringFieldValidators.NotAllowedTexts <br /> extends StringFieldError | lindovo.field(:string).NotAllowedTexts($notAllowed) |

### For Int type
| Name | Error class | Error message |
|---|---|---|
| minNumber(number: Int) |IntFieldValidators.MinNumber <br /> extends IntFieldError | lindovo.field(:int).MinNumber($number) |
| maxNumber(number: Int) |IntFieldValidators.MaxNumber <br /> extends IntFieldError | lindovo.field(:int).MaxNumber($number) |



### Added Json coder/encoder

``` scala
import upickle.default.{macroRW, ReadWriter => RW}

object Hero {
  implicit val rw: RW[Hero] = macroRW
}
```

### Shared validator

``` scala
import lindovo.Lindovo
import lindovo.Validators._
import lindovo.form.{LindovoBuilder, LindovoType}
import upickle.default.{macroRW, ReadWriter => RW}

object Hero extends LindovoBuilder[Hero, HeroWorkTemplate] {

  implicit val rw: RW[Hero] = macroRW

  override def builder(m: FormValidatorBuilder): FormValidator =
    new FormValidator(
      name = validateName(m),
      power = m.power,
      age = validateAge(m)
    )

  private def validateName(m: FormValidatorBuilder) = m.name
    .register(isRequire)
    .reg(
      _.length < 3,
      "Legalább 3 karakternek kell lennie"
    )
    .regNot(
      _ matches "[ a-zA-Z]*",
      "Csak az angol ABC karaktereit tartalmazhatja és szóközt."
    )
    .register(it =>
      if (it.head.toString == it.head.toString.toUpperCase)
        None
      else
        Some("Nagy betűvel kell kezdődnie."))
    .reg(_ == "Bob", "Tiltott név")

  private def validateAge(m: FormValidatorBuilder) = m.age
    .reg(
      _ < 13,
      "Kizárólag 13 évesnél idősebb hősök regisztrálhatóak."
    )
    .register(m.name) { (currentAge, currentName) =>
      currentName match {
        case "Deadpool" if currentAge < 18 =>
          Some("Deadpool névvel hős 18 évnél idősebb hősök lehetnek.")
        case _ =>
          None
      }
    }
    
}
```

## How to use on client

!> DEPRECATED!

### Added more validators

``` scala
import example.form.shared.{Hero, HeroWorkTemplate}
import linden.store.ClearContext
import lindovo.{StoreCollectForForm, StoreLindovo}
import lindovo.form.StoreLindovoFormWrapper
import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits._

class HeroForm extends StoreLindovoFormWrapper[Hero, HeroWorkTemplate] {

  override val asStoreForm: Extended = StoreLindovo.extension2

  def apply()(implicit clearContext: ClearContext): StoreCollectForForm[Hero, HeroWorkTemplate] =
    asStoreForm.createFormStore(Hero.builder, storeBuilder)

  def storeBuilder(m: FormValidatorBuilder): FormValidator =
    m.copy(
      name = m.name
        .registerAsync { name =>
          Ajax
            .get(url = "/my-example-url")
            .map { response =>
              Option.when(read[Boolean](response.responseText)) {
                "Ilyen néven már regisztráltak hőst!"
              }
            }
        }
    )
}
```

### Bind to Html

``` scala
import org.scalajs.dom.console
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits._

import upickle.default._

import linden.di.DIMacros.createInstance
import linden.flowers.{Component, ComponentInit}
import lindovo.Validated

import example.form.client.form.HeroForm


object HeroExampleInput
  extends ComponentInit(createInstance[HeroExampleInput](_))

final class HeroExampleInput(heroForm: HeroForm) extends Component {

  private val hero = heroForm()

  private def clickOnCreate(): Unit = {
    hero.get.foreach { hero =>
      console.log(hero.toString)
      Ajax
        .post(
          url = "my-example-register-url",
          data = write(hero)
        )
        .foreach { it =>
          console.log(it.toString)
        }
    }
  }

  override lazy val render: Html = new Html {
    div {
      input(hero.store.name)
      generate(hero.formValidatedStore.name init Validated.empty) { implicit context =>
        _.errors.foreach { it =>
          div {
            +it
          }
        }
      }
      input(hero.store.power)
      generate(hero.formValidatedStore.power init Validated.empty) { implicit context =>
        _.errors.foreach { it =>
          div {
            +it
          }
        }
      }
      input(hero.store.age)
      generate(hero.formValidatedStore.age init Validated.empty) { implicit context =>
        _.errors.foreach { it =>
          div {
            +it
          }
        }
      }
      div {
        generate(hero.validatedStore init None) { implicit context =>
          value =>
            if (value.isDefined)
              div {
                e click clickOnCreate
                +"OK - click to submit"
                div {
                  +value.toString
                }
              }
            else
              div {
                +"Nem OK"
              }
        }
      }
    }
  }
}
```

## How to use on server

!> DEPRECATED!

We are using Akka HTTP library on server side.

### Unmarshaller

``` scala
package example.util.UPickleUnmarshaller

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller.stringUnmarshaller
import akka.stream.Materializer
import upickle.default.{Reader, read}

import scala.concurrent.{ExecutionContext, Future}

class UPickleUnmarshaller[T: Reader] extends Unmarshaller[HttpRequest, T] {
  override def apply(request: HttpRequest)(
    implicit ec: ExecutionContext, materializer: Materializer
  ): Future[T] = stringUnmarshaller(request.entity).map(read[T](_))
}
```

### HTTP Route

``` scala
import scala.util.{Failure, Success}

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer

import upickle.default._

import example.form.Hero
import example.util.UPickleUnmarshaller


object HeroService {
  private var heros: List[Hero] = List.empty[Hero]
}

class HeroService(implicit val materializer: Materializer) extends Directives {

  implicit private def unMarshaller[T: Reader] = new UPickleUnmarshaller[T]

  val route: Route =
    pathPrefix("hero") {
      concat(
        // ...
        path("register") {
          post {
            entity(as[Hero]) { hero =>
              onComplete(hero.asForm.getValidated) {
                case Success(Left(acceptedHero)) =>
                  HeroService.heros :+= acceptedHero
                  complete("OK")
                case Success(Right(errors)) =>
                  println("ERROR: " + errors.toString)
                  complete("OK")
                case Failure(exception) =>
                  println(exception)
                  complete("OK")
              }
            }
          }
        }
      )
    }
}
```
