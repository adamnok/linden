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

lazy val commonSettings = Seq(
  //scalaVersion := "2.12.7",
  //scalaVersion := "2.13.1",
  scalaVersion := "3.1.0",
  //crossScalaVersions := Seq("2.12.7", "2.13.0-M5"),
  crossScalaVersions := Nil,
  version := "1.3.0",
  organization := "org.dongoteam.linden",
  resolvers += "DongoTeam repository" at "https://public.repository.dongoteam.hu",
  scalacOptions += "-source:future",
  pomExtra := scala.xml.NodeSeq.fromSeq(
    <licenses>
      <license>
        <name>MIT License</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <organization>
      <name>DongoTeam</name>
      <url>https://dongoteam.hu</url>
    </organization>
    <developers>
      <developer>
        <id>AdamNok</id>
        <name>Adam Nok</name>
        <email>adamnok@protonmail.com</email>
      </developer>
    </developers>
  )
)

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .in(file("source"))
  .settings(commonSettings)
  .jsSettings(
    name := "linden-dependency-injection",
    libraryDependencies ++= Seq(
      "io.monix" %%% "minitest" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("minitest.runner.Framework")
  )
  .jvmSettings(
    name := "linden-dependency-injection",
    libraryDependencies ++= Seq(
      "io.monix" %% "minitest" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("minitest.runner.Framework")
  )
