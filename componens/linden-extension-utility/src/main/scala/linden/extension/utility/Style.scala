/*
 * MIT LICENCE
 * 
 * Copyright (c) 2021 Adam Nok [adamnok@protonmail.com]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package linden.extension.utility

import linden.extension.utility.BuildStyledBlockElement.StyleEvaluationParameter

trait RawEntity:
  private[utility] def makeEntitiesString: String

trait Style:

  protected def initEntity: EntityGroup = Entity.empty

  protected final def composeEntities(a: EntityGroup, b: EntityGroup): EntityGroup = a & b

  protected final def extractEntity(entityGroup: EntityGroup): String =
    entityGroup.entities
      .filterNot(entityGroup.ignores.contains)
      .map(_.name)
      .mkString(" ")

  protected final def empty(): EntityGroup = Entity.empty

  protected final def create(name: String): EntityGroup = Entity(name)

  protected final def createWithIgnores(name: String, ignores: Seq[EntityGroup]): EntityGroup =
    Entity(name).withIgnore(ignores.flatMap(_.entities).distinct.toList)

  private[utility] case class EvaluationParameter(
    init: EntityGroup,
    compose: (EntityGroup, EntityGroup) => EntityGroup,
    extract: EntityGroup => String
  ) extends StyleEvaluationParameter[EntityGroup]

  private[utility] def evaluationParameter: EvaluationParameter =
    EvaluationParameter(initEntity, composeEntities, extractEntity)


  sealed trait EntityGroup extends RawEntity:
    private[Style] val entities: List[Entity]
    private[Style] val ignores: List[Entity]

    final def &(o: EntityGroup): EntityGroup = new EntityGroupIml(entities ++ o.entities, ignores ++ o.ignores)

    private[utility] final def makeEntitiesString: String = entities.map(_.name).mkString(" ")

  private final class EntityGroupIml private[Style](
    private[Style] override val entities: List[Entity],
    private[Style] override val ignores: List[Entity]
  ) extends EntityGroup

  object Entity:
    private[Style] def empty: EntityGroup = new EntityGroupIml(List(), List())

  final case class Entity private[Style](name: String) extends EntityGroup:
    private[Style] override val entities: List[Entity] = List(this)
    private[Style] override val ignores: List[Entity] = List()

    protected[Style] def withIgnore(ignore: Entity, ignores: Entity*): EntityGroup = withIgnore((ignore +: ignores).toList)

    protected[Style] def withIgnore(ignores: List[Entity]): EntityGroup = new EntityGroupIml(entities, ignores)
