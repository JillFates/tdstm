/*
 * A helper class for services
 */
class ServiceHelperService {

	def grailsApplication

	def getService(String serviceName) {
		String beanName = serviceName + 'Service'
		def bean = grailsApplication.mainContext.getBean(beanName)
		if (! bean) {
			throw new RuntimeException("Service $beanName was not found")
		}
		return bean
	}

	Map getApplicationConfig() {
		return grailsApplication.config
	}

}
