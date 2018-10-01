package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.bulk.change.BulkChangeMoveBundle
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class BulkChangeMoveBundleIntegrationSpec extends IntegrationSpec {
	BulkChangeMoveBundle bulkChangeMoveBundleService

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
		setup: 'given a new move bundle, not yet assigned to assets'
			MoveBundle moveBundle3 = moveBundleTestHelper.createBundle(project, null)
		when: 'bulk replacing tags on a list of devices, with the new tags'
			bulkChangeMoveBundleService.bulkReplace(moveBundle3, '', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, have the new moveBundle, but not the old one'
			asset1.refresh().moveBundle.id == moveBundle3.id
			asset2.refresh().moveBundle.id == moveBundle3.id
	}

	void 'test replace filter query'() {
		setup: 'given a new move bundle, not yet assigned to assets, and an AssetFilterQuery'
			MoveBundle moveBundle4 = moveBundleTestHelper.createBundle(project, null)
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk replacing tags for assets from the AssetFilterQuery, with the new tags'
			bulkChangeMoveBundleService.bulkReplace(moveBundle4, '', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, have the new moveBundle, but not the old one'
			asset1.refresh().moveBundle.id == moveBundle4.id
			asset2.refresh().moveBundle.id == moveBundle4.id
	}

	void 'test coerceBulkValue'() {
		when: 'coercing a string value that contains a moveBundle id to a moveBundle'
			def value = bulkChangeMoveBundleService.coerceBulkValue(project, "${moveBundle.id}")

		then: 'the moveBundle is returned'
			value.id == moveBundle.id
	}

	void 'test coerceBulkValue move bundle from another project'() {
		when: 'coercing and '
			bulkChangeMoveBundleService.coerceBulkValue(project, "${moveBundle2.id}")

		then: 'an EmptyResultException is returned'
			thrown EmptyResultException
	}
}