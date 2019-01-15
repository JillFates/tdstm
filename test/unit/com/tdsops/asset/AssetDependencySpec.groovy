package com.tdsops.asset

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.validators.CustomValidators
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.Project
import net.transitionmanager.service.AssetOptionsService
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@TestFor(AssetDependency)
@Mock([AssetDependency, AssetEntity, AssetOptions])
class AssetDependencySpec extends Specification {

	Project GMDEMO

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}

		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	def setup() {
		ApplicationContextHolder.instance.applicationContext = applicationContext
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		GroovyMock(CustomValidators, global: true)
		CustomValidators.optionsClosure >> { AssetOptions.AssetOptionsType type ->
			return []
		}
	}

	void 'test asset cannot be null'() {
		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: null,
				dependent: mockAsset(152255l, 'ACME Data Center', AssetClass.APPLICATION),
			)

		then:
			!assetDependency.validate(['asset'])
			assetDependency.errors['asset'].code == 'nullable'
	}

	void 'test dependent cannot be null'() {
		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: mockAsset(152255l, 'ACME Data Center', AssetClass.APPLICATION),
				dependent: null,
			)

		then:
			!assetDependency.validate(['dependent'])
			assetDependency.errors['dependent'].code == 'nullable'
	}

	void 'test asset and dependent cannot be same domain'() {
		given:
			AssetEntity  assetEntity = mockAsset(152255l, 'ACME Data Center', AssetClass.APPLICATION)
		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: assetEntity,
				dependent: assetEntity,
			)

		then:
			assetDependency.validate(['asset'])

		and:
			!assetDependency.validate(['dependent'])
			assetDependency.errors['dependent'].code == 'invalid.dependent'
	}

	private AssetEntity mockAsset(Long id,
								  String assetName,
								  AssetClass assetClass = AssetClass.DEVICE,
								  Project project = null) {
		AssetEntity mock = Mock()
		mock.getId() >> id
		mock.getAssetClass() >> assetClass
		mock.getAssetName() >> assetName
		mock.getProject() >> (project ?: GMDEMO)
		return mock
	}

}
