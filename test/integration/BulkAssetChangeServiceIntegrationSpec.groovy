import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.EditCommand
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.service.BulkAssetChangeService
import net.transitionmanager.service.BulkChangeDateService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.TagAssetService
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

	void setup() {
		bulkAssetChangeService.dataviewService.projectService.customDomainService = [
				fieldToControlMapping: {Project currentProject -> [
						tagAssets: 'asset-tag-selector',
						retireDate: 'retireDate'
				]}
		] as CustomDomainService
		bulkAssetChangeService.tagAssetService = Mock(TagAssetService)
		bulkAssetChangeService.dataviewService = Mock(DataviewService)
		bulkAssetChangeService.bulkChangeDateService = Mock(BulkChangeDateService)
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
			assetIds: [device.id, device2.id]
		)
	}

	void 'Test bulkChange add'() {
		setup: 'given an edit command for adding tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'add', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkAdd function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkAdd(null, [device.id, device2.id], [:])
	}

	void 'Test bulkChange add all assets in filter'() {
		setup: 'given an edit command for adding tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'add', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.assetIds = []
			bulkChangeCommand.allAssets = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkAdd is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.dataviewService.getAssetIdsHql(project, 1, dataviewUserParamsCommand)
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkAdd(null, [], null)
	}

	void 'Test bulkChange clear'() {
		setup: 'given an edit command for clearing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'clear', value: "[]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkRemove function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkClear(null, [device.id, device2.id], [:])
	}

	void 'Test bulkChange clear, all assets in filter'() {
		setup: 'given an edit command for clearing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'clear', value: "[]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.assetIds = []
			bulkChangeCommand.allAssets = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkRemove is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.dataviewService.getAssetIdsHql(project, 1, dataviewUserParamsCommand)
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkClear(null, [], null)
	}

	void 'Test bulkChange replace'() {
		setup: 'given an edit command for replacing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'replace', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkReplace(null, [device.id, device2.id], [:])
	}

	void 'Test bulkChange replace, all assets in filter'() {
		setup: 'given an edit command for replacing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'replace', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.assetIds = []
			bulkChangeCommand.allAssets = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkReplace is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.dataviewService.getAssetIdsHql(project, 1, dataviewUserParamsCommand)
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkReplace(null, [], null)
	}

	void 'Test bulkChange remove'() {
		setup: 'given an edit command for removing tags, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'remove', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkRemove function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkRemove(null, [device.id, device2.id], [:])
	}

	void 'Test bulkChange remove, all assets in filter'() {
		setup: 'given an edit command for removing tags, where allAssets it true, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'remove', value: "[${tag1.id}, ${tag2.id}]")
			bulkChangeCommand.edits = [editCommand]
			bulkChangeCommand.assetIds = []
			bulkChangeCommand.allAssets = true

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'The sql for getting the assets is looked up, and bulkRemove is invoked with that query to filter assets.'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.dataviewService.getAssetIdsHql(project, 1, dataviewUserParamsCommand)
			1 * bulkAssetChangeService.tagAssetService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.tagAssetService.bulkRemove(null, [], null)
	}

	void 'Test bulkChange invalid action'() {
			setup: 'given an edit command for removing tags, where allAssets it true, and a bulk change command holding the edit'
				EditCommand editCommand = new EditCommand(fieldName: 'tagAssets', action: 'inaction', value: "[${tag1.id}, ${tag2.id}]")
				bulkChangeCommand.edits = [editCommand]
				bulkChangeCommand.assetIds = []
				bulkChangeCommand.allAssets = true

			when: 'bulk change is called with the bulk change command'
				bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

			then: 'an InvalidParameter Exception is thrown'
				thrown InvalidParamException
		}

	void 'Test date/time bulkChange replace'() {
		setup: 'given an edit command for replacing date/time, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'retireDate', action: 'replace', value: '2018-09-19')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkReplace function is invoked'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkChangeDateService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkChangeDateService.bulkReplace(null, 'retireDate', [device.id, device2.id], [:])
	}

	void 'Test date/time bulkChange clear'() {
		setup: 'given an edit command for clearing a standard field, and a bulk change command holding the edit'
			EditCommand editCommand = new EditCommand(fieldName: 'retireDate', action: 'clear', value: '')
			bulkChangeCommand.edits = [editCommand]

		when: 'bulk change is called with the bulk change command'
			bulkAssetChangeService.bulkChange(project, bulkChangeCommand)

		then: 'the bulkRemove function is invoked, with no tags specified'
			bulkChangeCommand.validate()
			1 * bulkAssetChangeService.bulkChangeDateService.coerceBulkValue(project, editCommand.value)
			1 * bulkAssetChangeService.bulkChangeDateService.bulkClear('retireDate', [device.id, device2.id], [:])
	}
}
