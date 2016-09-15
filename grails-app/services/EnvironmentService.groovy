import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import grails.util.Holders
import java.io.File

class EnvironmentService implements InitializingBean {

	def servletContext
	def grailsApplication
	def configurationHolder

	String version
	String build
	def transactional = false

	// The following vars are initialized in afterPropertiesSet after IoC
	def ctx
	def sessionFactory

	// called during application intialization to read the build and version data
	public void afterPropertiesSet () throws Exception {
		ctx = Holders.grailsApplication.mainContext
		sessionFactory = ctx.sessionFactory

		def resource = ctx.getResource(grailsApplication.config.tdsops.buildFile)
		if (resource) {
			File buildFile = resource.getFile()
			FileInputStream is = new FileInputStream(buildFile)
			build = ""
			while (is.available() > 0)
				build += (char) is.read()
		}
		version = grailsApplication.metadata["tdstm.application.version"]
	}

	// gets the version and build data as a string
	def getVersionText () {
		return "Version ${version} (${build})"
	}
}
