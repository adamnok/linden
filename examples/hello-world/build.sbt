scalaVersion := "3.1.0"
organization := "example"

resolvers += "DongoTeam repository" at "https://public.repository.dongoteam.hu"

scalaJSUseMainModuleInitializer := false

libraryDependencies ++= Seq(
  "org.dongoteam.linden" %%% "linden-package-flowers" % "1.3.0"
)


enablePlugins(
  ScalaJSPlugin,
  WebPackPlugin,
  ServePlugin
)

serveExtra := Seq()
serve := (serve dependsOn webpack dependsOn (Compile / fastOptJS)).value
