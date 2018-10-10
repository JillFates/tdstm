package net.transitionmanager.bulk.change

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import spock.lang.See
import spock.lang.Shared
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper

class BulkChangePersonIntegrationSpec extends IntegrationSpec {

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()

	@Shared
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

	@Shared
	PersonTestHelper personTestHelper = new PersonTestHelper()

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

	@See('TM-12334')
	void 'Test clear'() {
		when: 'clear is called with a list of assets'
			BulkChangePerson.clear(Application.class, null, 'modifiedBy', [device.id, device2.id, device3.id], null)

		then: 'the bulkClear function is invoked and specified field on assets will be null out'
		[device, device2, device3].each {
			it.refresh()
			null == it.modifiedBy
		}
	}

	@See('TM-12334')
	void 'Test replace'() {
		setup:
			def modifiedBy = personTestHelper.createPerson()

		when: 'replace is called with a list of assets'
			BulkChangePerson.replace(Application.class, modifiedBy, 'modifiedBy', [device.id, device2.id, device3.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device, device2, device3].each {
				it.refresh()
				modifiedBy == it.modifiedBy
			}

		when: 'replace is called with a null replacement value'
			BulkChangePerson.replace(Application.class, null, 'modifiedBy', [device.id, device2.id, device3.id], null)

		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
	}
}
