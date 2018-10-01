package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.bulk.change.BulkChangeList
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class BulkChangeListIntegrationSpec extends IntegrationSpec {
	BulkChangeList bulkChangeListService

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

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)
	}

	void 'test replace'() {
		when: 'bulk replacing a list value with a new one'
			bulkChangeListService.bulkReplace('new value', 'custom9', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the list value has the new value'
			asset1.refresh().custom9 == 'new value'
			asset2.refresh().custom9 == 'new value'
	}

	void 'test replace filter query'() {
		setup: 'given a new move bundle, not yet assigned to assets, and an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk replacing a list value with a new one, using a query filter'
			bulkChangeListService.bulkReplace('new value', 'custom9', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the list value has the new value'
			asset1.refresh().custom9 == 'new value'
			asset2.refresh().custom9 == 'new value'
	}

	void 'test clear'() {
		when: 'bulk clear a list field'
			bulkChangeListService.bulkClear(null, 'custom9', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, list field is cleared'
			asset1.refresh().custom9 == ''
			asset2.refresh().custom9 == ''
	}

	void 'test clear with value'() {
		when: 'bulk clear a list field, sending in a value'
			bulkChangeListService.bulkClear('one potato', 'custom9', [device.id, device2.id])
		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
	}

	void 'test clear filter query'() {
		setup: 'given a new move bundle, not yet assigned to assets, and an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk clearing a list filed using a query filter'
			bulkChangeListService.bulkClear('', 'custom9', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, list field is cleared'
			asset1.refresh().custom9 == ''
			asset2.refresh().custom9 == ''
	}

	void 'test coerceBulkValue'() {
		when: 'coercing a string value validating that it is in a list'
			def value = bulkChangeListService.coerceBulkValue(project, 'custom9', 'one potato', ['one potato', 'two potato'])

		then: 'the list value is returned'
			value == 'one potato'
	}

	void 'test coerceBulkValue value not is list of values'() {
		when: 'coercing a string value validating that it is in a list '
			bulkChangeListService.coerceBulkValue(project, 'custom9', 'no potato', ['one potato', 'two potato'])

		then: 'an InvalidParamException is returned'
			thrown InvalidParamException
	}
}