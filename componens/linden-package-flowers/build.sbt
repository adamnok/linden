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

organization := "org.dongoteam.linden"
name := "linden-package-flowers"
version := "1.3.0"

//scalaVersion := "2.12.7"
//scalaVersion := "2.13.1"
scalaVersion := "3.1.0"
//crossScalaVersions := Seq("2.12.7", "2.13.0-M5")

val UpickleVersion = "1.4.1"

enablePlugins(ScalaJSPlugin)


libraryDependencies ++= Seq(
  "org.dongoteam.linden" %%% "linden-core" % "1.3.0",
  "org.dongoteam.linden" %%% "linden-browser" % "1.3.0",
  "org.dongoteam.linden" %%% "linden-dependency-injection" % "1.3.0",
  "com.lihaoyi" %%% "upickle" % UpickleVersion,
  /*
   * TEST
   */
  "io.monix" %%% "minitest" % "2.9.6" % "test"
)

testFrameworks += new TestFramework("minitest.runner.Framework")

scalacOptions += "-source:future"

pomExtra := scala.xml.NodeSeq.fromSeq(
  <licenses>
    <license>
      <name>MIT License</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <site>
    <url>https://linden.dongoteam.hu</url>
  </site>
  <developers>
    <developer>
      <id>AdamNok</id>
      <name>Adam Nok</name>
      <email>adamnok@protonmail.com</email>
    </developer>
  </developers>
)
