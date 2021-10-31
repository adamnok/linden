package example.form.shared

import lindovo.FieldValidators.*
import upickle.default.{macroRW, ReadWriter => RW}
import lindovo.{FieldValidator, FormMapping}

case class Hero(
  name: String,
  power: Option[String],
  age: Int
)

case class HeroWorkTemplate[T[_]](
  name: T[String],
  power: T[Option[String]],
  age: T[Int]
)

object Hero:

  given RW[Hero] = macroRW

  given FormMapping[Hero, HeroWorkTemplate](builder)

  def builder(m: HeroWorkTemplate[FieldValidator]) =
    import m.*
    copy(
      name = name
        .register(minTextLength(3))
        .register(onlyEnglishCharacters(), startTextWithUpperCase())
        .register(notAllowedTexts(Set("Bob"))),
      age = age
        .register(minNumber(13)("Kizárólag 13 évesnél idősebb hősök regisztrálhatóak."))
        .register(name) {
          case (currentAge, "Deadpool") if currentAge < 18 =>
            Some("Deadpool névvel hős 18 évnél idősebb hősök lehetnek.")
          case _ => None
        }
    )

  private def minTextLength(length: Int)(it: String) =
    Option.when(it.length < length)(s"Legalább $length karakternek kell lennie")

  private def onlyEnglishCharacters()(it: String) =
    Option.unless(it matches "[ a-zA-Z]*")("Csak az angol ABC karaktereit tartalmazhatja és szóközt.")

  private def startTextWithUpperCase()(it: String) =
    val firstLetter = it.head.toString
    Option.when(firstLetter != firstLetter.toUpperCase)("Nagy betűvel kell kezdődnie.")

  private def notAllowedTexts(notAllowed: Set[String])(it: String) =
    Option.when(notAllowed contains it)("Tiltott név")
  
  private def minNumber(number: Int)(errorMessage: String)(it: Int) = 
    Option.when(number > it)(errorMessage)
  

