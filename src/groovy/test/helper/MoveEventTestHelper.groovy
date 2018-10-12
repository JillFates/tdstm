package test.helper

import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

class MoveEventTestHelper {

	MoveEvent createMoveEvent(Project project) {
		MoveEvent moveEvent = new MoveEvent(
				project: project,
				name: 'Test MoveEvent - ' +  RandomStringUtils.randomAlphabetic(10),
				calcMethod: MoveEvent.METHOD_LINEAR
		).save(failOnError: true)
		return moveEvent
	}

	MoveEvent createMoveEvent(Project project, String name) {
		MoveEvent event = MoveEvent.findWhere([name: name, project: project])
		if (!event) {
			event = new MoveEvent([project: project, name: name, calcMethod: MoveEvent.METHOD_LINEAR])
			event.save(flush: true)
		}
		return event
	}
}
