package net.transitionmanager.domain

import grails.converters.JSON
import groovy.json.JsonBuilder
import net.transitionmanager.domain.License.Environment
import net.transitionmanager.domain.License.Status
import net.transitionmanager.domain.License.Type
import net.transitionmanager.domain.License.Method
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

import java.text.ParseException

/**
 * At first it was a good idea to extend from License, nos since License is an instance in every installation
 * I think that we can end with dependencies that can hurt more than do good, so we rather create (and repeat) some of the data needed
 * since it's used for different domain problems.
 * @author oluna
 */
class LicensedClient {
	String id
	String installationNum
	String email
	Environment environment
	Status status
	Type   type
	String project
	String client
	String owner
	Method method
	int    max = 0
	Date   requestDate
	Date   activationDate
	Date   expirationDate
	String requestNote
	String hostName
	String websitename
	String hash
	String bannerMessage
	int	   gracePeriodDays = 5

	/** Date when the change was performed */
	Date dateCreated
	Date lastUpdated

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		version		false
		tablePerHierarchy false
		activationDate	column:'valid_start'
		expirationDate	column:'valid_end'
		bannerMessage type:'text'
	}

	static constraints = {
		method 			nullable:true
		activationDate 	nullable:true
		expirationDate 	nullable:true
		requestNote 	nullable:true
		hash 			nullable:true
		bannerMessage	nullable:true
	}

	Map toMap() {
		JSONObject dProject = project ? JSON.parse(project) : null
		JSONObject dClient = client ? JSON.parse(client) : null
		JSONObject dOwner = owner ? JSON.parse(owner) : null

		Map data = [
				id				: id,
				email			: email,
				environment		: environment?.name(),
				type			: type?.name(),
				method			: [
						name: method?.name(),
						max: max
				],
				status			: status?.name(),
				installationNum	: installationNum,
				project			: dProject,
				client			: dClient,
				owner			: dOwner,
				activationDate	: activationDate?.format("yyyy-MM-dd"),
				expirationDate 	: expirationDate?.format("yyyy-MM-dd"),
				requestDate		: requestDate,
				requestNote		: requestNote,
				hostName		: hostName,
				websitename		: websitename,
				bannerMessage	: bannerMessage,
				gracePeriodDays : gracePeriodDays,
				activityList	: []
		]

		return data
	}

	String toJsonString(){
		new JsonBuilder( toMap() ).toString()
	}

	String missingPropertyErrors(){
		Collection<Map<String, String>> requiredProps = [
				[prop:"max",			error:"Max Servers"],
				[prop:"activationDate", error:"Begin Valid Date"],
				[prop:"expirationDate", error:"End Valid Date"]
		]

		String errors = ""
		requiredProps.each {
			if(!owner[it.prop]){
				errors += "|\t* ${it.error}\n"
			}
		}

		if(errors){
			errors = """\
					|Missing required Parameters to generate License check any of:
					${errors}
				""".stripMargin()
		}
		errors
	}

	static LicensedClient fetch(JSONElement json, createIfNotFound = false){
		Closure dateParser = {String strDate ->
			if(strDate){
				try {
					return Date.parse("yyyy-MM-dd", strDate)
				}catch(ParseException pe){
					log.error("Error Parsing Date", pe)
				}
			}
			return null
		}
		Closure dateTimeParser = {String strDate ->
			if(strDate){
				try {
					return 	Date.parse("yyyy-MM-dd'T'HH:mm:ssX", strDate)
				}catch(ParseException pe){
					log.error("Error Parsing Date", pe)
				}
			}
			return null
		}

		if(!json.id){
			return null
		}

		LicensedClient lc = LicensedClient.get(json.id)

		if(!lc && createIfNotFound) {
			lc = new LicensedClient()
		}

		lc.id = json.id

		if(json.installationNum != null) {
			lc.installationNum = json.installationNum
		}
		if(json.email != null) {
			lc.email = json.email
		}
		if(json.requestNote != null) {
			lc.requestNote = json.requestNote
		}
		if(json.hostName != null) {
			lc.hostName = json.hostName
		}
		if(json.websitename != null) {
			lc.websitename = json.websitename
		}
		if(json.expirationDate) {
			lc.expirationDate = dateParser(json.expirationDate)
		}
		if(json.activationDate) {
			lc.activationDate = dateParser(json.activationDate)
		}
		if(json.requestDate != null) {
			lc.requestDate = dateTimeParser(json.requestDate)
		}

		if(json.environment != null) {
			lc.environment = json.environment as License.Environment
		}
		if(json.method?.name != null) {
			lc.method = json.method?.name as License.Method
		}
		if(json.method?.max != null) {
			lc.max = (json.method?.max) ?: 0
		}
		if(json.type != null) {
			lc.type = json.type as License.Type
		}
		if(json.status != null) {
			lc.status = json.status as License.Status
		}
		if(json.project != null) {
			lc.project = json.project?.toString()
		}
		if(json.client != null) {
			lc.client = json.client?.toString()
		}
		if(json.owner != null) {
			lc.owner = json.owner?.toString()
		}

		if(json.bannerMessage != null) {
			lc.bannerMessage = json.bannerMessage
		}

		if(json.gracePeriodDays != null){
			lc.gracePeriodDays = json.gracePeriodDays
		}

		return lc
	}

	/**
	 * Retrieve all changes on the Entity and if any build a Json that will be stored
	 * @return
	 */
	def beforeUpdate(){
		//register the changes in the class
		LicenseActivityTrack.trackChanges(this)
		return true
	}

}
