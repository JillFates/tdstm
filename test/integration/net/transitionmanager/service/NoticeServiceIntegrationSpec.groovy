package net.transitionmanager.service


import com.tdssrc.grails.GormUtil
import e2e.PersonTestHelper
import grails.test.spock.IntegrationSpec
import net.transitionmanager.command.NoticeCommand
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import test.helper.ProjectTestHelper

class NoticeServiceIntegrationSpec extends IntegrationSpec {
	NoticeService noticeService

	private PersonTestHelper personHelper
	private ProjectTestHelper projectHelper

	@Shared
	Project project

	@Shared
	PartyGroup partyGroup

	@Shared
	Person whom

	@Shared
	private UserLogin user

	void setup() {
		personHelper = new PersonTestHelper()
		projectHelper = new ProjectTestHelper()

		project = projectHelper.createProject()

		whom = personHelper.createPerson()
		user = personHelper.createUserLoginWithRoles(whom, [], project, false)

		noticeService.securityService = [
				getUserCurrentProject: { -> project },
				assertCurrentProject : { Project project -> },
				loadCurrentPerson    : { -> whom },
				getUserLoginPerson   : { -> whom }
		] as SecurityService
	}

	NoticeCommand createNoticeCommand(Project project = null, Date activationDate = null, Date expirationDate = null, Boolean active = false, Boolean needAcknowledgement = false) {
		return new NoticeCommand(
				title: RandomStringUtils.randomAlphabetic(10),
				rawText: RandomStringUtils.randomAlphabetic(10),
				htmlText: '<div id="'+RandomStringUtils.randomAlphabetic(10)+'">'+RandomStringUtils.randomAlphabetic(10)+'</div>',
				typeId: Notice.NoticeType.POST_LOGIN,
				active: active,
				needAcknowledgement: needAcknowledgement,
				acknowledgeLabel: RandomStringUtils.randomAlphabetic(10),
				project: project,
				activationDate: activationDate,
				expirationDate: expirationDate
		)
	}

	void 'Test create/update/delete notice'() {
		when: 'creating notice without project'
			Notice notice = noticeService.saveOrUpdate(createNoticeCommand())

		then: 'notice is created successfully'
			notice
			notice.id

		when: 'creating notice with a project'
			Notice noticeWithProject = noticeService.saveOrUpdate(createNoticeCommand(project))

		then: 'notice is created successfully and it has a project'
			noticeWithProject
			noticeWithProject.id
			noticeWithProject.project
			noticeWithProject.project.id == project.id

		when: 'updating an existing notice'
			NoticeCommand command = new NoticeCommand()
			command.populateFromDomain(notice)
			command.setTitle('Test notice title')
			command.project = project

			notice = noticeService.saveOrUpdate(command, command.id)

		then: 'notice is updated and expected values are shown'
			notice
			notice.title == 'Test notice title'
			notice.project
			notice.project.id == project.id

		when: 'deleting an existing notice'
			noticeService.delete(noticeWithProject.id)

		then: 'notice should not exist'
			!noticeService.get(noticeWithProject.id)

	}


	void 'Test person unacknowledged notices'() {
		when: 'creating notice that requires acknowledgement'
			Notice notice = noticeService.saveOrUpdate(createNoticeCommand(project, null, null, true, true))

		then: 'person must have notices to acknowledge'
			noticeService.hasUnacknowledgedNotices(whom)

		when: 'notice is not active'
			notice.active = false
			notice.save()

		then: 'person does not have notices to acknowledge'
			!noticeService.hasUnacknowledgedNotices(whom)
	}

	void 'Test person unacknowledged notices with activation date'() {
		setup: 'having activation date'
			Date activationDate = new Date().clearTime()

		when: 'creating notice that requires acknowledgement'
			Notice notice = noticeService.saveOrUpdate(createNoticeCommand(project, activationDate, null, true, true))

		then: 'person must have notices to acknowledge'
			noticeService.hasUnacknowledgedNotices(whom)

		when: 'activation date is future'
			notice.activationDate = activationDate.plus(1)
			notice.save()

		then: 'person does not have notices to acknowledge'
			!noticeService.hasUnacknowledgedNotices(whom)

	}

	void 'Test person unacknowledged notices with expiration date'() {
		setup: 'having expiration date'
			Date expirationDate = new Date().plus(1).clearTime()

		when: 'creating notice that requires acknowledgement'
			Notice notice = noticeService.saveOrUpdate(createNoticeCommand(project, null, expirationDate, true, true))

		then: 'person must have notices to acknowledge'
			noticeService.hasUnacknowledgedNotices(whom)

		when: 'expiration date is past'
			notice.expirationDate = expirationDate.minus(1)
			notice.save()

		then: 'person does not have notices to acknowledge'
			!noticeService.hasUnacknowledgedNotices(whom)

	}

	void 'Test person acknowledge notice'() {
		when: 'creating notice that requires acknowledgement'
			Notice notice = noticeService.saveOrUpdate(createNoticeCommand(project, null, null, true, true))

		then: 'person must have notices to acknowledge'
			noticeService.hasUnacknowledgedNotices(whom)

		when: 'person acknowledge a notice'
			println(GormUtil.domainObjectToMap(user))
			noticeService.ack(notice.id, user.username)

		then: 'person does not have more notices to acknowledge'
			!noticeService.hasUnacknowledgedNotices(whom)

	}

}
