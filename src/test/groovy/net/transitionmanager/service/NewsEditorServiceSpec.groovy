package net.transitionmanager.service

import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import spock.lang.Shared
import spock.lang.Specification

class NewsEditorServiceSpec extends Specification implements ServiceUnitTest<NewsEditorService>, DataTest {

	@Shared
	Project project

	@Shared
	PartyGroup partyGroup

	@Shared
	MoveEvent moveEvent

	@Shared
	Person whom

	void setupSpec(){
		mockDomains PartyGroup, PartyType, Project, MoveEventNews, Person, MoveEvent
	}

	void setup() {
		partyGroup = new PartyGroup(name: 'projectClientName').save(flush: true, failOnError: true)

		project = new Project(
			name: 'projectName',
			projectCode: 'projectCode',
			completionDate: (new Date() + 5).clearTime(),
			description: 'projectDescription',
			client: partyGroup,
			workflowCode: '12345',
			guid: StringUtil.generateGuid()
		).save(flush: true, failOnError: true)

		whom = new Person(firstName: 'Danger', lastName: 'Powers').save(flush: true, failOnError: true)
		moveEvent = new MoveEvent([project: project, name: 'ERP Event']).save(flush: true, failOnError: true)

		service.securityService = [
			getUserCurrentProject: { -> project },
			assertCurrentProject : { Project project -> },
			loadCurrentPerson    : { -> whom }
		] as SecurityService
	}

	void '01. Test save'() {
		when: 'saving a valid move event news'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none', 0)
			MoveEventNews savedNews = MoveEventNews.get(news.id)
		then: 'the news is saved to the db'
			savedNews.id == news.id
			savedNews.message == 'a message'
			savedNews.archivedBy == null
			savedNews.isArchived == 0
			savedNews.resolution == 'none'
			savedNews.createdBy == whom
	}


	void '02. Test save archived'() {
		when: 'saving a valid move event news, setting it to archived'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none', 1)
			MoveEventNews savedNews = MoveEventNews.get(news.id)
		then: 'the news is saved, and archived by is also saved.'
			savedNews.id == news.id
			savedNews.message == 'a message'
			savedNews.archivedBy == whom
			savedNews.isArchived == 1
			savedNews.resolution == 'none'
			savedNews.createdBy == whom
	}

	void '03. Test save invalid move event'() {
		when: 'trying to save a move event news with a -1 for the move event id'
			MoveEventNews news = service.save(project, -1, 'a message', 'none', 1)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
		when: 'trying to save a move event news with a null for the move event id'
			news = service.save(project, null, 'a message', 'none', 1)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
		when: 'trying to save a move event news with an invalid id for the move event id'
			news = service.save(project,999999, 'a message',  'none', 1)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
	}


	void '04. Test update'() {
		setup: 'given a saved MoveEventNews'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none', 0)
		when: 'Updating the MoveEventNews with a new message and resolution'
			service.update(project, news.id,  'a  new message', 'resolved', 0)

			MoveEventNews savedNews = MoveEventNews.get(news.id)
		then: 'the changes are saved to the db'
			savedNews.id == news.id
			savedNews.message == 'a  new message'
			savedNews.archivedBy == null
			savedNews.isArchived == 0
			savedNews.resolution == 'resolved'
			savedNews.createdBy == whom
	}

	void '05. Test update archived'() {
		setup: 'given a saved MoveEventNews'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none',0)
		when: 'Updating the MoveEventNews with a new message, resolution, and archiving it'
			service.update(project, news.id, 'a  new message', 'resolved', 1)

			MoveEventNews savedNews = MoveEventNews.get(news.id)
		then: 'the changes are saved to the db, and archivedBy is set'
			savedNews.id == news.id
			savedNews.message == 'a  new message'
			savedNews.archivedBy == whom
			savedNews.isArchived == 1
			savedNews.resolution == 'resolved'
			savedNews.createdBy == whom
	}

	void '06. Test update invalid id'() {
		setup: 'given a saved MoveEventNews'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none', 0)
		when: 'trying to update a move event news with a -1 for the move event id'
			service.update(project,  -1, 'a  new message', 'resolved', 0)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
		when: 'trying to update a move event news with a null for the move event id'
			service.update(project, null, 'a  new message', 'resolved', 0)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
		when: 'trying to save a move event news with an invalid id for the move event id'
			service.update(project, 9999999, 'a  new message', 'resolved', 0)

			MoveEventNews.get(news.id)
		then: 'An Invalid Param Exception is thrown'
			thrown EmptyResultException
	}

	void '07. Test delete'() {
		setup: 'given a saved MoveEventNews'
			MoveEventNews news = service.save(project, moveEvent.id, 'a message', 'none', 0)
		when: 'deleting the news'
			service.delete(news.id, project)
			MoveEventNews savedNews = MoveEventNews.get(news.id)
		then: 'the news is no long in the db'
			!savedNews
	}


	void '08. Test delete invalid id'() {
		setup: 'given a saved MoveEventNews'
			service.save(project, moveEvent.id, 'a message', 'none', 0)
		when: 'trying to delete a null id  MoveEventNews'
			service.delete(null, project)
		then: 'an error message is returned'
			thrown InvalidParamException
		when: 'trying to delete a -1 id  MoveEventNews'
			service.delete(-1, project)
		then: 'an error message is returned'
			thrown InvalidParamException
		when: 'trying to delete an invalid id  MoveEventNews'
			service.delete(999999, project)
		then: 'an error message is returned'
			thrown EmptyResultException
	}
}
