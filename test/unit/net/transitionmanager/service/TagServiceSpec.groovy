package net.transitionmanager.service

import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.StringUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
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

		service.securityService = [getUserCurrentProject: { -> project }] as SecurityService
	}

	void 'Test create'() {
		when: 'Calling create with a valid name, decription, and color'
			Tag tag = service.create('tag1', 'description1', Color.Red)

		then: 'The tag is created and returned.'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test get'() {
		when: 'Calling get with an id of a tag'
			service.create('tag1', 'description1', Color.Red)
			Tag tag = service.get(1)

		then: 'The tag is returned'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test create missing name'() {
		when: 'Calling create with an invalid name parameter'
			Tag tag = service.create(null, 'description1', Color.Red)

		then: 'the tag will be returned with errors.'
			tag.errors.allErrors.collect { it.defaultMessage } == ['Property [{0}] of class [{1}] cannot be null']
	}

	void 'Test create missing description'() {
		when: 'Calling create with an invalid description parameter'
			Tag tag = service.create('tag1', null, Color.Red)

		then: 'the tag will be returned with errors.'
			tag.errors.allErrors.collect { it.defaultMessage } == ['Property [{0}] of class [{1}] cannot be null']
	}

	void 'Test create missing color'() {
		when: 'Calling create with an invalid color parameter'
			Tag tag = service.create('tag1', 'description1', null)

		then: 'the tag will be returned with errors.'
			tag.errors.allErrors.collect { it.defaultMessage } == ['Property [{0}] of class [{1}] cannot be null']
	}

	void 'Test update name'() {
		when: 'Calling update with a new name.'
			Tag tag = service.create('tag1', 'description1', Color.Red)
			tag = service.update(tag, 'new name')

		then: 'The updated tag is returned with the new name.'
			tag.name == 'new name'
			tag.description == 'description1'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test update description'() {
		when: 'Calling update with a new description.'
			Tag tag = service.create('tag1', 'description1', Color.Red)
			tag = service.update(tag, null, 'new description')

		then: 'The updated tag is returned with the new description.'
			tag.name == 'tag1'
			tag.description == 'new description'
			tag.color == Color.Red
			tag.project == project
	}

	void 'Test update color'() {
		when: 'Calling update with a new color.'
			Tag tag = service.create('tag1', 'description1', Color.Red)
			tag = service.update(tag, null, null, Color.Blue)

		then: 'The updated tag is returned with the new color.'
			tag.name == 'tag1'
			tag.description == 'description1'
			tag.color == Color.Blue
			tag.project == project
	}

	void 'Test delete'() {
		when: 'Calling delete passing a tag'
			Tag tag = service.create('tag1', 'description1', Color.Red)
			service.delete(tag)
			tag = service.get(1)

		then: 'The tag is deleted.'
			!tag
	}
}
