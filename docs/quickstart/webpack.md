# Webpack plugin

The *sbt-site* SBT plugin is working and everything is fine with him, however you can use a better plugin for developing to SPA application. It is the *Linden Webpack* plugin that you can find in the *linden-plugin-pack* plugin package. This package contains some useful plugin for SPA applications.


## Install
First step, you need to put this plugin into the `project/plugins.sbt` file.

``` scala
/* project/plugins.sbt file */
addSbtPlugin("org.dongoteam.linden" % "linden-plugin-pack" % "<last version>")
```

Then you need to enable `WebPackPlugin` and `ServePlugin` for your project.

``` scala
/* build.sbt file */
enablePlugins(
  ScalaJSPlugin,
  WebPackPlugin,
  ServePlugin
)
```

The lates step, define dependencie between tasks. If we use `serve` command, we want to run `webpack` and `fastOptJS` command too.

``` scala
/* build.sbt file */
serve := (serve dependsOn webpack dependsOn (Compile / fastOptJS)).value
```

## How to use

 - To generate webpack, just run `webpack` command.
 - To starting a local static webserver, just run `serve` command.

That's all!

## Output

You can find the generatet outputs in the `target/webapck` folder.