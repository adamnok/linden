import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val commonSettings = Seq(
  scalaVersion := "3.1.0",
  organization := "org.dongoteam.linden.example.form",
  resolvers += "DongoTeam repository" at "https://public.repository.dongoteam.hu"
)

lazy val server = (project in file("source/server"))
  .settings(commonSettings)
  .settings(
    webpackSource := Some(client),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.11" cross CrossVersion.for3Use2_13,
      "com.typesafe.akka" %% "akka-stream" % "2.6.3" cross CrossVersion.for3Use2_13,
    ),
    webpack := webpack.dependsOn(client / Compile / fastOptJS).value,
    (Compile / unmanagedResourceDirectories) += (Compile / target).value / "webpack"
  )
  .enablePlugins(WebPackPlugin)
  .dependsOn(sharedJvm)

lazy val client = (project in file("source/client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := false,
    libraryDependencies ++= Seq(
      "org.dongoteam.linden" %%% "linden-package-flowers" % "1.3.0"
    )
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(sharedJs)


lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("source/shared"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "1.4.1",
      "org.dongoteam.linden" %% "lindovo" % "1.3.0"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "1.4.1",
      "org.dongoteam.linden" %%% "lindovo" % "1.3.0"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "1.4.1",
      "org.dongoteam.linden" %% "lindovo" % "1.3.0"
    )
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }
