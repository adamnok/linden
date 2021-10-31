package linden.plugin.plugins.webpack.model.index

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

object Index {
  private val defaultConfig =
    """
      |https = true
      |title = ""
      |""".stripMargin

  implicit class OptionConfig(config: Config) {
    private def option[T](fn: => T) =
      try {
        Some(fn)
      } catch {
        case _: ConfigException.Missing => None
      }

    def getOptionString(path: String) = option(config.getString(path))
  }

  def apply(config: Config): Index = fromConfig(config.withFallback(ConfigFactory.parseString(defaultConfig)))

  private def fromConfig(config: Config): Index = {
    Index(
      httpsRedirection = config.getBoolean("https"),
      themeColor = config.getOptionString("theme"),
      title = config.getString("title"),
      refFonts = Seq(),
      refCss = Seq(),
      refJs = Seq()
    )
  }
}

case class Index(
  httpsRedirection: Boolean,
  themeColor: Option[String],
  title: String,
  refFonts: Seq[String],
  refCss: Seq[String],
  refJs: Seq[String]
) {

  private def genThemeColorFN(themeColor: String) =
    Seq("theme-color", "msapplication-navbutton-color", "apple-mobile-web-app-status-bar-style")
      .map(it => it -> themeColor)
      .map { case (key, value) =>
        s"""<meta name="$key" content="$value">"""
      }
      .mkString("\n")

  private def genThemeColor = themeColor.map(genThemeColorFN).getOrElse("")

  private def genTitle = s"<title>$title</title>"

  private def genHttpsRedirectionFN =
    """
      |<script>
      |  if (location.protocol === 'http:' && location.hostname !== 'localhost') {
      |    location.replace(`https:${location.href.substring(location.protocol.length)}`);
      |  }
      |</script>
      |""".stripMargin.trim

  private def genHttpsRedirection =
    if (httpsRedirection) genHttpsRedirectionFN
    else ""

  private def genFonts =
    s"""
       |<style type="text/css">
       |  ${refFonts.mkString("\n")}
       |</style>
       |""".stripMargin.trim

  def generate =
    s"""
       |<!DOCTYPE html>
       |<html>
       |  <head>
       |    <meta charset="utf-8">
       |    $genHttpsRedirection
       |    $genTitle
       |    $genThemeColor
       |    $genFonts
       |    ${refCss.mkString("\n")}
       |    ${refJs.mkString("\n")}
       |  </head>
       |  <body onload="if(typeof main !== undefined) main()">
       |    <div id="app"></div>
       |  </body>
       |</html>
       |""".stripMargin.trim

}
