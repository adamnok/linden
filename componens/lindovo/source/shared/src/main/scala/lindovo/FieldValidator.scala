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

package lindovo


import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import lindovo.internal.ApplicativeCache

trait FieldError:
  def message: String

object FieldError:
  def apply(value: String): FieldError =
    new FieldError:
      def message: String = value

object FetchableFieldErrors:
  def apply(seq: Seq[FetchableFieldErrors]): FetchableFieldErrors =
    assert(seq.nonEmpty)
    new FetchableFieldErrors:
      override def errors(useCache: Boolean)(using ec: ExecutionContext): Future[Seq[FieldError]] =
        Future.reduceLeft(seq.map(_.errors(useCache)))(_ ++ _)

  def fromFunction(fetcher: ExecutionContext ?=> Boolean => Future[Seq[FieldError]]) =
    new FetchableFieldErrors:
      override def errors(useCache: Boolean)(using ec: ExecutionContext): Future[Seq[FieldError]] =
        fetcher(useCache)

trait FetchableFieldErrors:
  def errors(useCache: Boolean)(using ec: ExecutionContext): Future[Seq[FieldError]]

case class Priority[T](priority: Int, value: T):
  def rePriority(plus: Int) = copy(priority = priority + plus)

given PriorityOrdering: Ordering[Priority[lindovo.FetchableFieldErrors]] =
  new Ordering[Priority[lindovo.FetchableFieldErrors]]:
    def compare(a: Priority[lindovo.FetchableFieldErrors], b: Priority[lindovo.FetchableFieldErrors]) = a.priority compare b.priority

class Group(values: Seq[Priority[FetchableFieldErrors]]):

  def errors(useCache: Boolean)(using ec: ExecutionContext): Future[Seq[FieldError]] =
    getFirstErrorsFrom(useCache)(reducePriority(values))

  infix def +(o: Group): Group = Group(values ++ o.rePriorityValues(nextPriority))

  infix def +(o: FetchableFieldErrors): Group = Group(values :+ Priority(nextPriority, o))

  infix def ++(o: Seq[FetchableFieldErrors]): Group =
    val priority = nextPriority
    Group(values ++ o.map(it => Priority(priority, it)))

  private def maxPriority: Int = values.map(_.priority).maxOption.getOrElse(1)

  private def nextPriority: Int = maxPriority + 1

  private def rePriorityValues(plus: Int) = values.map(_.rePriority(plus))

  private def reducePriority(values: Seq[Priority[FetchableFieldErrors]]): Seq[FetchableFieldErrors] =
    values
      .groupBy(_.priority)
      .map { case (priority, values) =>
        Priority(priority, FetchableFieldErrors(values.map(_.value)))
      }
      .toSeq
      .sorted
      .map(_.value)

  private def getFirstErrorsFrom(useCache: Boolean)(values: Seq[FetchableFieldErrors])(using ec: ExecutionContext): Future[Seq[FieldError]] =
    values match
      case Seq() => Future.successful(Seq())
      case head :: xs =>
        head.errors(useCache) flatMap {
          case Seq() => getFirstErrorsFrom(useCache)(xs)
          case errors => Future.successful(errors)
        }

