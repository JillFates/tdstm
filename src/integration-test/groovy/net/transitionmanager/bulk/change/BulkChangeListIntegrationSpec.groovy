package net.transitionmanager.bulk.change

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.common.FileSystemService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.ApplicationTestHelper

@Integration
@Rollback
class BulkChangeListIntegrationSpec extends Specification  {

	@Shared
	ApplicationTestHelper applicationTestHelper = new ApplicationTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project

	@Shared
	Project otherProject

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
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)
		device = applicationTestHelper.createApplication(AssetClass.DEVICE, project, moveBundle)
		device2 = applicationTestHelper.createApplication(AssetClass.DEVICE, project, moveBundle)
		device3 = applicationTestHelper.createApplication(AssetClass.DEVICE, otherProject, moveBundle2)
	}

	void 'test replace'() {
		when: 'bulk replacing a list value with a new one'
			BulkChangeList.replace(Application, 'new value', 'custom9', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the list value has the new value'
			asset1.refresh().custom9 == 'new value'
			asset2.refresh().custom9 == 'new value'
	}

	void 'test replace with filter query'() {
		setup: 'given an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk replacing a list value with a new one, using a query filter'
			BulkChangeList.replace(Application, 'new value', 'custom9', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the list value has the new value'
			asset1.refresh().custom9 == 'new value'
			asset2.refresh().custom9 == 'new value'
	}

	void 'test clear'() {
		when: 'bulk clear a list field'
			BulkChangeList.clear(Application, null, 'custom9', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, list field is cleared'
			asset1.refresh().custom9 == ''
			asset2.refresh().custom9 == ''
	}

	void 'test clear with filter query'() {
		setup: 'bulk clear a list field with an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk clearing a list field using a query filter'
			BulkChangeList.clear(Application, '', 'custom9', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, list field is cleared'
			asset1.refresh().custom9 == ''
			asset2.refresh().custom9 == ''
	}
}