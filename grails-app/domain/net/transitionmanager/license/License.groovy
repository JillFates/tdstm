package net.transitionmanager.license

import com.github.icedrake.jsmaz.Smaz
import com.tdssrc.grails.jasypt.GormEncryptedDateAsStringType
import groovy.json.JsonBuilder
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import org.apache.commons.codec.binary.Base64
import grails.core.GrailsApplication

/**
 * Created by octavio on 9/20/16.
 */
class License {
	/* TAG grapper for messaging */
	static String BEGIN_REQ_TAG = "-----BEGIN LICENSE REQUEST-----"
	static String END_REQ_TAG = "-----END LICENSE REQUEST-----"
	static String WILDCARD = ""

	String      id = UUID.randomUUID().toString()
	String      installationNum
	String      email
	PartyGroup  owner
	Environment environment
	Status      status
	Type        type
	String      project
	Method      method
	int         max = 0
	Date        requestDate
	Date        activationDate
	Date        expirationDate
	String      requestNote
	String      hash
	String      hostName
	String      websitename
	String      bannerMessage

	/** Date when the change was performed */
	Date 		dateCreated
	Date 		lastUpdated

	/** Last time that we saw this License in compliance (number of servers or other constrains) */
	Date 		lastCompliance

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		tablePerHierarchy false
		version		false
		activationDate	column:'valid_start'
		expirationDate	column:'valid_end'
		lastCompliance	type: GormEncryptedDateAsStringType
	}

	static constraints = {
		method 			nullable:true
		activationDate 	nullable:true
		expirationDate 	nullable:true
		requestNote 	nullable:true
		hash 			nullable:true
		bannerMessage	nullable:true
		lastCompliance	nullable:true
	}

	static transients = [ 'projectInstance' ]

	boolean isActive(){
		return (hash)? true : false
	}

	PartyGroup getClient(){
		PartyGroup client
		Project prj = projectInstance
		if(prj) {
			client = prj.client
		}

		return client
	}

	Project getProjectInstance() {
		Project prj
		if(project != "all") {
			prj = Project.get(project)
		}
		return prj
	}

	/*
	 TODO: Add comments, change all calls to this to toMap()
	Return a Map of the properties
	*/
	Map toJsonMap(GrailsApplication grailsApplication) {
		PartyGroup client = getClient()
		Map dProject = [
				  id:"",
				  name:"all",
				  guid: "",
				  // metricsGathering: false  // TODO: this is the client part of "Metrics Gathering" variable used by "tmci" to gather info is required in the CLIENT?
		]

		Project prj = projectInstance
		if(prj){
			dProject.id   = prj.id
			dProject.name = prj.name
			dProject.guid = prj.guid
		}

		String toEmail = (grailsApplication.config.tdstm?.license?.request_email) ?: ''

		Map data = [
			id         : id,
			email      : email,
			toEmail    : toEmail,
			environment: environment?.name(),
			owner: [
				id  : owner?.id,
				name: owner?.name
			],
			type  : type?.name(),
			method: [
				name: method?.name(),
				max : max
			],
			status         : status?.name(),
			installationNum: installationNum,
			project        : dProject,
			client         : [
				id  : client?.id,
				name: client?.name
			],
			activationDate: activationDate?.format("yyyy-MM-dd"),
			expirationDate: expirationDate?.format("yyyy-MM-dd"),
			requestDate   : requestDate,
			requestNote   : requestNote,
			hostName      : hostName,
			websitename   : websitename,
			bannerMessage : bannerMessage
		]

		return data
	}

	String toJsonString(GrailsApplication grailsApplication){
		new JsonBuilder( toJsonMap(grailsApplication) ).toString()
	}

	String toEncodedMessage(GrailsApplication grailsApplication){
		String body = new String(Base64.encodeBase64(Smaz.compress(toJsonString(grailsApplication))))
		return "${BEGIN_REQ_TAG}\n${body}\n${END_REQ_TAG}"
	}

	/** Enumerator Helpers *******************************************/
	enum Environment {
		ENGINEERING,
		TRAINING,
		DEMO,
		PRODUCTION
	}

	enum Type {
		SINGLE_PROJECT,
		MULTI_PROJECT
	}

	enum Method {
		// Accounting based on highwater limit of the number of servers
		MAX_SERVERS,

		// Accounting based on average number of servers under management during the month where tokens
				// are deducted until the tokensUsed >= tokensAllocated
		TOKEN,

		// Used for custom licenses where there is no limits of usage
		CUSTOM
	}

	enum Status {
		ACTIVE,
		EXPIRED,
		TERMINATED,
		PENDING,
		CORRUPT
	}

}
