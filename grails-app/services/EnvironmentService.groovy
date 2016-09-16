import grails.util.Metadata

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.Resource

class EnvironmentService implements InitializingBean {

	def transactional = false

	String version
	String build

	GrailsApplication grailsApplication

	void afterPropertiesSet() {
		Resource resource = grailsApplication.mainContext.getResource(grailsApplication.config.tdsops.buildFile)
		if (resource) {
			build = resource.inputStream.text
		}
		version = Metadata.current['tdstm.application.version']
	}

	// gets the version and build data as a string
	String getVersionText() {
		"Version $version ($build)"
	}
}
