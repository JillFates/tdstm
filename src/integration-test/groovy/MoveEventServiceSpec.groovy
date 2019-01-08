import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.ReportsService
import net.transitionmanager.service.SecurityService
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification

@Integration
@Rollback
class MoveEventServiceSpec extends Specification {

	MoveEventService moveEventService

	// IOC
	ProjectService projectService

	// Initialized by setup()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private Project           project

	void setup() {
		project = projectHelper.createProject()
	}

	void '01. Test save move event'() {
		when: 'Saving a new move event'
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					newsBarMode: 'off',
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		then: 'move event is saved to db'
			moveEvent
			moveEvent.id
			moveEvent.newsBarMode == 'off'
			moveEvent.runbookStatus == 'Pending'
	}

	void '02. Test find move event by id'() {
		setup: 'Given a move event'
			moveEventService.securityService = [getUserCurrentProject: { return project }] as SecurityService
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					newsBarMode: 'off',
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		when: 'Finding a move event by Id'
			MoveEvent foundMoveEvent = moveEventService.findById(moveEvent.id, true)
		then: 'Move event is retrieved from db'
			foundMoveEvent
			foundMoveEvent.id
			foundMoveEvent.newsBarMode == 'off'
			foundMoveEvent.runbookStatus == 'Pending'
		when: 'Finding a non existing move event'
			moveEventService.findById(-1l, true)
		then: 'EmptyResultException is thrown'
			thrown(EmptyResultException)
	}

	void '03. Test update move event'() {
		setup: 'Given a move event'
			moveEventService.securityService = [getUserCurrentProject: { return project }] as SecurityService
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					newsBarMode: 'off',
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		when: 'Updating a move event'
			CreateEventCommand updateCommand = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					newsBarMode: 'off',
					runbookStatus: 'Pending'
			)
			MoveEvent updatedMoveEvent = moveEventService.update(moveEvent.id, updateCommand)
		then: 'Move event is updated in db'
			updatedMoveEvent
			updatedMoveEvent.id
			updatedMoveEvent.newsBarMode == 'off'
			updatedMoveEvent.runbookStatus == 'Pending'
		when: 'Updating a move event missing constraints DomainUpdateException is thrown'
			updateCommand.name = ''
			moveEventService.update(moveEvent.id, updateCommand)
		then: 'DomainUpdateException is thrown'
			thrown(DomainUpdateException)
	}

	void '04. Export runbook to Excel'() {
		setup: 'Given a move event'
			def reportsServiceMock = Mock(ReportsService)
			reportsServiceMock.generatePreMoveCheckList(_ as Long, _ as MoveEvent, _ as Boolean) >>  ['allErrors': [] ]

			moveEventService.reportsService = reportsServiceMock
			moveEventService.securityService = [
					hasPermission        : { return true },
					getUserCurrentProject: { return project },
					isLoggedIn           : { return true },
					viewUnpublished		 : { return false }
			] as SecurityService
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					newsBarMode: 'off',
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		when: 'Exporting runbook to Excel'
			Map<String, ?> exportRunbookToExcel = moveEventService.exportRunbookToExcel(moveEvent, false)
		then: 'a workbook and a file filename is returned'
			exportRunbookToExcel
			exportRunbookToExcel.book
			exportRunbookToExcel.filename
	}
}
