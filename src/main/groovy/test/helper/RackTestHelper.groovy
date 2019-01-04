package test.helper

import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room

class RackTestHelper {


	Rack createRack(Project project, Room room, Manufacturer manufacturer = null, Model model = null, String location = 'ACME Data Center', String tag = 'ACME'){
		return buildRacks(project, room, manufacturer, model, location, tag, 'L', 1, 160, 0, 1430, 1430, 0, 'Rack')
	}

	Rack buildRacks(Project project, Room room, Manufacturer manufacturer, Model model, String location,
	                String tag, String front, Integer source, Integer roomX, Integer roomY,
	                Integer powerA, Integer powerB, Integer powerC, String rackType) {
		Rack rack = new Rack()
		rack.project = project
		rack.room = room
		rack.tag = tag
		rack.manufacturer = manufacturer
		rack.model = model
		rack.location = location
		rack.front = front
		rack.source = source
		rack.roomX = roomX
		rack.roomY = roomY
		rack.powerA = powerA
		rack.powerB = powerB
		rack.powerC = powerC
		rack.rackType = rackType
		rack.save(failOnError: true)
		return rack
	}
}
