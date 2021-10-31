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

package linden.router

import linden.component.{Component, ComponentFactory}
import linden.definition.Html
import linden.store.ReadStore


class Router[S](private val routs: Seq[(String, RoutParams => (Any, ComponentFactory[S]))])(locator: Locator) extends Component[S]:

  private val currentComponentStore: ReadStore[Option[ComponentFactory[S]]] =
    locator.url
      .map { url =>
        val urlHandler = new UrlHandler(url)
        routs
          .find(rout => urlHandler.isPattern(rout._1))
          .map(it => it._2(urlHandler.hashPattern(it._1).map(RoutParams.apply).get))
      }
      .flowControl((old, current) => old.isDefined && current.isDefined && old.get._1 == current.get._1)
      .map(_.map(_._2))

  override def render: Html[S] = new IHtml:
    generate(currentComponentStore) { it =>
      it.foreach(+_)
    }
