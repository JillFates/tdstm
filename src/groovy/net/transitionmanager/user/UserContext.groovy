package net.transitionmanager.user

import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin

class UserContext {

	UserLogin userLogin

	Person person

	Project project

	MoveEvent moveEvent

	MoveBundle moveBundle

	String timezone

	String dateFormat


	/**
	 * Create and return a map representation of the User's context with only a selection of fields.
	 * @return a map with the values for the context object.
	 */
	Map toMap() {
		Map eventMap = null
		if (moveEvent) {
			eventMap = [
				id: moveEvent.id,
				name: moveEvent.name
			]
		}
		Map bundleMap = null
		if (moveBundle) {
			bundleMap = [
				id: moveBundle.id,
				name: moveBundle.name
			]
		}
		return [
		    user: [
		        id: userLogin.id,
			    username: userLogin.username
		    ],
			person: [
			    id: person.id,
				fullName: person.toString()
			],
			currentProject: [
			    id: project.id,
				name: project.name
			],
			currentEvent: eventMap,
			currentBundle: bundleMap,
			timezone: timezone,
			dateFormat: dateFormat
		]
	}
}
