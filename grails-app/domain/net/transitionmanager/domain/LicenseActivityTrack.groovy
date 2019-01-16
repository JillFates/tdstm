package net.transitionmanager.domain

import com.tdsops.common.grails.ApplicationContextHolder
import grails.converters.JSON
import groovy.json.JsonSlurper
import net.transitionmanager.service.SecurityService

/**
 * Created by octavio on 2/20/17.
 */
class LicenseActivityTrack {
	/** Date when the change was performed */
	Date dateCreated

	/** User who did the change */
	UserLogin userLogin

	/** Client that has holds the activity*/
	LicensedClient licensedClient

	/** json Object representing the changes in the activity
		{
			property:{
				oldValue:[value],
				newValue:[value]
			}
		}
 	*/
	String changes

	static transients=['securityService', 'changesJSON']
	static mapping = {
		autowire true
		version false
	}

	static constraints = {
		userLogin	nullable:true
	}

	/*
	 * Used to access the changes as a List of Map objects instead of JSON text
	 * @return The changes JSON String as Groovy List<Map>
	 */
	List<Map> getChangesList(){
		JsonSlurper slurper = new JsonSlurper()
		List<Map> list = []
		if (changes) {
			try {
				list = slurper.parseText(changes)
			} catch (e) {
				log.error "'changes' was not propertly formed JSON (value=$changes) : ${e.getMessage()}"
			}
		}
		list
	}

	/**
	 * Constructor that creates a new LicenseActivityTrack using the tracked changes of the Object
	 * @param userLogin
	 * @param licensedClient
	 * @param changes
	 */
	LicenseActivityTrack(UserLogin userLogin, LicensedClient licensedClient, List<Map> changes){
		this.licensedClient = licensedClient
		this.changes = (changes as JSON).toString()

		this.userLogin = userLogin
	}

	/**
	 * Factory/Creator object that creates an instance of an LicenseActivityTrack if there are changes to report
	 * @param licensedClient
	 * @return
	 */
	static LicenseActivityTrack trackChanges(LicensedClient licensedClient){
		//Convert non standar Json Values (String, Number, Boolean) to the toString representation
		Closure toBasicType = { input ->
			switch(input){
				case String:
				case Number:
				case Boolean: break
				default:
					input = String.valueOf(input)
			}
			input
		}

		/*
		  WARNING: Beware of the IDEs!! somehow IntelliJ doesn't get the 'dirtyPropertyNames'
		  but this is the correct way of doing it!!
		 */
		Collection<String> dirtyProperties = licensedClient.dirtyPropertyNames
		List<Map> changes = []
		dirtyProperties.each { field ->
			Object currentValue = toBasicType(licensedClient[field])
			Object originalValue = toBasicType(licensedClient.getPersistentValue(field))

			if (currentValue != originalValue) {
				changes << [field:field, oldValue:originalValue, newValue:currentValue]
			}
		}

		LicenseActivityTrack licenseActivityTrack
		if(changes) {
			SecurityService securityService = ApplicationContextHolder.getService("securityService")
			UserLogin userLogin = securityService.userLogin
			licenseActivityTrack = new LicenseActivityTrack(userLogin, licensedClient, changes)
			licenseActivityTrack.save()
		}
		licenseActivityTrack
	}
}
