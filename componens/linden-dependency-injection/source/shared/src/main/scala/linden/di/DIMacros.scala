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

package linden.di

import scala.quoted.*

object DIMacros:

  inline def ref[T]: Reference[T] = ${referenceMacro[T]}

  inline def reference[T]: Reference[T] = ${referenceMacro[T]}

  def referenceMacro[T](using t: Type[T])(using quotes: Quotes): Expr[Reference[T]] =
    '{ linden.di.Reference[T](${Expr(Type.show[T])}) }

  inline def createInstanceReverseArgs[T](session: SessionDI): T =
    ${createInstanceArgsReverseMacro[T]('session)}

  def createInstanceArgsReverseMacro[T](session: Expr[SessionDI])(using t: Type[T])(using quotes: Quotes): Expr[T] =
    createInstanceArgsFullMacro[T](session)(Seq())

  inline def createInstanceReverseArgs[T](session: SessionDI)(inline args1: Any*): T =
    ${createInstanceArgsReverseMacro[T]('session)('args1)}

  def createInstanceArgsReverseMacro[T](
    session: Expr[SessionDI]
  )(
    args1: Expr[Seq[Any]]
  )(using Type[T], Quotes): Expr[T] =
    createInstanceArgsFullMacro[T](session)(Seq(args1))

  inline def createInstanceReverseArgs[T](session: SessionDI)(inline args1: Any*)(inline args2: Any*): T =
    ${createInstanceArgsReverseMacro[T]('session)('args1, 'args2)}

  def createInstanceArgsReverseMacro[T](
    session: Expr[SessionDI]
  )(
    args1: Expr[Seq[Any]],
    args2: Expr[Seq[Any]]
  )(using Type[T], Quotes): Expr[T] =
    createInstanceArgsFullMacro[T](session)(Seq(args1, args2))

  inline def createInstanceReverseArgs[T](session: SessionDI)(inline args1: Any*)(inline args2: Any*)(inline args3: Any*): T =
    ${createInstanceArgsReverseMacro[T]('session)('args1, 'args2, 'args3)}

  def createInstanceArgsReverseMacro[T](
    session: Expr[SessionDI]
  )(
    args1: Expr[Seq[Any]],
    args2: Expr[Seq[Any]],
    args3: Expr[Seq[Any]]
  )(using Type[T], Quotes): Expr[T] =
    createInstanceArgsFullMacro[T](session)(Seq(args1, args2, args3))

  def createInstanceArgsFullMacro[T](
    session: Expr[SessionDI]
  )(
    argsGroup: Seq[Expr[Seq[Any]]]
  )(using t: Type[T])(using quotes: Quotes): Expr[T] =
    import quotes.reflect.*

    val qualifiedName = Type.show[T]
    val qualifiedNameEpr = Expr[String](qualifiedName)

    val primaryConstructor: Symbol = TypeRepr.of[T].typeSymbol.primaryConstructor

    val typeParameters = TypeRepr.of[T] match {
      case appliedType: AppliedType => appliedType.args.map(Inferred(_))
      case _ => List()
    }

    val constructorParamLists: List[List[Symbol]] =
      primaryConstructor.paramSymss
        .map(_.filterNot(_.flags.is(Flags.Given)))
        .filter(_.nonEmpty)

    if (constructorParamLists.length == 0) {
      TypeRepr.of[T].classSymbol.map(sym =>
        Apply(Select(New(TypeTree.of[T]), sym.primaryConstructor), List()).asExprOf[T]
      ).getOrElse(???)
    } else if (!Set(argsGroup.length, argsGroup.length + 1).contains(constructorParamLists.length - typeParameters.length)) {
      val params = constructorParamLists.map {
        _.map(sym => (sym.name, TypeRepr.of[T].memberType(sym).show))
          .map((a, b) => s"$a = $b")
          .mkString
      }.mkString(" ", "(", ")")
      throw IllegalArgumentException(
        s"Expected length for $qualifiedName is ${argsGroup.length} or ${argsGroup.length + 1} but I have got ${constructorParamLists.length}: $params"
      )
    } else {
      def getInjectableArgs =
        if constructorParamLists.length == argsGroup.length
        then None
        else
          val formattedParams: Seq[(String, String, TypeRepr)] = constructorParamLists.last
            .map(sym => (sym.name, TypeRepr.of[T].memberType(sym).show, TypeRepr.of[T].memberType(sym)))

          val injectedArgs = formattedParams
            .map { (rawName, rawTypez, typeRepr: TypeRepr) =>
              val name = Expr[String](rawName)
              val typez = Expr[String](rawTypez)
              typeRepr.asType match
                case '[t] =>
                  '{
                    $session.get[t]($typez, $name)
                      .getOrElse {
                        throw IllegalArgumentException(
                          s"Not found value in the session container for {$$qualifiedNameEpr}. Parameter was {$$name: $$typez}"
                        )
                      }
                  }.asTerm
            }
            Some(injectedArgs)
      def resolve(args: Expr[Seq[Any]]) = 
        args match
          case Varargs(arguments) => arguments.map(_.asTerm)
      val explicitArgs =
        argsGroup.map(resolve)
      TypeRepr.of[T].classSymbol.map( sym =>
        val selected = Select(New(TypeTree.of[T]), sym.primaryConstructor)
        def useTypeParemeters(parent: Select)(args: List[TypeTree]): Term =
          if args.isEmpty then parent
          else TypeApply(parent, args)
        def useParameters(parent: Term)(ags: Seq[Seq[Term]]): Term =
          ags match
            case Nil => parent
            case it :: xs => useParameters(Apply(parent, it.toList))(xs)
        def fullArgs = 
          getInjectableArgs match
            case None => explicitArgs
            case Some(injectedArgs) => explicitArgs :+ injectedArgs
        useParameters(useTypeParemeters(selected)(typeParameters))(fullArgs).asExprOf[T]
      ).getOrElse(???)
    }
  end createInstanceArgsFullMacro

end DIMacros