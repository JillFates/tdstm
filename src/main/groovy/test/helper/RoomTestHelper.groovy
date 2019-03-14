package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Room

@Transactional
class RoomTestHelper {

	Room createRoom(Project project, String name = 'DC1', String location = 'ACME Data Center'){
		return buildRoom(project, name, location, 26, 40, '112 Main St', 'Cumberland', 'IA', '50843')
	}

	Room buildRoom(Project project, String name, String location,
	               Integer roomDepth, Integer roomWidth, String address,
	               String city, String stateProv, String postalCode) {

		Room room = new Room()
		room.project = project
		room.roomName = name
		room.location = location
		room.roomDepth = roomDepth
		room.roomWidth = roomWidth
		room.address = address
		room.city = city
		room.stateProv = stateProv
		room.postalCode = postalCode
		room.save()
		return room
	}
}
