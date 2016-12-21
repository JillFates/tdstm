package net.transitionmanager.domain

import grails.converters.JSON
import groovy.json.JsonBuilder
import net.transitionmanager.domain.License.Environment
import net.transitionmanager.domain.License.Status
import net.transitionmanager.domain.License.Type
import net.transitionmanager.domain.License.Method

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

	public toJsonMap() {
		def dProject = project ? JSON.parse(project) : null
		def dClient = client ? JSON.parse(client) : null
		def dOwner = owner ? JSON.parse(owner) : null

		[
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
				activityList	: []
		]
	}

	public toJsonString(){
		new JsonBuilder( toJsonMap() ).toString()
	}

}
