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
name := "linden-core"
version := "1.3.0"

//scalaVersion := "2.12.7"
//scalaVersion := "2.13.1"
scalaVersion := "3.1.0"

scalacOptions += "-source:future"
//scalacOptions += "-Yindent-colons"
//crossScalaVersions := Seq("2.12.7", "2.13.0-M5")

enablePlugins(ScalaJSPlugin)


//libraryDependencies += "org.scala-js" % "scalajs-dom_sjs0.6_2.12" % "0.9.5"
//libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6"

//libraryDependencies += "org.wvlet.airframe" %% "airframe" % "0.42"

libraryDependencies += "io.monix" %%% "minitest" % "2.9.6" % "test"
testFrameworks += new TestFramework("minitest.runner.Framework")


Compile / sourceGenerators += Def.task {
  import org.jtwig.JtwigModel
  import org.jtwig.JtwigTemplate
  import java.nio.file.Files
  import java.nio.file.FileVisitOption
  import java.util.stream.Collectors
  import scala.collection.JavaConverters._
  import java.io.File
  val managedFiles = sourceDirectory.value / "main" / "scala"
  val files: Set[File] = Files
    .find(
      managedFiles.toPath,
      20,
      (p, _) => !p.toString.endsWith(".macro.scala.twig") && p.toString.endsWith(".scala.twig"),
      FileVisitOption.FOLLOW_LINKS
    )
    .collect(Collectors.toSet())
    .asScala
    .map(_.toFile)
    .toSet
  println(files)
  val cachedFun = FileFunction.cached(streams.value.cacheDirectory / "twig", FilesInfo.lastModified) { (in: Set[File]) =>
    in.map { file =>
      val targetScalaFileName = file.name.take(file.name.lastIndexOf('.'))
      val relativeTarget = file.relativeTo(managedFiles).get.toPath.getParent.resolve(targetScalaFileName).normalize
      val outPath = ((Compile / sourceManaged).value / "main").toPath.resolve(relativeTarget.toString)

      val template = JtwigTemplate.fileTemplate(file.getAbsolutePath)
      Files.createDirectories(outPath.getParent)
      Files.deleteIfExists(outPath)
      Files.write(outPath, template.render(JtwigModel.newModel).getBytes)
      println(s"twig compile to $outPath")
      outPath.toFile
    }
  }
  cachedFun(files).toSeq
}.taskValue

pomExtra :=
  scala.xml.NodeSeq.fromSeq(
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
