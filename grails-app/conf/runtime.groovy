import grails.util.Environment

// copy binding variables into properties in the config for visibility in external scripts; as of 2.5.4 the
// vars are: appName, appVersion, basedir, baseFile, baseName, grailsHome,
//           grailsSettings, grailsVersion, groovyVersion, springVersion, userHome
getBinding().variables.each { name, value -> setProperty name, value }
String appName = this.appName ?: 'tdstm'

grails.config.locations = []

// Change the bindData default so that it doesn't automatically convert blanks to null when storing in the database
// See http://docs.grails.org/latest/guide/theWebLayer.html#dataBinding
grails.databinding.convertEmptyStringsToNull = false

List candidates = []
String configManagerFile = null
String configFileFromJavaOpts = System.getProperty("${appName}.config.location")

if (configFileFromJavaOpts){
	candidates << configFileFromJavaOpts
	try{ //get the possible Manager config File
		File f = new File(configFileFromJavaOpts)
		configManagerFile = "${f.getParent()}/licman-config.groovy"
	}catch(e){
		//configuration not found fails silently??
	}
}

//check if the manager config file was injected on the JavaOPTS
configManagerFile = System.getProperty("licManConf") ?: configManagerFile
if(configManagerFile){
	candidates << configManagerFile
}

if($userHome) {
	candidates << "$userHome/.grails/${appName}-config.groovy"
}

boolean foundAppConfig=false
for (appConfigLocation in candidates) {
	File f
	try {
		f = new File(appConfigLocation)
	}
	catch (e) {
		throw new IllegalArgumentException("ERROR Invalid application configuration file path '$appConfigLocation'")
	}

	if (!f.exists()) continue

	try {
		// Test that there are no errors in the config syntax
		new ConfigSlurper(Environment.current.name).parse(f.toURI().toURL())
	}
	catch (e) {
		throw new IllegalArgumentException("ERROR There appears to be an error in the $appConfigLocation application configuration file: $e.message")
	}

	grails.config.locations << 'file:' + appConfigLocation
	println "INFO The application config was loaded from ${appConfigLocation}"

	foundAppConfig = true
}
if (!foundAppConfig) {
	if (Environment.current.name == 'test') {
		println "ERROR The application configuration file was not found in the following locations: ${candidates}"
	} else {
		throw new IllegalArgumentException("The application configuration file was not found in the following locations: ${candidates}")
	}
}