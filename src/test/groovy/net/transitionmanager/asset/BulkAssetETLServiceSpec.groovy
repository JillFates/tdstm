package net.transitionmanager.asset

import com.tdsops.tm.enums.ControlType
import com.tdsops.tm.enums.domain.AssetClass
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import net.transitionmanager.exception.LogicException
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.tag.TagAssetService
import spock.lang.Specification
import spock.lang.Unroll

class BulkAssetETLServiceSpec extends Specification implements ServiceUnitTest<BulkAssetETLService>, DataTest, GrailsWebUnitTest {

	void setup() {
		mockDomains(AssetEntity, Person, MoveBundle, Room, Rack, TagAsset)
		List<Tag> tags = [new Tag(name: 'tag it'), new Tag(name: 'tag it again')]
		service.tagAssetService = [getTags: { Project currentProject, Long assetId -> tags }] as TagAssetService
	}

	@Unroll
	void 'test convertValues'() {

		given: 'a project to stand in as the current project and a domain class not in Assets.'
			Project project = new Project()


		expect: ''
			service.convertValues(value, fieldSpec, project, 1) == convertedValue

		where:
			value                                                                                  | fieldSpec                                                          || convertedValue
			new AssetEntity(assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', priority: 2) | [control: ControlType.STRING.value()]                              || [id: null, name: 'ACMEVMPROD01']
			new Person(firstName: 'first', middleName: 'middle', lastName: 'last')                 | [control: ControlType.STRING.value()]                              || [id: null, name: 'first middle last']
			new MoveBundle(name: 'bundleName')                                                     | [control: ControlType.STRING.value()]                              || [id: null, name: 'bundleName']
			new Room(location: 'location', roomName: 'roomName')                                   | [control: ControlType.STRING.value()]                              || [id: null, name: 'location / roomName']
			new Rack(tag: 'the tag')                                                               | [control: ControlType.STRING.value()]                              || [id: null, name: 'the tag']
			new Project(projectCode: "42")                                                         | [control: ControlType.STRING.value()]                              || [id: null, name: '42']
			[new TagAsset()] as Collection                                                         | [control: ControlType.STRING.value()]                              || ['tag it', 'tag it again']
			[] as Collection                                                                       | [control: ControlType.STRING.value()]                              || []
			1                                                                                      | [control: ControlType.STRING.value()]                              || 1
			1L                                                                                     | [control: ControlType.STRING.value()]                              || 1L
			true                                                                                   | [control: ControlType.STRING.value()]                              || true
			Date.parse("yyyy-MM-dd hh:mm:ss", "2014-04-03 1:23:45")                                | [control: ControlType.STRING.value()]                              || Date.parse("yyyy-MM-dd hh:mm:ss", "2014-04-03 1:23:45")
			'SomeString'                                                                           | [control: ControlType.STRING.value()]                              || 'SomeString'
			'42'                                                                                   | [control: ControlType.NUMBER.value(), constraints: [precision: 0]] || new BigInteger('42')
			'42.42'                                                                                | [control: ControlType.NUMBER.value(), constraints: [precision: 2]] || new BigDecimal('42.42')
			ControlType.STRING                                                                     | [control: ControlType.STRING.value()]                              || ControlType.STRING.name()
			null                                                                                   | [control: ControlType.STRING.value()]                              || null

	}

	@Unroll
	void 'test convertValues with exceptions'() {

		setup: 'a project to stand in as the current project '
			Project project = new Project()

		when: 'calling convert with values that should cause exceptions'
			service.convertValues(value, fieldSpec, project, 1)
		then: 'exceptions are thrown'
			thrown exception

		where:
			value                        | fieldSpec                             || exception
			[new Person()] as Collection | [control: ControlType.STRING.value()] || LogicException
			[1, 2, 3]                    | [control: ControlType.STRING.value()] || LogicException

	}

}
