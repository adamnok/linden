# Dependencies

## Prerequisite

Prerequisites for the Linden is the next:

 - **[Scala](https://www.scala-lang.org/)** version must be minimum **3.1.0**
 - **[Scala.js](https://www.scala-js.org/)** version must be mimimum **1.7.0**

If you use **[SBT](https://www.scala-sbt.org/)** and you want to use *Linden plugin pack*, its version should be minimum **1.5.5**. This documentation contains description to SBT.

## Install linden flowers

It would be best if you appended a linden package to build.sbt of the project as a dependency.

The `linden-package-flowers` is recommended to use that contains a few useful linden's components. For example, the most important components are `linden-core`, `routing`, `browser-renderer`, and `di`.

``` scala
resolvers +=
  "DongoTeam repository" at "https://public.repository.dongoteam.hu"
libraryDependencies +=
  "org.dongoteam.linden" %%% "linden-package-flowers" % "<last version>"
```

## Install ScalaJS

You can access exact information about ScalaJS on the official website of ScalaJS: https://www.scala-js.org.

<!-- tabs:start -->

#### ** build.sbt **
``` scala
scalaVersion := "3.1.0"

enablePlugins(ScalaJSPlugin)

// This is an application with a main method (or false if you wanna customized entry point)
scalaJSUseMainModuleInitializer := true
```
#### ** project/plugins.sbt **
``` scala
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.0")
```

<!-- tabs:end -->