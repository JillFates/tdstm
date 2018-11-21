package net.transitionmanager.bulk.change


import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.FileSystemService
import spock.lang.Shared
import test.helper.ApplicationTestHelper

class BulkChangeReferenceIntegrationSpec extends IntegrationSpec {
	@Shared
	ApplicationTestHelper applicationTestHelper = new ApplicationTestHelper()

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
	MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2= moveBundleTestHelper.createBundle(otherProject, null)

	@Shared
	MoveBundle moveBundle3 = moveBundleTestHelper.createBundle(project, null)

	@Shared
	MoveBundle moveBundle4 = moveBundleTestHelper.createBundle(project, null)

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
		device = applicationTestHelper.createApplication(AssetClass.DEVICE, project, moveBundle)
		device2 = applicationTestHelper.createApplication(AssetClass.DEVICE, project, moveBundle)
		device3 = applicationTestHelper.createApplication(AssetClass.DEVICE, otherProject, moveBundle2)
	}

	void 'test replace'() {
		when: 'bulk replacing tags on a list of devices, with the new tags'
			BulkChangeReference.replace(AssetEntity, moveBundle3, 'moveBundle', [device.id, device2.id])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, have the new moveBundle, but not the old one'
			asset1.refresh().moveBundle.id == moveBundle3.id
			asset2.refresh().moveBundle.id == moveBundle3.id
	}

	void 'test replace filter query'() {
		setup: 'given a new move bundle, not yet assigned to assets, and an AssetFilterQuery'
			Map params = [project: project, assetClasses: [AssetClass.APPLICATION, AssetClass.DEVICE]]
			String query = """
				SELECT AE.id
				FROM AssetEntity AE
				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
			"""
		when: 'bulk replacing tags for assets from the AssetFilterQuery, with the new tags'
			BulkChangeReference.replace(AssetEntity, moveBundle4, 'moveBundle', [], [query: query, params: params])
			AssetEntity asset1 = AssetEntity.get(device.id)
			AssetEntity asset2 = AssetEntity.get(device2.id)
		then: 'the assets, have the new moveBundle, but not the old one'
			asset1.refresh().moveBundle.id == moveBundle4.id
			asset2.refresh().moveBundle.id == moveBundle4.id
	}

	void 'test coerceBulkValue'() {
		when: 'coercing a string value that contains a moveBundle id to a moveBundle'
			def value = BulkChangeReference.coerceBulkValue(project, "${moveBundle.id}", 'moveBundle', AssetEntity.class)

		then: 'the moveBundle is returned'
			value.id == moveBundle.id
	}

	void 'test coerceBulkValue move bundle from another project'() {
		when: 'coercing and '
			def value = BulkChangeReference.coerceBulkValue(project, "${moveBundle2.id}", 'moveBundle', AssetEntity.class)

		then: 'an EmptyResultException is returned'
			!value
	}
}