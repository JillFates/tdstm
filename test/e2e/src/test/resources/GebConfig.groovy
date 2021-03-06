/*
	This is the Geb configuration file.

	See: http://www.gebish.org/manual/current/#configuration
*/

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.Platform
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

waiting {
	timeout = 20
}

environments {

    // Grid default URL
	System.properties['browser.location.default'] = "http://chrome:4444"

    // Use Grid default URL if browser.location is not set
    def browserLocation = System.properties['browser.location'] ?: System.properties['browser.location.default']

	// run via “./gradlew chromeTest”
	// See: http://code.google.com/p/selenium/wiki/ChromeDriver
	chrome {
		atCheckWaiting = 10

        if (browserLocation == 'local') {  // use local browser (not grid)
            println "browser.location: Using local chrome browser : ${browserLocation}"
            driver = {
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
								new ChromeDriver(chromeOptions)
            }
        } else {  // use remote grid URL as default
            println "browser.location: Using remote grid as browser location: ${browserLocation}"
            driver = {
                DesiredCapabilities capabilities = DesiredCapabilities.chrome()
								ChromeOptions options = new ChromeOptions();

								options.addArguments("--headless");
								options.addArguments("--start-maximized");
				        options.addArguments("--enable-automation");
				        options.addArguments("--no-sandbox");
				        options.addArguments("--disable-infobars");
				        options.addArguments("--disable-dev-shm-usage");
				        options.addArguments("--disable-browser-side-navigation");
				        options.addArguments("--disable-gpu");
                        options.addArguments("--window-size=2048,1080");

								options.addArguments("--verbose");
								options.addArguments("--enable-logging");
								options.addArguments("--v=1");
								options.addArguments("--user-data-dir=/home/automation");
								options.addArguments("--log-path=chromedriver.log")

								capabilities.setCapability(ChromeOptions.CAPABILITY, options);

                capabilities.setCapability("acceptSslCerts", true)
                capabilities.setCapability("unexpectedAlertBehaviour", "dismiss")

								new RemoteWebDriver(new URL(browserLocation), capabilities)
				    }
        }
	}

	// run via “./gradlew firefoxTest”
	// See: http://code.google.com/p/selenium/wiki/FirefoxDriver
	firefox {
		atCheckWaiting = 10

        if (browserLocation == 'local') {  // use local browser (not grid)
            println "browser.location: Using local firefox browser : ${browserLocation}"
            driver = {
                driverinstance = new FirefoxDriver()
                driverinstance.manage().window().maximize()
                driverinstance
            }
        } else {  // use remote grid URL as default
            println "browser.location: Using remote grid browser location: ${browserLocation}"
            driver = {
                DesiredCapabilities capabilities = DesiredCapabilities.firefox()
                //capabilities.setVersion("45.4.0esr")
                //capabilities.setPlatform(Platform.LINUX)
                new RemoteWebDriver( new URL(browserLocation), capabilities )

                // This code shows how to modify the Firefox profile settings
                //FirefoxProfile profile = new FirefoxProfile()
                //profile.setPreference("intl.accept_languages", "en-us")
                //def driverInstance = new FirefoxDriver(profile)
                //driverInstance
            }
        }

	}

//	println "*** START - All System Properties settings seen by GebConfig ***"
//	def tmgc = System.properties as TreeMap
//	tmgc.each { println it }
//    println "*** END - All System Properties settings seen by GebConfig *****"

}

// To run the tests with chrome and firefox browsers just run “./gradlew test”

// Set or get the baseUrl of the system to be tested
baseUrl = System.properties['geb.build.baseUrl'] ?: 'https://tmqa11.transitionmanager.net'
println "geb.build.baseUrl: Testing qa environment ${baseUrl}"

//reportsDir = "target/geb-reports"
