package example.functionalstore.client.state

import scala.language.implicitConversions
import linden.store.functional.types.*
import linden.store.functional.{Action, Store}

final class HeroState(val name: Simple[Option[String]] = Store(None)):
  lazy val actions = new HeroStateActions(this)

final class HeroStateActions(heroStore: HeroState) extends Action:
  def fillWithHero(heroName: String): Unit = heroStore.name change Some(heroName)
