package net.transitionmanager.license

import com.github.icedrake.jsmaz.Smaz
import com.tdsops.common.security.AESCodec
import com.tdssrc.grails.TimeUtil

import groovy.json.JsonBuilder
import groovy.time.TimeCategory
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import org.apache.commons.codec.binary.Base64
import grails.core.GrailsApplication

import java.security.GeneralSecurityException

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

	/** Last time that we saw this License in compliance (number of servers or other constraints) */
	String 		lastComplianceHash

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		tablePerHierarchy false
		version		false
		activationDate	column:'valid_start'
		expirationDate	column:'valid_end'
	}

	static constraints = {
		method 				nullable:true
		activationDate		nullable:true
		expirationDate		nullable:true
		requestNote 		nullable:true
		hash 				nullable:true
		bannerMessage		nullable:true
		lastComplianceHash	nullable:false, blank: false
	}

	static transients = [ 'projectInstance', 'lastComplianceDate' ]

	boolean isActive(){
		return (hash)? true : false
	}

	/**
	 * Apply logic to resolve the compliance of the license Date
	 * @author oluna
	 */
	void settleCompliance() {
		use( TimeCategory ) {
			setLastComplianceDate( complianceShiftDate() )
		}
	}

	/**
	 * Return future compliance date applying the logic of 1 hour in the future
	 * @return Date Now + 1 hour
	 */
	static Date complianceShiftDate() {
		use( TimeCategory ) {
			return TimeUtil.nowGMT() + 1.hour
		}
	}

	/**
	 * Returns the decripted form of the las seen compliance Date
	 * If is set in the past, and the number of licenses is below permitted then we assume it was the last time that the license was valid
	 * if it's in the future (a constrainted ammount) it's a shift date to avoid people messing with values
	 * If the data is corrupted we can believe somebody tryed to hack it so we return a GeneralSecurityException
	 * @return Date last compliance of the license
	 * @throws GeneralSecurityException
	 */
	Date getLastComplianceDate() throws GeneralSecurityException {
		String epochStr = AESCodec.getInstance().decode(lastComplianceHash, id)
		return new Date(epochStr.toLong())
	}

	/**
	 * Establish a fix date as the last compliance date and encrypt it
	 * @param date to set
	 */
	void setLastComplianceDate( Date date ) {
		long epochTime = date.time
		String epochStr = "${epochTime}"
		lastComplianceHash = AESCodec.getInstance().encode(epochStr, id)
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
