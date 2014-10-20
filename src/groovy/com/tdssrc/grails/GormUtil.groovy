package com.tdssrc.grails;

import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import com.tdsops.common.grails.ApplicationContextHolder

import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod  
 
public class GormUtil {

    private static BindDynamicMethod bindDynamicMethod = new BindDynamicMethod()  

    /** 
     * make the controller bindData method statically available, e.g. for service layer use 
     * implemented as closure to allow static import emulating controller layer bindData usage 1:1 
	 *
 	 * Used to bind params to a domain object by using the Grails bindUtil method used in controllers
 	 * TODO : JPM 10/2014 : The bindData doesn't appear to work correctly in Grails 1.3.8 but hopefully will in 2.3
	 * @param domainObj - the object to assign the parameters to
	 * @param params - the list of parameters
	 * @param options - a map of optional values for include, exclude and filter
	 * @see http://grails.org/doc/2.4.3/ref/Controllers/bindData.html
	 * @see http://codingwithpassion.blogspot.com/2014/06/grails-databind-in-service-layer.html
	 */
    static Closure bindData = { Object[] args ->  
        bindDynamicMethod.invoke(args ? args[0] : null, BindDynamicMethod.METHOD_SIGNATURE, args)  
    }  

	/**
	 * Used to output GORM Domain constraints and update errors in human readable format
	 * @param Domain the domain instance that has errors
	 * @param String the separator to used between listings (default ' : ')
	 * @param String locale (optional)
	 * @return String the errors formatted in human readable format
	 */
	def static String allErrorsString(domain, separator=" : ", locale=java.util.Locale.US) {
		def messageSource = ApplicationContextHolder.getApplicationContext().messageSource
		StringBuilder text = new StringBuilder()
		domain?.errors?.allErrors?.each() { 
			text.append("$separator ${messageSource.getMessage(it, locale)}") 
		}
		text.toString()
	}

	/**
	 * Used to output GORM Domain constraints and update errors in human readable HTML Unordered List
	 * @param Domain the domain instance that has errors
	 * @param String locale (optional)
	 * @return String the errors formatted in human readable format
	 */
	def static String errorsAsUL(domain, locale=java.util.Locale.US) {
		StringBuilder text = new StringBuilder('<ul>')
		text.append(allErrorsString(domain, '<li>', locale))
		text.append('</ul>')
		return text.toString()
	}

	/**
	 * Converts date into GMT
	 * @param date
	 * @return converted Date
	 * @deprecated GormUtil.convertInToGMT has be refactored to the DateUtil.convertInToGMT method
	 */
	def public static convertInToGMT( date, tzId ) {
		return TimeUtil.convertInToGMT(date, tzId)
	}

	/**
	 * Converts date from GMT to local format
	 * @param date
	 * @return converted Date
	 * @deprecated GormUtil.convertInToUserTZ has be refactored to the DateUtil.convertInTOUserTZ method
	 */
	def public static convertInToUserTZ(date, tzId) {
		return TimeUtil.convertInToUserTZ(date, tzId)
	}
	
	/*
	 * Convert a list into a comma delimited of type String to use inside sql statement.
	 * @param List
	 * @return converted List in to String as comma delimited
	 */
	 public static String asCommaDelimitedString( def list ){
		return list.toString().replace("[","").replace("]","")
	}
	
	/*
	 * Convert a list into a quoted comma delimited of type String to use inside sql statement.
	 * @param List
	 * @return converted List in to String as comma delimited
	 */
	 public static String asQuoteCommaDelimitedString( def list ) {
		StringBuffer sb = new StringBuffer()
		def first = true
		list.each { 
			sb.append( (first?'':',') + "'${it}'")
			first = false
		}
		return sb.toString()
	}
	
	/**
	 * This method is used to generate a list of the domain property names that the have the specified constraint. If at value is passed then 
	 * only those properties having the constraint value as such will be returned.
	 * @param domain - the Domain class to find properties within
	 * @param constraintName - String that defines the constraint name (presently supports nullable and blank)
	 * @param value - if passed then it will check the constraint value against that to further filter the list
	 * @return List<String> containing the property name(s) in the domain with/without blank/null constraint
	 * @usage getDomainPropertiesWithConstraint(Model, 'nullable', true) - returns all properties that are nullable 
	 */
	public static List<String> getDomainPropertiesWithConstraint(def domain, def constraintName, def value=null ) {
		 
		def fields = []
		domain.constraints.each() { propName, props ->
			def constraint = props.getAppliedConstraint( constraintName )?.getAt(constraintName)
			switch (constraintName) {
				case 'blank':
					/* By default property blank is false except String prop so if false is requested as value 
					 * and property is not string so considering as 'blank : false' */
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
		if (version.is(null)) {
			println "domainVersionCheck failed on domain $domainObj for no version id parameter"
			throw new RuntimeException("The $label version was missing from request")
		} else {
			if (domainObj.version > version) {
				throw new DomainUpdateException("The $label was updated by someone while you were editting therefore your changes were not saved.")
			}
		}
	}

}
