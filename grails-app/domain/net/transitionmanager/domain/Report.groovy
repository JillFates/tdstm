package net.transitionmanager.domain

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil

class Report {

	Project project
	Person person
	String name
	Boolean isSystem
	Boolean isShared
	String reportSchema
	Date dateCreated
	Date lastModified

    static constraints = {
		name nullable: false
		lastModified nullable: true
    }

	static mapping = {
		name sqltype: 'varchar(30)'
		project column: 'project_id'
		person column: 'person_id'
	}

	Map toMap(Long currentPersonId) {
		Boolean isOwner = person ? (person.id == currentPersonId) : false
		Map data = [
				name			: name,
				isSystem		: isSystem,
				isShared		: isShared,
				isOwner			: isOwner,
				schema			: JsonUtil.parseJson(reportSchema)
		]
		return data
	}

	def beforeInsert = {
		dateCreated = dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

}