scalaVersion := "3.1.0"
organization := "example"
name := "todoapp"

resolvers += "DongoTeam repository" at "https://public.repository.dongoteam.hu"

scalaJSUseMainModuleInitializer := false
libraryDependencies ++= Seq(
  "org.dongoteam.linden" %%% "linden-package-flowers" % "1.3.0",
  "org.dongoteam.linden" %%% "lindovo" % "1.3.0",
  "org.dongoteam.linden" %%% "linden-extension-to-bulma" % "1.3.0"
)


enablePlugins(
  ScalaJSPlugin,
  WebPackPlugin,
  SassPackPlugin,
  ServePlugin
)


serveExtra := Seq()
serve := (serve dependsOn webpack dependsOn sass dependsOn (Compile / fastOptJS)).value

scalacOptions += "-source:future"
//scalacOptions += "-Yindent-colons"
