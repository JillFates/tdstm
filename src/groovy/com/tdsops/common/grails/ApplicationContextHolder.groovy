package com.tdsops.common.grails
 
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import javax.servlet.ServletContext
 
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * This is an Application Context Holder that can be used by the application code to gain access to
 * various components within the application at runtime where IoC is not possible.
 *
 * This was taken directly from a Burt Beckwith blog posting:
 * @see http://burtbeckwith.com/blog/?p=1017
 */ 
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {
 
	private ApplicationContext ctx
 
	private static final Map<String, Object> TEST_BEANS = [:]
 
	void setApplicationContext(ApplicationContext applicationContext) {
		 ctx = applicationContext
	}
 
	static ApplicationContext getApplicationContext() {
		getInstance().ctx
	}
 
	static Object getBean(String name) {
		TEST_BEANS[name] ?: getApplicationContext().getBean(name)
	}
 
	static GrailsApplication getGrailsApplication() {
		getBean('grailsApplication')
	}
 
	static ConfigObject getConfig() {
		getGrailsApplication().config
	}
  
	static GrailsPluginManager getPluginManager() {
		getBean('pluginManager')
	}
 
	// For testing
	static void registerTestBean(String name, bean) {
		TEST_BEANS[name] = bean
	}
 
	// For testing
	static void unregisterTestBeans() {
		TEST_BEANS.clear()
	}
}