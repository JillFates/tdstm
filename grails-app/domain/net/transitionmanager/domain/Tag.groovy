package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.Color

class Tag {
	String  name
	String  description = ''
	Color   color
	Date    dateCreated
	Date    lastUpdated

	Collection tagAssets

	static belongsTo = [project: Project]

	static hasMany = [tagAssets: TagAsset]

	static constraints = {
		name size: 1..50, unique: 'project'
		description size: 0..255
		color inList: Color.values().toList()
		dateCreated nullable: true
		lastUpdated nullable: true
	}

	static mapping = {
		id column: 'tag_id'
	}


	Map toMap() {
		[
			id          : id,
			name        : name,
			description : description,
			color       : color.name(),
			css         : color.css,
			dateCreated : dateCreated,
			lastModified: lastUpdated
		]
	}
}
