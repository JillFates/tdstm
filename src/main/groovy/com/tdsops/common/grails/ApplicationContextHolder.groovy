package com.tdsops.common.grails

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.GrailsPluginManager
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
/**
 * An ApplicationContext holder to gain access to various components within
 * the application at runtime where IoC is not possible.
 *
 * This was taken directly from a Burt Beckwith blog posting:
 * http://burtbeckwith.com/blog/?p=1017
 */
@CompileStatic
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {

	ApplicationContext applicationContext

	static ApplicationContext getApplicationContext() {
		getInstance().@applicationContext
	}

	static <T> T getBean(String name, Class<T> type = null) {
		(T) (type ? getApplicationContext().getBean(name, type) : getApplicationContext().getBean(name))
	}

 	static Object getService(String name) {
 		getBean(name)
 	}

	static GrailsApplication getGrailsApplication() {
		getBean('grailsApplication', GrailsApplication)
	}

	static ConfigObject getConfig() {
		(ConfigObject)getGrailsApplication().config
	}

	/**
	 * Used to retrieve Grails Application artefacts (e.g. Grails domain object)
	 * @param type - the type of artefact (e.g. 'Domain')
	 * @param className - the name of the class
	 * @return a GrailsClass for the specified artefact
	 * @usage ApplicationContextHolder.getArtefact('Domain', domainClassName)
	 */
	static GrailsClass getArtefact(String type, String className) {
		getGrailsApplication().getArtefact(type, className)
	}

	/**
	 * used to access classes by name
	 * @param name - the name of the class to lookup
	 * @example getClassForName("library.Book"); to lookup a domain class
	 */
	static getClassForName(String name) {
		getGrailsApplication().getClassForName(name)
  	}

	static GrailsPluginManager getPluginManager() {
		getBean('pluginManager', GrailsPluginManager)
	}

 /*
	// For testing
	static void registerTestBean(String name, bean) {
		TEST_BEANS[name] = bean
	}

	// For testing
	static void unregisterTestBeans() {
		TEST_BEANS.clear()
	}
*/
}
