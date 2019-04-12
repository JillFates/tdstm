package net.transitionmanager.license


import grails.converters.JSON
import groovy.json.JsonSlurper
import net.transitionmanager.security.UserLogin

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

	static transients=['changesJSON']
	static mapping = {
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
}
