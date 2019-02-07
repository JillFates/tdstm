package com.tdsops.common.grails

import grails.util.Holders
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContextAware
import groovy.transform.CompileStatic

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * An ApplicationContext holder to gain access to various components within
 * the application at runtime where IoC is not possible.
 *
 * This was taken directly from a Burt Beckwith blog posting:
 * @see http://burtbeckwith.com/blog/?p=1017
 */
@CompileStatic
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {

	ApplicationContext applicationContext

	static ApplicationContext getApplicationContext() {
		getInstance().@applicationContext
	}

	/**
	 * Return a bean from the container given its name and/or class.
	 * Bear in mind that some beans, such as ApplicationTagLib, are not available in the application context,
	 * but in the main context.
	 *
	 * @param name
	 * @param type
	 * @return
	 */
	static <T> T getBean(String name, Class<T> type = null) {
		T bean
		try {
			bean = (T) (type ? getApplicationContext().getBean(name, type) : getApplicationContext().getBean(name))
		} catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
			if (type) {
				bean = (T) Holders.grailsApplication.mainContext.getBean(type)
			} else {
				throw noSuchBeanDefinitionException
			}
		}
		return bean
	}

 	static Object getService(String name) {
 		getBean(name)
 	}

	static GrailsApplication getGrailsApplication() {
		getBean('grailsApplication', GrailsApplication)
	}

	static ConfigObject getConfig() {
		getGrailsApplication().config
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