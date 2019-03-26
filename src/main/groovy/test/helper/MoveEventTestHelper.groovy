package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class MoveEventTestHelper {

	MoveEvent createMoveEvent(Project project) {
		MoveEvent moveEvent = new MoveEvent(
				project: project,
				name: 'Test MoveEvent - ' +  RandomStringUtils.randomAlphabetic(10),
				calcMethod: MoveEvent.METHOD_LINEAR
		).save(flush: true)
		return moveEvent
	}

	/**
	 * Create a move event if not exists from given name for E2EProjectSpec to persist at server DB
	 * @param: project
	 * @param: name
	 * @returm the event
	 */
	MoveEvent createMoveEvent(Project project, String name) {
		MoveEvent event = MoveEvent.findWhere([name: name, project: project])
		if (!event) {
			event = new MoveEvent([project: project, name: name, calcMethod: MoveEvent.METHOD_LINEAR])
			event.save(flush: true)
		}
		return event
	}
}
