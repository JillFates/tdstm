package com.tdssrc.grails

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Room
import net.transitionmanager.service.DomainUpdateException
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.validation.Constraint
import org.hibernate.FlushMode
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.context.MessageSource
import org.springframework.util.Assert

@Slf4j(value='logger')
class GormUtil {

	/**
	 * Output GORM Domain constraints and update errors in human readable format
	 * @param Domain the domain instance that has errors
	 * @param String the separator to used between listings (default ' : ')
	 * @param String locale (optional)
	 * @return String the errors formatted in human readable format
	 */
	static String allErrorsString(domain, String separator = " : ", Locale locale = Locale.US) {
		MessageSource messageSource = ApplicationContextHolder.getBean('messageSource', MessageSource)
		StringBuilder text = new StringBuilder()
		domain?.errors?.allErrors?.each {
			text << separator << ' ' << messageSource.getMessage(it, locale)
		}
		text.toString()
	}

	/**
	 * Output GORM Domain constraints and update errors in human readable HTML Unordered List
	 * @param domain  the domain instance that has errors
	 * @param locale  the locale to use when resolving messages (optional)
	 * @return the errors formatted in human readable format
	 */
	static String errorsAsUL(domain, Locale locale = Locale.US) {
		StringBuilder text = new StringBuilder('<ul>')
		text.append(allErrorsString(domain, '<li>', locale))
		text.append('</ul>')
		return text.toString()
	}

	/**
	 * Convert a list into a comma delimited of type String to use inside sql statement.
	 */
	 static String asCommaDelimitedString(list) {
		WebUtil.listAsMultiValueString list
	}

	/**
	 * Convert a list into a quoted comma delimited of type String to use inside sql statement.
	 */
	static String asQuoteCommaDelimitedString(list) {
		list.collect { "'" + it + "'" }.join(',')
	}

	/**
	 * Generate a list of the domain property names that the have the specified constraint. If a
	 * value is passed then only those properties having the constraint value will be returned.
	 * @param clazz  the Domain class to find properties within
	 * @param constraintName - the constraint name (presently supports 'nullable' and 'blank')
	 * @param value - if passed then it will check the constraint value against that to further filter the list
	 * @return the property name(s) in the domain with/without blank/null constraint
	 * @usage getDomainPropertiesWithConstraint(Model, 'nullable', true) - returns all properties that are nullable
	 */
	static List<String> getDomainPropertiesWithConstraint(Class clazz, String constraintName, value = null) {

		List<String> propertyNames = []
		Map<String, ConstrainedProperty> constraints = clazz.constraints
		for (propertyName in constraints.keySet()) {
			def constraintValue = getConstraintValue(clazz, propertyName, constraintName) != null
			if (constraintValue) {
				if ((value == null && constraintValue != null) || (value != null && constraintValue == value)) {
					propertyNames << propertyName
				}
			}
		}

		return propertyNames
	}

	/**
	 * Validate if the version id of a domain is valid and hasn't been ticked by someone else while the user was editing the domain
	 * @param domainObj - the domain object to check the version on
	 * @param versionFromForm - the original value of the domain version when it was originally read
	 * @param label - the text to indicate the domain object in error message
	 * @throws RuntimeException if there no initialVersion value
	 * @throws DomainUpdateException if the version number was ticked since the initialVersion
	 */
	static void optimisticLockCheck(domainObj, Map params, String label) {
		Long version = NumberUtil.toLong(params.version)
		if (version == null) {
			println "domainVersionCheck failed on domain $domainObj for no version id parameter"
			throw new DomainUpdateException("The $label version was missing from request")
		}

		if (domainObj.version > version) {
			throw new DomainUpdateException("The $label was updated by someone while you were editing therefore your changes were not saved.")
		}
	}

	/**
	 * Set the value of a Domain object property if the value is not null/blank and different than existing value
	 * @param object - the object to set the property on
	 * @param propName - the String name of the property
	 * @param value - an object value
	 */
	static void overridePropertyValueIfSet(domainObj, String propName, value) {
		if (value && domainObj[propName] != value) {
			domainObj[propName] = value
		}
	}

	/**
	 * Merge/reattach domain object with the current Hibernate session.
	 *
	 * @param domainObject  the instance to be merged back into the session after detaching from the session
	 */
	static <T> T mergeWithSession(T domainObject) {
		(T) currentSession.merge(domainObject)
	}

