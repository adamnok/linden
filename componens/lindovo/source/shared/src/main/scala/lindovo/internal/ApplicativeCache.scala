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

package lindovo.internal


trait CacheableType[C]:
  val value: C
  def toNonCacheable = NonCacheable(value)
  def toCacheable = Cacheable(value)
case class Cacheable[C](value: C) extends CacheableType[C]
case class NonCacheable[C](value: C) extends CacheableType[C]

/**
 * @tparam K type of cachable key
 * @tparam D type of cachable data
 */
class ApplicativeCache[K, D]:
  /**
   * The last value that we used. None - we never used any data.
   */
  private var last: Option[(K, D)] = None

  /**
   * @param key Key value whereof we van create the data.
   * @param withCache Can we skip calculation and use data from cache or not.
   * - true if we want to use cache
   * - false if recreation should be forced.
   */
  def apply(key: K, withCache: Boolean)(create: K  => CacheableType[D]): D =
    this.synchronized {
      last match
        case Some(lastKey, lastData) if withCache && key == lastKey =>
          lastData
        case _ =>
          create(key) match
            case Cacheable(data: D) =>
              last = Some(key -> data)
              data
            case NonCacheable(data: D) =>
              data
    }
