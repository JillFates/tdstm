package net.transitionmanager.bulk.change

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.exception.InvalidParamException
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper

@Integration
@Rollback
class BulkChangeStringIntegrationSpec extends Specification{

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()

	@Shared
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

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

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundle2)
	}

	@See('TM-12334')
	void 'Test clear'() {
		when: 'clear is called with a list of assets'
			BulkChangeString.clear(Application.class, null, 'externalRefId', [device.id, device2.id, device3.id], null)

		then: 'the bulkClear function is invoked and specified field on assets will be null out'
			[device, device2, device3].each {
				it.refresh()
				null == it.externalRefId
			}
	}

	@See('TM-12334')
	void 'Test replace'() {
		setup:
			def externalRefId = RandomStringUtils.randomAlphanumeric(5)

		when: 'replace is called with a list of assets'
			BulkChangeString.replace(Application.class, externalRefId, 'externalRefId', [device.id, device2.id, device3.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device, device2, device3].each {
				it.refresh()
				externalRefId == it.externalRefId
			}

		when: 'replace is called with a null replacement value'
			BulkChangeString.replace(Application.class, null, 'externalRefId', [device.id, device2.id, device3.id], null)

		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
	}
}
