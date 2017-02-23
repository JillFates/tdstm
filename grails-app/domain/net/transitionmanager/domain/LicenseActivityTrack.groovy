package net.transitionmanager.domain

import com.tdsops.common.grails.ApplicationContextHolder
import grails.converters.JSON
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.web.json.JSONElement

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
		version false
	}

	JSONElement getChangesJSON(){
		JSON.parse(changes)
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
		/*
		  WARNING: Beware of the IDEs!! somehow IntelliJ doesnt get the 'dirtyPropertyNames'
		  but this is the correct way of doing it
		 */
		def dirtyProperties = licensedClient.dirtyPropertyNames
		List<Map> changes = []
		dirtyProperties.each { field ->
			def currentValue = licensedClient[field]
			def originalValue = licensedClient.getPersistentValue(field)
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