object FieldValidator:
  import lindovo.internal.{
    CacheableType as AppCacheableType,
    Cacheable as AppCacheable,
    NonCacheable as AppNonCacheable
  }

  type Result = ( Option[String] | Option[FieldError] | Future[Option[String]] | Future[Option[FieldError]] )
  type UnionResult = ( Result | AppCacheable[Result] | AppNonCacheable[Result] )
  type EachFunction[A] = A => UnionResult
  type AsyncFunction[A] = ExecutionContext ?=> A => AppCacheableType[Future[Seq[FieldError]]]

  private def resolveResult[A](result: UnionResult)(using ec: ExecutionContext): AppCacheableType[Future[Seq[FieldError]]] =
    result match
      case AppCacheable(it) => resolveResult(it.asInstanceOf[Result]).toCacheable
      case AppNonCacheable(it) => resolveResult(it.asInstanceOf[Result]).toNonCacheable
      case None => AppCacheable(Future.successful(Seq()))
      case Some(message: String) => AppCacheable(Future.successful(Seq(FieldError(message))))
      case Some(error: FieldError) => AppCacheable(Future.successful(Seq(error)))
      case it: Future[_] => AppCacheable {
        it.map {
          case None => Seq()
          case Some(it) => 
            it match
              case error: FieldError => Seq(error)
              case message: String => Seq(FieldError(message))
        }
      }

  private def resolve[A](condition: EachFunction[A])(using ExecutionContext): AsyncFunction[A] =
    (input: A) => resolveResult(condition(input))

  private def createFetchable[A](value: ExecutionContext ?=> Future[A], condition: EachFunction[A]): FetchableFieldErrors =
    val applicativeCache = new ApplicativeCache[A, Future[Seq[FieldError]]]()
    FetchableFieldErrors.fromFunction { useCache =>
      value.flatMap(applicativeCache(_, useCache)( resolve(condition)))
    }

  private[FieldValidator] def createFetchables[A](value: ExecutionContext ?=> Future[A], conditions: Seq[EachFunction[A]]): FetchableFieldErrors =
    FetchableFieldErrors(conditions.map(createFetchable(value, _)))

  object NonCacheable:
    def apply[A](condition: A => Result): EachFunction[A] = (input: A) => AppNonCacheable[Result](condition(input))
    def apply(value: Result): AppNonCacheable[Result] = AppNonCacheable[Result](value)


class FieldValidator[T](val optional: Boolean, val value: () => Future[T], private val group: Group = Group(Seq())):
  import FieldValidator.{EachFunction, createFetchables}

  export group.errors


  private def create(n: FetchableFieldErrors) = FieldValidator[T](optional, value, group + n)

  def register(condition: EachFunction[T]*) = 
    create(createFetchables(tupleFuture(value()), condition))

  def register[G](useG: FieldValidator[G])(condition: EachFunction[(T, G)]*) =
    create(createFetchables(tupleFuture(value(), useG.value()), condition))

  def register[G, H](useG: FieldValidator[G], useH: FieldValidator[H])(condition: EachFunction[(T, G, H)]*) =
    create(createFetchables(tupleFuture(value(), useG.value(), useH.value()), condition))

  def register[G, H, J](useG: FieldValidator[G], useH: FieldValidator[H], useJ: FieldValidator[J])(condition: EachFunction[(T, G, H, J)]*) =
    create(createFetchables(tupleFuture(value(), useG.value(), useH.value(), useJ.value()), condition))

  def register[G, H, J, K](
    useG: FieldValidator[G],
    useH: FieldValidator[H],
    useJ: FieldValidator[J],
    useK: FieldValidator[K]
  )(condition: EachFunction[(T, G, H, J, K)]*) =
    create(createFetchables(tupleFuture(value(), useG.value(), useH.value(), useJ.value(), useK.value()), condition))

  private inline def tupleFuture[A](a: Future[A])(using ExecutionContext): Future[A] =
    a
    
  private inline def tupleFuture[A, B](a: Future[A], b: Future[B])(using ExecutionContext): Future[(A, B)] =
    for
      aVal <- a
      bVal <- b
    yield (aVal, bVal)

  private inline def tupleFuture[A, B, C](a: Future[A], b: Future[B], c: Future[C])(using ExecutionContext): Future[(A, B, C)] =
    for
      aVal <- a
      bVal <- b
      cVal <- c
    yield (aVal, bVal, cVal)

  private inline def tupleFuture[A, B, C, D](a: Future[A], b: Future[B], c: Future[C], d: Future[D])(using ExecutionContext): Future[(A, B, C, D)] =
    for
      aVal <- a
      bVal <- b
      cVal <- c
      dVal <- d
    yield (aVal, bVal, cVal, dVal)

  private inline def tupleFuture[A, B, C, D, E](a: Future[A], b: Future[B], c: Future[C], d: Future[D], e: Future[E])(using ExecutionContext): Future[(A, B, C, D, E)] =
    for
      aVal <- a
      bVal <- b
      cVal <- c
      dVal <- d
      eVal <- e
    yield (aVal, bVal, cVal, dVal, eVal)

  infix def + (o: FieldValidator[T]) =  FieldValidator[T](optional, value, group + o.group)
    
