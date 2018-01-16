package com.tdssrc.grails

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Room
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
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
import org.opensaml.xml.signature.P
import org.springframework.context.MessageSource
import org.springframework.util.Assert

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.orm.hibernate.cfg.CompositeIdentity
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.codehaus.groovy.grails.orm.hibernate.cfg.Mapping
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.validation.Constraint

@Slf4j(value='logger')
public class GormUtil {

// TODO : JPM 1/2017 : PersonMerge -- enum Operator was deleted by Burt
    // Used to control how some functions will perform comparisons with multiple where criteria
	enum Operator { AND, OR }

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

	/*
// TODO : JPM 1/2017 : Validate getDomainPropertiesWithConstraint method that Burt replaced
	public static List<String> getDomainPropertiesWithConstraint(def domain, def constraintName, def value=null ) {
		def fields = []
		domain.constraints.each() { propName, props ->
			def constraint = props.getAppliedConstraint( constraintName )?.getAt(constraintName)
			switch (constraintName) {
				case 'blank':
					// By default property blank is false except String prop so if false is requested as value
					// and property is not string so considering as 'blank : false'
					def type = GrailsClassUtils.getPropertyType(domain, propName)?.getName()
					if (type == 'java.lang.String' && constraint in [null , true])
						constraint = true
					else
						constraint = false
					break

				case ['nullable', 'range']:
					// println "propName=$propName, constraintName=$constraintName, constraint=$constraint, value=$value"
					break

				default:
					log.error "Called getDomainPropertiesWithConstraint() with unsupported constraint $constraintName"
			}

			if ( (value == null && constraint != null) || (value != null && constraint == value) )
				fields <<  propName
		}
		return fields
	}
	*/

	/**
	 * Used to access all constraints for a single property of a domain object
	 * @param domainClass - the domain class to get the constraint from
	 * @param property - the name of the property to be accessed
	 * @return the map of constraints
	 */
	public static Map getConstrainedProperties(Class domainClass) {
		if (! isDomainClass(domainClass)) {
			throw new RuntimeException("A non-domain class parameter was provided")
		}

		String domainName = domainClass.getName()
		def domainObj = ApplicationContextHolder.getArtefact('Domain', domainName)
		if (! domainObj) {
			throw RuntimeException('Unable to find artefact for domain ' + domainName)
		}

		return domainObj.getConstrainedProperties()
	}

	/**
	 * Used to access all constraints for a single property of a domain object
	 * @param domainClass - the domain class to get the constraint from
	 * @param propertyName - the name of the property to be accessed
	 * @return the map of constraints
	 */
	public static ConstrainedProperty getConstrainedProperty(Class domainClass, String propertyName) {
		Map props = getConstrainedProperties(domainClass)

		if (! props[propertyName]) {
			throw new RuntimeException("An invalid property name ($propertyName) was specified")
		}

		return props[propertyName]
	}

	/**
	 * Used to access individual constraints from a domain object
	 * @param domainClass - the domain class to get the constraint from
	 * @param property - the name of the property to be accessed
	 * @param constraintName - the individual constraint to access
	 * @return the constraint value
	 */
	public static getConstraint(Class domainClass, String property, String constraintName) {
		ConstrainedProperty constraint =  getConstrainedProperty(domainClass, property)
		return constraint.getAppliedConstraint(constraintName)
	}


