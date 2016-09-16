package com.tdsops.common.grails

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import groovy.transform.CompileStatic

import org.codehaus.groovy.grails.commons.GrailsApplication
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
		getGrailsApplication().config
	}

	static GrailsPluginManager getPluginManager() {
		getBean('pluginManager', GrailsPluginManager)
	}
}
