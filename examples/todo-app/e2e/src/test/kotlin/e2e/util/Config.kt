package e2e.config

import org.dongoteam.pendler.envs.dfa.DFAConfig
import org.dongoteam.pendler.envs.dfa.Global
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver


class Config : DFAConfig() {
    override fun createWebDriver(): WebDriver {
        System.setProperty("webdriver.edge.driver", "C:\\ProgramFiles\\SeleniumWebDrivers\\msedgedriver-94.exe")

        val driver: WebDriver = EdgeDriver()
        //driver.quit()
        return driver

        /*val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--disable-extensions")
        options.addArguments("--no-sandbox")
        //options.addArguments("--disable-dev-shm-usage")
        options.addArguments("start-maximized")
        //options.addArguments("disable-infobars")
        //options.addArguments("--disable-gpu")
        //options.setExperimentalOption("useAutomationExtension", false)
        return ChromeDriver(options)*/
    }

    override fun initTestCase(global: Global): Task =
        Task {
            web {
                open(global["url"] as String) alias "default"
            }
            code {
            }
        }

    override fun endTestCase(global: Global): Task =
        Task {
            web {
                close()
            }
        }
}