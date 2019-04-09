package net.transitionmanager.tag

import net.transitionmanager.project.MoveEvent

class TagEvent {
	Date      dateCreated

	static belongsTo = [tag: Tag, event: MoveEvent]

	static constraints = {
		tag unique: ['event']
	}

	static mapping = {
		version false
		id column: 'tag_event_id'
		domainClass enumType: "string"
	}

	Map toMap() {
		[
			id          : id,
			tagId       : tag.id,
			name        : tag.name,
			description : tag.description,
			color       : tag.color.name(),
			css         : tag.color.css,
			dateCreated : dateCreated,
		]
	}
}
