package net.transitionmanager.domain

import com.google.gson.JsonElement
import grails.converters.JSON
import groovy.json.JsonBuilder
import net.transitionmanager.domain.License.Environment
import net.transitionmanager.domain.License.Status
import net.transitionmanager.domain.License.Type
import net.transitionmanager.domain.License.Method
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

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

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		version 	false
		tablePerHierarchy false
		activationDate	column:'valid_start'
		expirationDate	column:'valid_end'
		installationNum column:'instalation_num'
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
				environment		: [
						id: environment?.id,
						name: environment?.name()
				],
				type			: [
						id: type?.id,
						name: type?.name()
				],
				method			: [
						id: method?.id,
						name: method?.name(),
						max: max
				],
				status			: [
						id: status?.id,
						name: status?.name()
				],
				installationNum	: installationNum,
				project			: dProject,
				client			: dClient,
				owner			: dOwner,
				activationDate	: activationDate,
				expirationDate 	: expirationDate,
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

	static LicensedClient fetch(JSONElement json, createIfNotFound = false){
		Closure dateParser = {String strDate ->
			if(strDate){
				try {
					return org.apache.tools.ant.util.DateUtils.parseIso8601DateTime(strDate)
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
			lc.requestDate = dateParser(json.requestDate)
		}

		if(json.requestDate != null) {
			lc.environment = License.Environment.forId(json.environment?.id)
		}
		if(json.method?.id != null) {
			lc.method = License.Method.forId(json.method?.id)
		}
		if(json.method?.max != null) {
			lc.max = (json.method?.max) ?: 0
		}
		if(json.type?.id != null) {
			lc.type = License.Type.forId(json.type?.id)
		}
		if(json.status?.id != null) {
			lc.status = License.Status.forId(json.status?.id)
		}
		if(json.environment?.id != null) {
			lc.environment = License.Environment.forId(json.environment?.id)
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

}
