package net.transitionmanager.service

import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.StringUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.validation.ValidationException
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.domain.Tag
import spock.lang.Shared
import spock.lang.Specification

@TestFor(TagService)
@TestMixin([GrailsUnitTestMixin, ControllerUnitTestMixin])
@Mock([PartyGroup, PartyType, Project, Setting, Tag])
class TagServiceSpec extends Specification {

	@Shared
	Project project

	@Shared
	PartyGroup partyGroup

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

		service.securityService = [
			getUserCurrentProject: { -> project },
			assertCurrentProject : { Project project -> }
		] as SecurityService
	}

	void 'Test create'() {
		when: 'Calling create with a valid name, decription, and color'
			Tag tag = service.create(project, 'tag1', 'description1', Color.Red)

		then: 'The tag is created and returned.'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test get'() {
		when: 'Calling get with an id of a tag'
			service.create(project, 'tag1', 'description1', Color.Red)
			Tag tag = service.get(Tag, 1, project)

		then: 'The tag is returned'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test create missing name'() {
		when: 'Calling create with an invalid name parameter'
			Tag tag = service.create(project, null, 'description1', Color.Red)

		then: 'the tag will be returned with errors.'
			thrown ValidationException
	}

	void 'Test create missing description'() {
		when: 'Calling create with an invalid description parameter'
			Tag tag = service.create(project, 'tag1', null, Color.Red)

		then: 'the tag will be returned with errors.'
			thrown ValidationException
	}

	void 'Test create missing color'() {
		when: 'Calling create with an invalid color parameter'
			Tag tag = service.create(project, 'tag1', 'description1', null)

		then: 'the tag will be returned with errors.'
			thrown ValidationException
	}

	void 'Test update name'() {
		when: 'Calling update with a new name.'
			Tag tag = service.create(project, 'tag1', 'description1', Color.Red)
			tag = service.update(tag.id, project, 'new name')

		then: 'The updated tag is returned with the new name.'
			tag.name == 'new name'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test update description'() {
		when: 'Calling update with a new description.'
			Tag tag = service.create(project, 'tag1', 'description1', Color.Red)
			tag = service.update(tag.id, project, null, 'new description')

		then: 'The updated tag is returned with the new description.'
			tag.name == 'tag1'
			tag.description == 'new description'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test update color'() {
		when: 'Calling update with a new color.'
			Tag tag = service.create(project, 'tag1', 'description1', Color.Red)
			tag = service.update(tag.id, project, null, null, Color.Blue)

		then: 'The updated tag is returned with the new color.'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Blue
			tag.project == project
	}

	void 'Test delete'() {
		when: 'Calling delete passing a tag'
			Tag tag = service.create(project, 'tag1', 'description1', Color.Red)
			service.delete(tag.id, project)
			tag = service.get(Tag, 1l, project)

		then: 'The tag is deleted.'
			thrown EmptyResultException
	}

	void 'Test handleName'() {
		when: 'calling handleName with a non null, not empty value'
			List whereFilter = []
			Map params = [:]
			service.handleName('name', whereFilter, params)
		then: "the where filter is populated, and it's corresponding params"
			whereFilter == ['t.name like :name']
			params == [name: '%name%']
	}

	void 'Test handleName null'() {
		when: 'calling handleName with a null, or empty value'
			List whereFilter = []
			Map params = [:]
			service.handleName(null, whereFilter, params)
		then: 'No where filters are added, and no parameters are added.'
			whereFilter == []
			params == [:]
	}

	void 'Test handleDescription'() {
		when: 'calling handleDescription with a non null, not empty value'
			List whereFilter = []
			Map params = [:]
			service.handleDescription('description', whereFilter, params)
		then: "the where filter is populated, and it's corresponding params"
			whereFilter == ['t.description like :description']
			params == [description: '%description%']
	}

	void 'Test handleDescription null'() {
		when: 'calling handleDescription with a null, or empty value'
			List whereFilter = []
			Map params = [:]
			service.handleDescription(null, whereFilter, params)
		then: 'No where filters are added, and no parameters are added.'
			whereFilter == []
			params == [:]
	}

	void 'Test handleDateCreated'() {
		when: 'calling handleDateCreated with a non null value'
			List whereFilter = []
			Map params = [:]
			service.handleDateCreated(new Date().parse('d/M/yyyy', '1/1/2018'), whereFilter, params)
		then: "the where filter is populated, and it's corresponding params"
			whereFilter == ['(t.dateCreated >= :dateCreatedStart AND t.dateCreated < :dateCreatedEnd)']
			params == [
				dateCreatedStart: new Date().parse('d/M/yyyy', '1/1/2018'),
				dateCreatedEnd  : new Date().parse('d/M/yyyy', '1/1/2018') + 1
			]
	}

	void 'Test handleDateCreated null'() {
		when: 'calling handleDateCreated with a null value'
			List whereFilter = []
			Map params = [:]
			service.handleDateCreated(null, whereFilter, params)
		then: 'No where filters are added, and no parameters are added.'
			whereFilter == []
			params == [:]
	}

	void 'Test handleLastUpdated'() {
		when: 'calling handleLastUpdated with a non null value'
			List whereFilter = []
			Map params = [:]
			service.handleLastUpdated(new Date().parse('d/M/yyyy', '1/1/2018'), whereFilter, params)
		then: "the where filter is populated, and it's corresponding params"
			whereFilter == ['(t.lastUpdated >= :lastUpdatedStart AND t.lastUpdated < :lastUpdatedEnd)']
			params == [
				lastUpdatedStart: new Date().parse('d/M/yyyy', '1/1/2018'),
				lastUpdatedEnd  : new Date().parse('d/M/yyyy', '1/1/2018') + 1
			]
	}

	void 'Test handleLastUpdated null'() {
		when: 'calling handleLastUpdated with a null value'
			List whereFilter = []
			Map params = [:]
			service.handleLastUpdated(null, whereFilter, params)
		then: 'No where filters are added, and no parameters are added.'
			whereFilter == []
			params == [:]
	}

	void 'Test handleMoveIds'() {
		when: 'calling handleMoveIds with a non null values'
			List whereFilter = []
			Map params = [:]
			String joins = service.handleMoveIds([1], 1, whereFilter, params)
		then: "the where filter is populated, it's corresponding params are populated, and the join query is returned."
			joins.stripIndent() == """
				LEFT JOIN tl.asset a
				LEFT JOIN a.moveBundle mb
				LEFT OUTER JOIN mb.moveEvent me
			""".stripIndent()
			whereFilter == ['mb.id in (:moveBundleIds)', 'me.id = :moveEventId']
			params == [moveBundleIds:[1], moveEventId:1]
	}

	void 'Test handleMoveIds moveEventId null'() {
		when: 'calling handleMoveIds with, moveEventId null, and moveBundleId non null values'
			List whereFilter = []
			Map params = [:]
			String joins = service.handleMoveIds([1], null, whereFilter, params)
		then: "the where filter is populated, it's corresponding params are populated, and the join query is returned."
			joins.stripIndent() == """
				LEFT JOIN tl.asset a
				LEFT JOIN a.moveBundle mb
				LEFT OUTER JOIN mb.moveEvent me
			""".stripIndent()
			whereFilter == ['mb.id in (:moveBundleIds)']
			params == [moveBundleIds:[1]]
	}

	void 'Test handleMoveIds moveBundleId null'() {
		when: 'calling handleMoveIds with, moveEventId non null, and moveBundleId null values'
			List whereFilter = []
			Map params = [:]
			String joins = service.handleMoveIds(null, 1, whereFilter, params)
		then: "the where filter is populated, it's corresponding params are populated, and the join query is returned."
			joins.stripIndent() == """
				LEFT JOIN tl.asset a
				LEFT JOIN a.moveBundle mb
				LEFT OUTER JOIN mb.moveEvent me
			""".stripIndent()
			whereFilter == ['me.id = :moveEventId']
			params == [moveEventId:1]
	}


	void 'Test handleMoveIds both null'() {
		when: 'calling handleMoveIds with, both moveEventId and moveBundleId with null values values'
			List whereFilter = []
			Map params = [:]
			String joins = service.handleMoveIds(null, null, whereFilter, params)
		then: 'No where filters are added, no params added, and an empty string is returned for the join query.'
			joins == ''
			whereFilter == []
			params == [:]
	}
}
