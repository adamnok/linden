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

ThisBuild / version := "1.3.0"
ThisBuild / organization := "org.dongoteam.linden"
ThisBuild / homepage := Some(url("https://dongoteam.hu"))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.13",
    name := "linden-plugin-pack",
    (pluginCrossBuild / sbtVersion) := "1.5.5",
    (pluginCrossBuild / scalaVersion) := "2.12.13"
  )
  .settings(
    libraryDependencies ++= Seq(
      /**
       * @see https://mvnrepository.com/artifact/com.lihaoyi/upickle
       */
      "com.lihaoyi" %% "upickle" % "1.4.1",
      "com.sparkjava" % "spark-core" % "2.9.3",
      "com.typesafe" % "config" % "1.4.1"
    )
  )

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
