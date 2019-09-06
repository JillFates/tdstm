package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class ArchitectureGraphServiceIntegrationSpec extends Specification {
	@Shared
	ArchitectureGraphService architectureGraphService


	void 'test buildArchitectureGraph'() {

		setup: 'given'
			Set assetsList = [] as Set
			Set dependencyList = [] as Set
			List<Long> assets = [114072l]
			Integer level = 4

		when: 'calling buildArchitectureGraph'
			architectureGraphService.buildArchitectureGraph(assets, level, assetsList, dependencyList)
		then: 'a list of graph nodes, as a list of maps is returned.'


			assetsList[0].criticality == 'Important'
			assetsList[0].assetName == 'Exchange 2013 Test'
			assetsList[0].model == null
			assetsList[0].id == 114072
			assetsList[0].assetClass == AssetClass.APPLICATION
			assetsList[0].assetType == 'Application'


			assetsList[1].criticality == null
			assetsList[1].assetName == 'Mailserver03'
			assetsList[1].model.modelName == 'ProLiant DL580 G7'
			assetsList[1].id == 113955
			assetsList[1].assetClass == AssetClass.DEVICE
			assetsList[1].assetType == 'Server'


			assetsList[2].criticality == null
			assetsList[2].assetName == 'Mailserver04'
			assetsList[2].model.modelName == 'ProLiant DL580 G7'
			assetsList[2].id == 113956
			assetsList[2].assetClass == AssetClass.DEVICE
			assetsList[2].assetType == 'Server'


			dependencyList[0] == [
				dependentId     : 113955,
				isStatusResolved: true,
				id              : 41929,
				assetId         : 114072,
				status          : 'Validated',
				isFuture        : false
			]

			dependencyList[1] == [
				dependentId     : 113956,
				isStatusResolved: true,
				id              : 41930,
				assetId         : 114072,
				status          : 'Validated',
				isFuture        : false
			]
	}
}
