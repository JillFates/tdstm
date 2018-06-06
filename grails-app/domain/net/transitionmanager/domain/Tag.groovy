package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil

class Tag {
	String  name
	String  description = ''
	Color   color
	Project project
	Date    dateCreated
	Date    lastUpdated

	Collection tagLinks

	static belongsTo = [project: Project]

	static hasMany = [tagLinks: TagLink]

	static constraints = {
		name size: 1..50, unique: 'project'
		description size: 0..255
		color inList: Color.values().toList()
		dateCreated nullable: true
		lastUpdated nullable: true
	}

	static mapping = {
		id column: 'tag_id'
		autoTimestamp false
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT().clearTime()
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT().clearTime()
	}

	Map toMap() {
		[
			id          : id,
			Name        : name,
			Description : description,
			Color       : color.name(),
			css         : color.css,
			DateCreated : dateCreated,
			LastModified: lastUpdated
		]
	}
}
