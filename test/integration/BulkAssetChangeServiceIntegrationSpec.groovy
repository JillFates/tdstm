import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.bulk.change.BulkChangeTag
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.EditCommand
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.service.BulkAssetChangeService
import net.transitionmanager.bulk.change.BulkChangeDate
import net.transitionmanager.bulk.change.BulkChangeNumber
import net.transitionmanager.bulk.change.BulkChangePerson
import net.transitionmanager.bulk.change.BulkChangeString
import net.transitionmanager.bulk.change.BulkChangeYesNo
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.TagAssetService
import spock.lang.See
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class BulkAssetChangeServiceIntegrationSpec extends IntegrationSpec {
	BulkAssetChangeService bulkAssetChangeService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	PersonTestHelper personHelper = new PersonTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Project otherProject = projectTestHelper.createProject()

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetEntity device3

	@Shared
	Map context

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagAsset tagAsset1

	@Shared
	TagAsset tagAsset2

	@Shared
	TagAsset tagAsset3

	@Shared
	TagAsset tagAsset4

	@Shared
	Date now

	@Shared
	DataviewUserParamsCommand dataviewUserParamsCommand

	@Shared
	BulkChangeCommand bulkChangeCommand

	@Shared
	Map params

	@Shared
	String query

	@Shared
	Person person

	void setup() {
		bulkAssetChangeService.dataviewService.projectService.customDomainService = [fieldToBulkChangeMapping: {
			Project p ->
				[
					'tagAssets'  : [
						control          : 'Tag',
						bulkChangeActions: [
							'add',
							'clear',
							'replace',
							'remove '
						]
					],
					retireDate   : [
						control          : 'Date',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
					externalRefId: [
						control          : 'String',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
					size         : [
						control          : 'Number',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
					appOwner     : [
						control          : 'Date',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
					validation   : [
						control          : 'String',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
					latency      : [
						control          : 'YesNo',
						bulkChangeActions: [
							'clear',
							'replace'
						]
					],
				]
		}] as CustomDomainService

		params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
		query = """
			SELECT AE.id
			FROM AssetEntity AE
			WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
		"""

		bulkAssetChangeService.tagAssetService = Mock(TagAssetService)
		bulkAssetChangeService.dataviewService = [
			getAssetIdsHql: { Project project, Long dataViewId, DataviewUserParamsCommand userParams -> [query: query, params: params] }
		] as DataviewService

		bulkAssetChangeService.bulkServiceMapping = [
			(TagAsset.class.name): GroovySpy(BulkChangeTag, global: true),
			(Date.class.name)    : GroovySpy(BulkChangeDate, global: true),
			'String'             : GroovySpy(BulkChangeString, global: true),
			(Integer.class.name) : GroovySpy(BulkChangeNumber, global: true),
			(Person.class.name)  : GroovySpy(BulkChangePerson, global: true),
			'YesNo'              : GroovySpy(BulkChangeYesNo, global: true),
		]

		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true, failOnError: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true, failOnError: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true, failOnError: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true, failOnError: true)

		now = TimeUtil.nowGMT().clearTime()

		dataviewUserParamsCommand = [
			sortDomain  : 'device',
			sortProperty: 'id',
			filters     : ['id': [1, 2, 3]]
		] as DataviewUserParamsCommand

		bulkChangeCommand = new BulkChangeCommand(
			userParams: dataviewUserParamsCommand,
			dataViewId: 1,
			ids: [device.id, device2.id],
			type: 'APPLICATION'
		)

		person = personHelper.createPerson(null, project.client, project)
		personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ADMIN}"], project, true)
	}

	void 'Test bulkChange add'() {
		setup: 'given an edit command for adding tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'add', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkAdd function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].add(AssetClass.domainClassFor(AssetClass.APPLICATION), [tag1.id, tag2.id], 'tagAssets', [device.id, device2.id], [:])
	}

	void 'Test bulkChange add all assets in filter'() {
		setup: 'given an edit command for adding tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'add', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkAdd is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].add(Application.class, [tag1.id, tag2.id], 'tagAssets', [], [query: query, params: params])
	}

	void 'Test bulkChange clear'() {
		setup: 'given an edit command for clearing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'clear', value: "[]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkRemove function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].clear(Application.class, [], 'tagAssets', [device.id, device2.id], [:])
	}

	void 'Test bulkChange clear, all assets in filter'() {
		setup: 'given an edit command for clearing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'clear', value: "[]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkRemove is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].clear(Application.class, [], 'tagAssets', [], [query: query, params: params])
	}

	void 'Test bulkChange replace'() {
		setup: 'given an edit command for replacing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'replace', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].replace(Application.class, [tag1.id, tag2.id], 'tagAssets', [device.id, device2.id], [:])
	}

	void 'Test bulkChange replace, all assets in filter'() {
		setup: 'given an edit command for replacing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'replace', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkReplace is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].replace(Application.class, [tag1.id, tag2.id], 'tagAssets', [], [query: query, params: params])
	}

	void 'Test bulkChange remove'() {
		setup: 'given an edit command for removing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'remove', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkRemove function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].remove(Application.class, [tag1.id, tag2.id], 'tagAssets', [device.id, device2.id], [:])
	}

	void 'Test bulkChange remove, all assets in filter'() {
		setup: 'given an edit command for removing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'remove', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkRemove is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(TagAsset.class.name)].remove(Application.class, [tag1.id, tag2.id], 'tagAssets', [], [query: query, params: params])
	}

	void 'Test bulkChange invalid field'() {
		setup: 'given an edit command for removing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tgAssets', action: 'inaction', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'an InvalidParameter Exception is thrown'
			thrown InvalidParamException
	}

	void 'Test bulkChange invalid action'() {
		setup: 'given an edit command for removing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'inaction', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.ids = []
			bulkChangeCommand.allIds = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'an InvalidParameter Exception is thrown'
			thrown InvalidParamException
	}

	@See('TM-12334')
	void 'Test date/time field bulkChange replace'() {
		setup: 'given an edit command for replacing date/time, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'retireDate', action: 'replace', value: '2018-09-19T01:00Z')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Date.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Date.class.name)].replace(Application.class, _ as Date, 'retireDate', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test date/time field bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'retireDate', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkClear function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Date.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Date.class.name)].clear(Application.class, null, 'retireDate', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test string field bulkChange replace'() {
		setup: 'given an edit command for replacing string field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'externalRefId', action: 'replace', value: '1abcd')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping['String'].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping['String'].replace(Application.class, '1abcd', 'externalRefId', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test string field bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'externalRefId', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkClear function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping['String'].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping['String'].clear(Application.class, null, 'externalRefId', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test numeric field bulkChange replace'() {
		setup: 'given an edit command for replacing numeric field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'size', action: 'replace', value: '1')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Integer.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Integer.class.name)].replace(Application.class, 1, 'size', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test numeric field bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'size', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkClear function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Integer.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Integer.class.name)].clear(Application.class, null, 'size', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test person field bulkChange replace'() {
		setup: 'given an edit command for replacing person field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'appOwner', action: 'replace', value: "${person.id}")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Person.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Person.class.name)].replace(Application.class, person, 'appOwner', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test person field bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'appOwner', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkClear function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping[(Person.class.name)].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping[(Person.class.name)].clear(Application.class, null, 'appOwner', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test yes/no field bulkChange replace'() {
		setup: 'given an edit command for replacing yes/no field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'latency', action: 'replace', value: 'yes')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping['YesNo'].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping['YesNo'].replace(Application, 'Y', 'latency', [device.id, device2.id], [:])
	}

	@See('TM-12334')
	void 'Test yes/no field bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'latency', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkClear function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkServiceMapping['YesNo'].coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkServiceMapping['YesNo'].clear(Application, 'N', 'latency', [device.id, device2.id], [:])
	}
}