	/**
	 * Flush/clear the Hibernate session to eliminate memory/performance issues.
	 * @param rowsProcessed - the count of how many rows have been proceeded
	 * @param flushAfterLimit - the limit at which the session is flushed (default 50)
	 * @return true if the limit was hit and the session was flushed
	 */
	static boolean flushAndClearSession(int rowsProcessed, int flushAfterLimit = 50) {
		if (rowsProcessed % flushAfterLimit == 0) {
			flushAndClearSession()
			return true
		}
		return false
	}

	/**
	 * Used to clear out the hibernate session of objects no longer needed to help performance. It will also merge the existing
	 * @param sessionFactory
	 * @return
	 */
	static flushAndClearSession(SessionFactory sessionFactory) {
		sessionFactory.currentSession.flush()
		sessionFactory.currentSession.clear()
		DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()
	}

	/**
	 * Flush the current Hibernate session.
	 */
	static void flushSession() {
		currentSession.flush()
	}

	/**
	 * Flush and clear the current Hibernate session.
	 */
	static void flushAndClearSession() {
		Session session = currentSession
		session.flush()
		session.clear()
	}

	static void setSessionFlushMode(FlushMode flushMode) {
		currentSession.flushMode = flushMode
	}

	static FlushMode getSessionFlushMode() {
		currentSession.flushMode
	}

	private static Session getCurrentSession() {
		Room.withSession { Session session -> session }
	}

	/**
	 * Frees up memory that is allocated in local threads when GORM is used in background processes where
	 * there is no HTTP Request object in the Session. This is a known problem documented in the blog post
	 * http://burtbeckwith.com/blog/?p=73.
	 */
	static void releaseLocalThreadMemory() {
		// TODO BB remove for 3.x
		DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()
	}

	/**
	 * Access the maxSize constraint value on a property of a Domain class
	 * @param clazz  the Domain class to access the property on
	 * @param propertyName - the name of the property to get the maxSize constraint of
	 * @return The value set in the maxSize constraint
	 */
	static Integer getConstraintMaxSize(Class clazz, String propertyName) {
		if (!grailsApplication.isDomainClass(clazz)) {
			throw new IllegalArgumentException("An non-domain class was provided for getMaxSize constraint")
		}

		Map<String, ConstrainedProperty> constraints = clazz.constraints
		ConstrainedProperty constrainedProperty = constraints[propertyName]
		if (!constrainedProperty) {
			throw new IllegalArgumentException("An invalid property name ($propertyName) was specified")
		}

		if (!constrainedProperty.getAppliedConstraint('maxSize')) {
			throw new IllegalArgumentException("Property $propertyName does not have the maxSize constraint")
		}

		return constrainedProperty.getMaxSize()
	}

	static getConstraintValue(Class clazz, String propertyName, String constraintName) {
		Map<String, ConstrainedProperty> constraints = getDomainClass(clazz).constrainedProperties
		Constraint constraint = constraints[propertyName].getAppliedConstraint(constraintName)

		// 'blank' is only supported for String properties and defaults to true, but there won't
		// be an actual constraint registered for a String property that doesn't specify a rule
		// in the constraints block (unlike 'nullable' which does add a nullable:false if nothing is specified).
		// For consistency a String property without an explicit rule for 'blank' will return true, not null
		if (!constraint && constraintName == 'blank' && isStringType(clazz, propertyName)) {
			return true
		}

		constraint?.parameter
	}

	static boolean isStringType(Class clazz, String propertyName) {
		String == GrailsClassUtils.getPropertyType(clazz, propertyName)
	}

	static Collection<GrailsDomainClassProperty> updatablePersistentProperties(Class clazz, Collection<String> notToUpdateNames) {
		getDomainClass(clazz).persistentProperties.findAll { it.isPersistent() && !notToUpdateNames.contains(it.name) }
	}

	static void copyUnsetValues(to, from, Collection<String> notToUpdateNames = []) {
		Assert.isTrue(from.getClass() == to.getClass() || (from instanceof Map),
				'Can only copy between instances of the same type unless from is a Map')
		for (GrailsDomainClassProperty property in updatablePersistentProperties(to.getClass(), notToUpdateNames)) {
			String name = property.name
			if (from[name] && !to[name]) {
				to[name] = from[name]
			}
		}
	}

	static boolean hasStringId(Class clazz) {
		getDomainClass(clazz).identifier.type == String
	}

	static GrailsDomainClass getDomainClass(Class clazz) {
		// TODO Assert.isTrue(app.isDomainClass(clazz))
		(GrailsDomainClass) grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, clazz.name)
	}

	private static GrailsApplication getGrailsApplication() {
		ApplicationContextHolder.grailsApplication
	}
}
