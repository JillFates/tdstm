package net.transitionmanager.command

import net.transitionmanager.project.Project
import net.transitionmanager.asset.Room

/**
 * RoomCommand is to be used with the creation and editing of Room domain
 */

class RoomCommand implements CommandObject {
	Project  project
	String   roomName
	String   location
	Integer  roomWidth = 24
	Integer  roomDepth = 24
	String   address
	String   city
	String   stateProv
	String   postalCode
	String   country

	static constraints = {
		importFrom Room, include: [
				  'project',
				  'roomName',
				  'location',
				  'roomWidth = 24',
				  'roomDepth = 24',
				  'address',
				  'city',
				  'stateProv',
				  'postalCode',
				  'country',
		]
	}
}
