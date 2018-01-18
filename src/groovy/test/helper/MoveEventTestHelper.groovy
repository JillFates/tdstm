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
		).save(flush: true, failOnError: true)
		return moveEvent
	}
}