	/**
	 * Used to validate if the version id of a domain is valid and hasn't been ticked by someone else while the user was editing the domain
	 * @param domainObj - the domain object to check the version on
	 * @param versionFromForm - the original value of the domain version when it was originally read
	 * @param label - the text to indicate the domain object in error message
	 * @throws RuntimeException if there no initialVersion value
	 * @throws DomainUpdateException if the version number was ticked since the initialVersion
	 */
	public static void optimisticLockCheck(Object domainObj, Object params, String label) {
		def version = NumberUtil.toLong(params.version)
		if (version == null) {
			throw new DomainUpdateException("The $label version was missing from request")
		} else {
			if (domainObj.version > version) {
				throw new DomainUpdateException("The $label was updated by someone while you were editting therefore your changes were not saved.")
			}
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
	 * Merge/reattach domain object with the current Hibernate session
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
	 * @return The value set in the maxSize constraint or null of the property does not have the constraint
	 */
	static Long getConstraintMaxSize(Class domainClass, String propertyName) {
		Constraint constraint = GormUtil.getConstraint(domainClass, propertyName, 'maxSize')
		return constraint?.getMaxSize()
	}

	/**
	 * Used to get the Uniqueness Group or list of properties that combined make up the uniques of
	 * a domain class for the given propertyName.
	 * @param domainClass - the Domain class to access the property on
	 * @param propertyName - the name of the property to get the maxSize constraint of
	 * @return a list of the property names
	 */
	static List getConstraintUniqueProperties(Class domainClass, String propertyName) {
		Constraint cp = GormUtil.getConstraint(domainClass, propertyName, 'unique')
		return cp.getUniquenessGroup()
	}

	/**
	 * Used to retrieve the value specified for a particular property constraint
	 * @param clazz - the Class object of the domain being inspected
	 * @param propertyName - the property name to inspect
	 * @param constraintName - the name of the constraint being inspected
	 * @return the value be it an Integer, Closure, Range, etc base on the constraint type
	 */
	static Object getConstraintValue(Class clazz, String propertyName, String constraintName) {
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

	/**
	 * Used to determine if a particular domain property is a String
	 * @param clazz - the Class object of the domain being inspected
	 * @param propertyName - the property name to inspect
	 * @return true if the property is a String otherwise false
	 */
	static boolean isStringType(Class clazz, String propertyName) {
		String == GrailsClassUtils.getPropertyType(clazz, propertyName)
	}

	/**
	 * Returns a Collection of GrailsDomainClassProperty properties for a domain class that are
	 * persistable and updatable.
	 * @param clazz - the Class object of the domain being inspected
	 * @param notToUpdateNames - a list of property names that should be excluded from the result
	 * @return a list of GrailsDomainClassProperty that are updatable
	 */
	static Collection<GrailsDomainClassProperty> updatablePersistentProperties(Class clazz, Collection<String> notToUpdateNames) {
		getDomainClass(clazz).persistentProperties.findAll { it.isPersistent() && !notToUpdateNames.contains(it.name) }
	}

// TODO : JPM copyUnsetValues - reverse the from/to parameters

	/**
	 * Used to clone persistent properties from one domain object to another with ability to
	 * exclude a list of properties that should be ignored.
	 * @param from - domain object to copy property values from
	 * @param to - the domain object to copy the properties to
	 * @param notToUpdateNames - an optional list of property names that should not be copied
	 */
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

	/**
	 * Used to validate that a property exists on a Domain class
	 * @param domainObject - the domain to inspect
	 * @param propertyName - the name of the property to validate
	 * @return true if the property exists otherwise false
	 */
	static boolean isDomainProperty(Object domainObject, String propertyName) {
		boolean valid=false
		if (propertyName && domainObject) {
			valid = domainObject.metaClass.hasProperty(domainObject.getClass(), propertyName)
		}
		return valid
	}

	/**
	 * Used to determine if an object is a Domain class
	 * @param clazz - the class to evaluate to determine if it is a Domain class
	 * @return true if the object is a Domain class otherwise false
	 */
	static boolean isDomainClass(Class domainClass) {
		def grailsApp = com.tdsops.common.grails.ApplicationContextHolder.getGrailsApplication()
		return grailsApp.isDomainClass( domainClass )
//		return grailsApp.isDomainClass( domainObj.getClass() )
		//org.codehaus.groovy.grails.commons.isDomainClass(domainClass)
	}

	/**
	 * Used to determine if an object is a Domain class
	 * @param domainInstance - the object to evaluate to determine if it is a Domain class
	 * @return true if the object is a Domain class otherwise false
	 */
	static boolean isDomainClass(Object domainInstance) {
		return isDomainClass(domainInstance.getClass())
	}

	/**
	 * Used to get the list of persistent properties for a given domain class
	 * @param domain - the Domain class to get the properties for
	 * @return A list of the properties
	 */
	static List<String> persistentProperties(Object domain) {
		if (!isDomainClass(domain)) {
			throw new RuntimeException('persistentProperties called with non-domain object')
		}

		def d = new DefaultGrailsDomainClass(domain.class)

		return d.persistentProperties.name
	}

	/**
	 * Used to get the list of persistent properties for a given domain class
	 * @param domainClass - the Domain class to get the properties for
	 * @param propertyName - the name of a property to retrieve
	 * @return The GrailsDomainClassProperty object
	 */
	static GrailsDomainClassProperty getDomainProperty(Class domainClass, String propertyName) {
		if (!isDomainClass(domainClass)) {
			println "getDomainProperty() domainClass=${domainClass.getName()}"
			throw new RuntimeException('Called with non-domain class parameter')
		}

		def d = new DefaultGrailsDomainClass(domainClass)
		GrailsDomainClassProperty cp = d.getPropertyByName(propertyName)

		return cp
	}

	/**
	 * Retrieve a list of domain properties. If a list of property names is given, only
	 * those properties will be included. If a list of properties to be skipped is provided,
	 * those properties will be excluded.
	 * If
	 * @param domainClass
	 * @param properties
	 * @return
	 */
	static List<GrailsDomainClassProperty> getDomainProperties(Class domainClass, List<String> properties = null, List<String> skipProperties = null) {
		List<GrailsDomainClassProperty> domainProperties = []
		boolean allProperties = false
		DefaultGrailsDomainClass dfdc = new DefaultGrailsDomainClass(domainClass)
		if (properties) {
			for (String property in properties) {
				GrailsDomainClassProperty domainProperty = dfdc.getPersistentProperty(property)
				if (domainProperty) {
					domainProperties << domainProperty
				} else {
					/* if at least one property wasn't found, assume there's not enough information and all the
					properties should be returned. */
					allProperties = true
					break
				}
			}
		} else {
			allProperties = true
		}

		if (allProperties) {
			domainProperties = dfdc.getPersistentProperties()
		}

		if (skipProperties) {
			domainProperties = domainProperties.findAll { !(it.name in skipProperties) }
		}
		return domainProperties

	}

	/**
	 * Used to get the list of persistent properties for a given domain class
	 * @param domainInst - the Domain instance to get the properties for
	 * @param propertyName - the name of a property to retrieve
	 * @return The GrailsDomainClassProperty object
	 */
	static GrailsDomainClassProperty getDomainProperty(Object domainInst, String propertyName) {
		return getDomainProperty(domainInst.getClass(), propertyName)
	}

	/**
	 * Used to retrieve the Domain Binder Mapping of a domain which can then be interrogated
	 * @param domainClass - the domain to integrate
	 * @return true if it has a composite primary key
	 */
	static Mapping getDomainBinderMapping(Class domainClass) {
		if (!isDomainClass(domainClass)) {
			throw new RuntimeException('a Grails domain class parameter is required')
		}
		Mapping binderMapping = new GrailsDomainBinder().getMapping(domainClass)
		//if (!binderMapping) {
		//	throw new RuntimeException('Failed to load Grails domain mapping for ' + getDomainNameForClass(domainClass))
		//}
		return binderMapping
	}

	/**
	 * Used to determine if a Domain class has a Composite Key
	 * @param domainClass - the domain to integrate
	 * @return true if it has a composite primary key
	 */
	static boolean hasCompositeKey(Class domainClass) {
		Mapping mapping = getDomainBinderMapping(domainClass)
		boolean hasCK = (mapping && (mapping.identity instanceof CompositeIdentity))
		return hasCK
	}

	/**
	 * Used to get the list of properties that make up a composite key on a domain class
	 * @param domainClass - the domain to integrate
	 * @return a list of the property names
	 */
	static List<String> getCompositeKeyProperties(Class domainClass) {
		List props = []
		if (hasCompositeKey(domainClass)) {
			def binder = new GrailsDomainBinder().getMapping(domainClass)
			props = binder.identity.propertyNames as List
		}
		return props
	}

	/**
	 * Used to determine if a property for a given domain class is part of a composite key
	 * @param domainClass - the Domain class to iterrogate
	 * @param propertyName - the name of a property to check as being a composite key element
	 * @return true if property is part of a composite key otherwise false
	 */
	static boolean isCompositeProperty(Class domainClass, String propertyName) {
		GrailsDomainClassProperty property = getDomainProperty(domainClass, propertyName)
		return isCompositeProperty(domainClass, property)
	}

	/**
	 * Used to determine if a property for a given domain class is part of a composite key
	 * @param domainClass - the Domain class to iterrogate
	 * @param property - the domain property to check as being a composite key element
	 * @return true if property is part of a composite key otherwise false
	 */
	static boolean isCompositeProperty(Class domainClass, GrailsDomainClassProperty property) {
		Mapping mapping = getDomainBinderMapping(domainClass)
		return new GrailsDomainBinder().isCompositeIdProperty(mapping, property)
	}

	/**
	 * Used to determine if a single key property can be replace or if the domain object would need to be cloned. This is helpful when merging people
	 * or users for instance. It will verify if the propert(y|ies) is part of the composite key.
	 * @param domainClass - the class to check
	 * @param propertyName - the name of a property to examine
	 */
	static boolean canDomainPropertyBeReplaced(Class domainClass, String propertyName) {
		return canDomainPropertiesBeReplaced(domainClass, [propertyName])
	}

	/**
	 * Used to determine if a list of key property can be replace or if the domain object would need to be cloned. This is helpful when merging people
	 * or users for instance. It will verify if the propert(y|ies) is part of the composite key.
	 * @param domainClass - the class to check
	 * @param propertyNames - a list of the
	 */
	static boolean canDomainPropertiesBeReplaced(Class domainClass, List propertyNames) {
		boolean replaceable=true
		for (propName in propertyNames) {
			if (replaceable) {
				GrailsDomainClassProperty dp = getDomainProperty(domainClass, propName)
				if (dp.isIdentity()) {
					replaceable=false
				} else {
					if (isCompositeProperty(domainClass, dp)) {
						replaceable=false
					}
					if (replaceable) {
						// See it the property has a unique constraint on it that will require merging vs replacement
						Constraint cp = getConstraint(domainClass, propName, 'unique')
						if (cp) {
							replaceable = false
						} else {
							// Try looking at the mapping
							def mapping = getDomainBinderMapping(domainClass)
							if (mapping) {
								//println "** property $propName"
								// println "*** Mapping columns ${mapping.columns}"
								def propConfig = mapping.getPropertyConfig(propName)
								if (propConfig && propConfig.isUnique()) {
									replaceable = false
								}
							}
						}
					}
				}
			}
		}
		return replaceable
	}

	/**
	 * Used to clone a domain object to a new object subsituting key properties and optionally deleting the original
	 * @param originalDomain - the domain to clone properties from
	 * @param replaceKeys - a Map of property name(s) and the associated values to set, if value is null then it is not set
	 * @param deleteOriginal - a flag if the original domain should be deleted (default:false)
	 * @return the cloned object
	 */
	static Object cloneDomain(Object originalDomain, Map replaceKeys = [:], boolean deleteOriginal=false) {
		Object newDomain = domainClone(originalDomain, replaceKeys)

		newDomain.save(flush:true)

		if (deleteOriginal) {
			originalDomain.delete(flush:true)
		}

		return newDomain
	}


	/**
	 * Used to clone a domain object to a new object subsituting key properties,
	 * note that the new object is NOT persisted in the Database. The method will copy all properties excluding
	 * any Collection properties and those in the replaceKeys parameter. The replaceKeys allows swapping out certain
	 * properties with specified values instead of cloning from the origin domain.
	 * @param originalDomain - the domain to clone properties from
	 * @param replaceKeys - a Map of property name(s) and the associated values to set, if value is null then it is not set
	 * @return the cloned object
	 */
	static Object domainClone(Object originDomain,  Map replaceKeys = [:]) {
		logger.debug("** Clonning: {} *****", originDomain.getClass())
		if (!isDomainClass(originDomain.getClass())) {
			throw new RuntimeException('A non-Grails Domain object was received')
		}

		// Create a new domain object
		Object newDomain = originDomain.getClass().getConstructor().newInstance()

		// Assign the key values to the domain
		replaceKeys.each { key, value ->
			if (value != null) {
				newDomain[key] = value
			}
		}

		// Get the list of persistent properties from the domain that are not the keys being overridden
		List keys = replaceKeys.keySet() as List
		List props = persistentProperties(originDomain)

		// Strip off the replacement keys from the list and then copy over the values to the new domain
		props = props - keys

		// Iterate over all of the properties and assign to the new domain object
		for (p in props) {
			// Skip over properties that are of Collection type (TM-6879)
			if (originDomain[p] instanceof Collection) {
				continue
			}

			newDomain[p] = originDomain[p]
		}

		return newDomain
	}

	/**
	 * Used to find a domain object matching one to be cloned where certain key(s) properties are overridden
	 * @param domainObj - the object to clone
	 * @param replaceKeys - a map of keys and the values to override in the lookup
	 * @return the object if found otherwise null
	 */
	static Object findCloneDomainTarget(Object domainObj, Map replaceKeys) {
		if (!isDomainClass(domainObj.getClass())) {
			throw new RuntimeException('a Grails domain object parameter is required')
		}

		List props = GormUtil.getCompositeKeyProperties(domainObj.getClass())
		List keys = replaceKeys.keySet() as List

		// Remove the override keys from the property list
		props = props - keys

		String tableName = domainObj.getClass().getName()

		List params = []
		StringBuilder hql = new StringBuilder("from $tableName x where ")
		boolean first=true
		int paramIndex=0
		props.each { propName ->
			params << domainObj[propName]
			if (first) {
				first=false
			} else {
				hql.append(' AND ')
			}
			hql.append("x.$propName=?")
		}

		keys.each { keyName ->
			params << replaceKeys[keyName]
			if (first) {
				first = false
			} else {
				hql.append(' AND ')
			}
			hql.append("x.$keyName=?")
		}

		String hqlStr = hql.toString()
		logger.debug("findCloneDomainTarget() query={}, params={}", hql, params)
		Object record = domainObj.getClass().find(hqlStr, params)

		return record

	}

	/**
	 * Used to clone a domain object matching one to be cloned where certain key(s) properties are overridden but
	 * only if it does not already exist. Optionally it will delete the original record.
	 * @param domainObj - the object to clone
	 * @param replaceKeys - a map of keys and the values to override in the lookup
	 * @return the object if it was cloned otherwise null
	 */
	static Object cloneDomainIfNotExist(Object domain, Map keyValues, boolean deleteOriginal=false) {
		Object clone
		if (! findCloneDomainTarget(domain, keyValues)) {
			// println "cloneDomainIfNotExist() cloning domain ($domain) and replacing ($keyValues)"
			clone = cloneDomain(domain, keyValues, deleteOriginal)
			// println "cloneDomainIfNotExist() resulted in $clone"
		} else {
			if (deleteOriginal) {
				// println "cloneDomainIfNotExist() deleting duplicate domain $domain"
				domain.delete()
			}
		}
		return clone
	}

	/**
	 * Used to retrieve a list of records from a domain by one or more key values
	 * @param domainClass - the class of the domain to search
	 * @param propertiesMap - a map of the property names and the associated value/objects
	 * @return list of the domain objects found
	 */
	static List<Object> findAllByProperties(Class domainClass, Map propertiesMap, Operator operator = Operator.AND) {
		String domainName = domainClass.getName()
		boolean first=true
		StringBuilder hql = new StringBuilder('From ' + domainName + ' x Where ')
		List params = []
		int paramIndex=0
		propertiesMap.each { propName, value ->
			if (first) {
				first=false
			} else {
				hql.append(" ${operator.toString()} ")
			}
			hql.append("x.$propName=?")
			params << value
		}
		String hqlStr = hql.toString()
		//logger.debug("findAllByProperties() query={}, params={}", hql, params)

		List rows = domainClass.findAll(hqlStr, params)
		return rows
	}

	/**
	 * Used to get just the Name of the domain stripping off the package name if it exists
	 * @param domainClass - the class of the domain interested in
	 * @return a String representing the name of the Domain
	 */
	static String getDomainNameForClass(Class domain) {
		String name = domain.getName()
		name = name.tokenize('.').last()
		return name
	}

	/**
	 * Used to delete or null out references of an object that is going to be deleted. This relies on the Domain having the
	 * domainReferences being properly defined in the domain class.
	 * @param domainObject - the object to deference
	 * @param deleteObject - a flag to indicate if the object should be deleted when finished dereferencing (default: false)
	 * @return A list consisting of reference deletedCount and nulledCount
	 */
	static List deleteOrNullDomainReferences(Object domainObject, boolean deleteObject=false) {
		int deletedCount=0
		int nulledCount=0

		domainObject.domainReferences.each { attribs ->

			Class domainClass = attribs.domain
			String domainName = getDomainNameForClass(attribs.domain)

			// Build & execute HQL statement(s) to delete or null out references for each Domain
			if (attribs.onDelete == 'delete') {
				// Delete the reference
				StringBuilder hql = new StringBuilder("DELETE FROM $domainName x WHERE ")
				boolean first=true
				List params=[]
				int paramIndex=0
				attribs.properties.each { propName ->
					if (first) {
						first=false
					} else {
						hql.append(' OR ')
					}
					hql.append("x.$propName=?")
					params << (attribs.transform ? attribs.transform.call(domainObject) : domainObject)
				}
				String hqlStr = hql.toString()
				log.debug "deleteOrNullUserLoginReferences() Delete statement: $hqlStr, params: $params"
				deletedCount += attribs.domain.executeUpdate(hqlStr, params)
			} else {
				// Null out the reference(s)
				int paramIndex=0
				attribs.properties.each { propName ->
					String hql = "UPDATE $domainName SET $propName=NULL WHERE $propName=:ref"
					Map params = [ref:(attribs.transform ? attribs.transform.call(domainObject) : domainObject)]
					log.debug "deleteOrNullUserLoginReferences() Delete statement: $hql, params: $params"
					nulledCount += attribs.domain.executeUpdate(hql, params)
				}
			}
		}

		if (deleteObject) {
			domainObject.delete(flush:true)
		}

		return [deletedCount, nulledCount]
	}

	/**
	 * Used to determine if a particular Domain property can be merged. Domain properties that are
	 * associated and is the Owning Side require merging in a different manor.
	 * @param domain the class of the domain to check
	 * @param propName the name of the property to check
	 * @return true if the property can be merged
	 */
	static boolean canMergeDomainProperty(Class domainClass, String propName) {
		GrailsDomainClassProperty domainProp =  getDomainProperty(domainClass, propName)
		boolean isAssoc = domainProp.isAssociation()
		boolean isOwning = domainProp.isOwningSide()
		boolean isOne = domainProp.isOneToOne()
		return ! (isAssoc && isOwning && isOne)
	}

	/**
	 * Used to merge all references from one domain object over to another. This relies on the Domain having the domainReferences variable
	 * properly defined in the domain class.
	 * @param fromDomain - the domain that will be replaced
	 * @param toDomain - the domain that will remain and have all outstanding references
	 * @param deleteFrom - flag that if true will delete the fromDomain when finished (default: false)
	 */
	static void mergeDomainReferences(Object fromDomain, Object toDomain, boolean deleteFrom=false) {

		Class domainClass = fromDomain.getClass()
		String domainName = domainClass.getName()

		if (! domainClass.domainReferences) {
			throw new RuntimeException("Domain $domainName is missing required static Map domainReferences")
		}

		domainClass.domainReferences.each { attribs ->
			// See if there is a transformation that needs to be performed on the key (e.g. convert to String)
			def toRef, fromRef
			if (attribs.transform) {
				fromRef = attribs.transform.call(fromDomain)
				toRef = attribs.transform.call(toDomain)
			} else {
				fromRef = fromDomain
				toRef = toDomain
			}

			// logger.debug "mergeDomainReferences() fromRef=${fromRef.getClass().getName()} $fromRef ::: ${toRef.getClass().getName()} $toRef"

			// Determine if we can replace or need to clone the domain object by seeing if the object can be replaced
			if (GormUtil.canDomainPropertiesBeReplaced(attribs.domain, attribs.properties)) {
				String updateTableName = getDomainNameForClass(attribs.domain)
				// Map params = [to: toRef.id, from: fromRef.id]
				Map params = [to: toRef, from: fromRef]
				attribs.domain.withSession { session ->
					attribs.properties.each { propName ->
						boolean canMerge = canMergeDomainProperty(attribs.domain, propName)
						if (!canMerge) {
							log.debug "***** mergeDomainReferences() skipping the merge for $domainName reference ${attribs.domain}.$propName"
						} else {
							// String hql = "update $updateTableName x set x.${propName}.id=:to WHERE x.${propName}.id=:from"
							String hql = "update $updateTableName x set x.${propName}=:to WHERE x.${propName}=:from"
							// log.debug "mergeDomainReferences() hql=$hql, params=$params"
							int changes = attribs.domain.executeUpdate(hql, params)
							//log.debug "mergeDomainReferences() reassigned $changes $updateTableName references"
						}
					}
					session.flush()
				}
			} else {
				// Clone to domain with the new reference properties and delete the from domain when finished
				Map findMap = [:]
				Map refValuesToReplace = [:]
				attribs.properties.each { propertyName ->
					findMap.put(propertyName, fromRef)
					refValuesToReplace.put(propertyName, toRef)
				}
				List rows = GormUtil.findAllByProperties(attribs.domain, findMap, GormUtil.Operator.OR)
				int rowCount = rows.size()
				if (rowCount) {
					log.info "mergeDomainReferences() merging ${rowCount} $domainName row(s)"
					rows.each { row ->
						// println "mergeDomainReferences() calling cloneDomainIfNotExist for $row"
						GormUtil.cloneDomainIfNotExist(row, refValuesToReplace, true)
					}
				}
			}
		}

		// After replacing references we can look to delete the original domain
		if (deleteFrom) {
			logger.debug("Deleting {} {}", fromDomain.getClass().getName(), fromDomain)
			domainClass.executeUpdate("delete $domainName x where x.id=:id", [id:fromDomain.id])
			//fromDomain.delete(flush:true)
		}
	}

	/**
	 * Returns the data type for a given property on a Domain class
	 * @param domainObject - the domain to inspect
	 * @param property - the name of the property
	 * @return the class name of the property as a string
	 */
	static Class getDomainPropertyType(Object domainObject, String property) {
		return getDomainPropertyType(domainObject.getClass(), property)
	}

	/**
	 * Returns the data type for a given property on domain class.
	 *
	 * @param clazz : the class to inspect.
	 * @param property : the property to look up.
	 *
	 * @return the data type, null if the property doesn't exist.
	 */
	static Class getDomainPropertyType(Class clazz, String property) {
		return GrailsClassUtils.getPropertyType(clazz, property)
	}

	/**
	 * Find an instance of the given type using the id provided for the
	 * project specified.
	 *
	 * @param project
	 * @param type
	 * @param id
	 * @param throwException
	 * @return
	 */
	static <T> T findInProject(Project project, Class<T> type, id, boolean throwException = false) {
		T instance = null
		String errorMsg
		// Project, type and id are mandatory
		if (type && id && project) {
			// Check if the class is a domain class.
			if (isDomainClass(type)) {
				try{
					// fetch the instance
					instance = type.get(id)
					if (!instance) {
						errorMsg = "The domain object with type $type couldn't be found using the id $id"
					}
				// Most likely and invalid type was given. Using a generic Exception to not tie it to a particular Hibernate implementation.
				} catch(Exception e) {
					errorMsg = "The domain object with type $type couldn't be found using the id $id"
				}

				// Make sure the domain class has a 'project' field.
				if (instance && isDomainProperty(instance, 'project')) {
					// Validate the projects' id match.
					if (project.id != instance.project.id) {
						errorMsg = "The domain object with type $type and id $id doesn't belong to project ${project.name}."
					}
				}
			} else {
				errorMsg = "$type is not a domain class."
			}
		} else {
			errorMsg = "findInProject requires a project, a domain class and an id."
		}

		if (errorMsg) {
			logger.error(errorMsg)
			if (throwException) {
				throw new EmptyResultException(errorMsg)
			} else {
				instance = null
			}
		}
		return instance
	}

	/**
	 * Return a map representation for the given domain object
	 * @param domainObject
	 * @param properties - you can narrow down the list of properties to be included by providing a list with their names.
	 * @param skipProperties - you can exclude certain properties by passing their names in this list.
	 * @return
	 */
	static Map domainObjectToMap(domainObject, List<String> properties = null, List<String> skipProperties = null, boolean navigateReferences = true) {

		if (!domainObject) {
			return null
		}

		List<String> minimalProperties = ["id", "name"]
		Map domainMap = [:]

		Class domainClass = domainObject.class
		if (isDomainClass(domainClass)) {
			// Get all the domain properties.
			List<GrailsDomainClassProperty> domainProperties = getDomainProperties(domainClass, properties, skipProperties)

			// Iterate over all the domain properties
			for (GrailsDomainClassProperty property : domainProperties) {
				// if the property is an enum, copy its .name()
				if (property.type.isEnum()) {
					if (domainObject[property.name]) {
						domainMap[property.name] = domainObject[property.name].name()
					}
					// if the property is a reference, call this method recursively with a predefined list of fields..
				} else if (isDomainClass(property.type)) {
					if (navigateReferences) {
						domainMap[property.name] = domainObjectToMap(domainObject[property.name], minimalProperties, null, false)
					}

				} else if (Collection.isAssignableFrom(property.type)){
					if (navigateReferences) {
						List<Map> listReferences = []
						for (element in domainObject[property.name]) {
							listReferences << domainObjectToMap(element, minimalProperties, null, false)
						}
					}
				} else {
					// If it's a regular property, just copy its value.
					domainMap[property.name] = domainObject[property.name]
				}
			}
		}

		return domainMap
	}
}