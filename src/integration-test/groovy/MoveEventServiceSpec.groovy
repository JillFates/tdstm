import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.ProjectService
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.security.SecurityService
import org.apache.commons.lang3.RandomStringUtils
import grails.validation.ValidationException
import spock.lang.Specification

@Integration
@Rollback
class MoveEventServiceSpec extends Specification {

	MoveEventService moveEventService

	// IOC
	ProjectService projectService

	// Initialized by setup()
	private ProjectTestHelper projectHelper
	private Project project

	void setup() {
		projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()
	}

	void '01. Test save move event'() {
		when: 'Saving a new move event'
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		then: 'move event is saved to db'
			moveEvent
			moveEvent.id
			moveEvent.runbookStatus == 'Pending'
	}

	void '02. Test find move event by id'() {
		setup: 'Given a move event'
			moveEventService.securityService = [getUserCurrentProject: { return project }] as SecurityService
			CreateEventCommand command = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		when: 'Finding a move event by Id'
			MoveEvent foundMoveEvent = moveEventService.findById(moveEvent.id, true)
		then: 'Move event is retrieved from db'
			foundMoveEvent
			foundMoveEvent.id
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
					runbookStatus: 'Pending'
			)
			MoveEvent moveEvent = moveEventService.save(command, project)
		when: 'Updating a move event'
			CreateEventCommand updateCommand = new CreateEventCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
					runbookStatus: 'Pending'
			)
			MoveEvent updatedMoveEvent = moveEventService.update(moveEvent.id, updateCommand)
		then: 'Move event is updated in db'
			updatedMoveEvent
			updatedMoveEvent.id
			updatedMoveEvent.runbookStatus == 'Pending'
		when: 'Updating a move event missing constraints DomainUpdateException is thrown'
			updateCommand.name = ''
			moveEventService.update(moveEvent.id, updateCommand)
		then: 'DomainUpdateException is thrown'
			thrown(ValidationException)
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
