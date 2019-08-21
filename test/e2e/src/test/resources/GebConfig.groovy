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
	System.properties['browser.location.default'] = "http://ad2dc1selgrd01.tdsops.net:4444/wd/hub"

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
                def driverinstance = new ChromeDriver(chromeOptions)
                driverinstance
            }
        } else {  // use remote grid URL as default
            println "browser.location: Using remote grid as browser location: ${browserLocation}"
            driver = {
                DesiredCapabilities capabilities = DesiredCapabilities.chrome()
								ChromeOptions options = new ChromeOptions();

								options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
				        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
				        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
				        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
				        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
				        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
				        options.addArguments("--disable-gpu");
								capabilities.setCapability(ChromeOptions.CAPABILITY, options);
								
                capabilities.setCapability("acceptSslCerts", true)
                capabilities.setCapability("unexpectedAlertBehaviour", "dismiss")
                new RemoteWebDriver( new URL(browserLocation), capabilities )
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
