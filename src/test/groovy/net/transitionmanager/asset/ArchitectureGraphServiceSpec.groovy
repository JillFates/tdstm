package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import spock.lang.Specification

class ArchitectureGraphServiceSpec extends Specification implements ServiceUnitTest<ArchitectureGraphService>, DataTest, GrailsWebUnitTest {

	void setup() {
		mockDomain([AssetEntity])
		service.deviceService = new DeviceService()
	}

	void 'test createGraphNodes'() {

		setup: 'given a root Asset and an asset List.'
			AssetEntity rootAsset = new AssetEntity(assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', priority: 2)
			Set assetList = [
				[
					criticality: null,
					assetName  : '1test',
					model      : null,
					id         : null,
					assetClass : 'DEVICE',
					assetType  : null
				],
				[
					criticality: '',
					assetName  : 'QAE2E YNPmU App For E2E Created',
					model      : null, id: 152821,
					assetClass : 'APPLICATION',
					assetType  : 'Application'
				],
				[
					criticality: '',
					assetName  : 'TestingSomething',
					model      : null,
					id         : 165456,
					assetClass : 'APPLICATION',
					assetType  : 'Application'
				],
				[
					criticality: null,
					assetName  : 'App Testing 01',
					model      : null, id: 165388,
					assetClass : 'APPLICATION',
					assetType  : 'Application'
				]
			] as Set

		when: 'calling createGraphNodes'
			List graphNodes = service.createGraphNodes(assetList, rootAsset)
		then: 'a list of graph nodes, as a list of maps is returned.'
			graphNodes[0] == [
				id        : null,
				name      : '1test',
				type      : 'Other',
				assetClass: 'DEVICE',
				shape     : 'circle',
				size      : 150,
				title     : '1test',
				color     : 'red',
				parents   : [],
				children  : [],
				checked   : false,
				siblings  : []
			]

			graphNodes[1] == [
				id        : 152821,
				name      : 'QAE2E YNPmU App For E2E Created',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'QAE2E YNPmU App For E2E Created',
				color     : 'grey',
				parents   : [],
				children  : [],
				checked   : false,
				siblings  : []
			]

			graphNodes[2] == [
				id        : 165456,
				name      : 'TestingSomething',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'TestingSomething',
				color     : 'grey',
				parents   : [],
				children  : [],
				checked   : false,
				siblings  : []
			]

			graphNodes[3] == [
				id        : 165388,
				name      : 'App Testing 01',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'App Testing 01',
				color     : 'grey',
				parents   : [],
				children  : [],
				checked   : false,
				siblings  : []
			]
	}

	void 'test create graph links'() {
		setup: 'given a list of graph nodes, and a set of dependencies.'

			List<Map> graphNodes = [
				[
					id        : 0,
					name      : '1test',
					type      : 'Other',
					assetClass: 'DEVICE',
					shape     : 'circle',
					size      : 150,
					title     : '1test',
					color     : 'red',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 152821,
					name      : 'QAE2E YNPmU App For E2E Created',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'QAE2E YNPmU App For E2E Created',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 165456,
					name      : 'TestingSomething',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'TestingSomething',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 165388,
					name      : 'App Testing 01',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'App Testing 01',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				]
			]

			Set<Map> dependencyList = [
				[
					dependentId     : 165442,
					isStatusResolved: true,
					id              : 78283,
					assetId         : 152821,
					status          : 'Unknown',
					isFuture        : false
				], [
					dependentId     : 165442,
					isStatusResolved: true,
					id              : 78289,
					assetId         : 165456,
					status          : 'Unknown',
					isFuture        : false
				], [
					dependentId     : 152821,
					isStatusResolved: true,
					id              : 78282,
					assetId         : 165388,
					status          : 'Unknown',
					isFuture        : false
				]
			] as Set<Map>


		when: 'createGraphLinks is called'
			List graphLinks = service.createGraphLinks(dependencyList, graphNodes)
		then: 'a list of links is returned as a list of maps.'
			graphLinks[0] == [
				id           : 2,
				parentId     : 165388,
				childId      : 152821,
				child        : 1,
				parent       : 3,
				value        : 2,
				opacity      : 1,
				redundant    : false,
				mutual       : null,
				notApplicable: false,
				future       : false,
				validated    : false,
				questioned   : false,
				unresolved   : false
			]


	}


	void 'test addLinksToNodes'() {
		setup: 'given a list of graph nodes and a list of graph links.'

			List graphNodes = [
				[
					id        : 152820,
					name      : '1test',
					type      : 'Other',
					assetClass: 'DEVICE',
					shape     : 'circle',
					size      : 150,
					title     : '1test',
					color     : 'red',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 152821,
					name      : 'QAE2E YNPmU App For E2E Created',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'QAE2E YNPmU App For E2E Created',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 165456,
					name      : 'TestingSomething',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'TestingSomething',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				], [
					id        : 165388,
					name      : 'App Testing 01',
					type      : 'Application',
					assetClass: 'APPLICATION',
					shape     : 'circle',
					size      : 200,
					title     : 'App Testing 01',
					color     : 'grey',
					parents   : [],
					children  : [],
					checked   : false,
					siblings  : []
				]
			]

			List graphLinks = [
				[
					id           : 0,
					parentId     : 152821,
					childId      : 165442,
					child        : 0,
					parent       : 1,
					value        : 2,
					opacity      : 1,
					redundant    : false,
					mutual       : null,
					notApplicable: false,
					future       : false,
					validated    : false,
					questioned   : false,
					unresolved   : false
				],
				[
					id           : 1,
					parentId     : 165456,
					childId      : 165442,
					child        : 0,
					parent       : 2,
					value        : 2,
					opacity      : 1,
					redundant    : false,
					mutual       : null,
					notApplicable: false,
					future       : false,
					validated    : false,
					questioned   : false,
					unresolved   : false
				],
				[
					id           : 2,
					parentId     : 165388,
					childId      : 152821,
					child        : 1,
					parent       : 3,
					value        : 2,
					opacity      : 1,
					redundant    : false,
					mutual       : null,
					notApplicable: false,
					future       : false,
					validated    : false,
					questioned   : false,
					unresolved   : false
				]
			]


		when: 'addLinksToNodes is called'
			service.addLinksToNodes(graphLinks, graphNodes)
		then: 'the graph nodes are updated with parents/children'
			graphNodes[0] == [
				id        : 152820,
				name      : '1test',
				type      : 'Other',
				assetClass: 'DEVICE',
				shape     : 'circle',
				size      : 150,
				title     : '1test',
				color     : 'red',
				parents   : [0, 1],
				children  : [],
				checked   : false,
				siblings  : []
			]

			graphNodes[1] == [
				id        : 152821,
				name      : 'QAE2E YNPmU App For E2E Created',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'QAE2E YNPmU App For E2E Created',
				color     : 'grey',
				parents   : [2],
				children  : [0],
				checked   : false,
				siblings  : []
			]

			graphNodes[2] == [
				id        : 165456,
				name      : 'TestingSomething',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'TestingSomething',
				color     : 'grey',
				parents   : [],
				children  : [1],
				checked   : false,
				siblings  : []
			]

			graphNodes[3] == [
				id        : 165388,
				name      : 'App Testing 01',
				type      : 'Application',
				assetClass: 'APPLICATION',
				shape     : 'circle',
				size      : 200,
				title     : 'App Testing 01',
				color     : 'grey',
				parents   : [],
				children  : [2],
				checked   : false,
				siblings  : []
			]
	}
}
