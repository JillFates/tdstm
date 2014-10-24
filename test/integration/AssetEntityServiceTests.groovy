import grails.test.*

import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.AssetOptions

class AssetEntityServiceTests extends GrailsUnitTestCase {
	
	def assetEntityService

	def project    

	void testEntityInfoMethod() {

		def keys = ['servers', 'applications', 'dbs', 'files', 'networks', 'dependencyType', 'dependencyStatus']
		def data = assetEntityService.entityInfo(project)

		keys.each {
			assertTrue "$it exists in results", data.containsKey(it)
			println "$it has ${data[it].size()} assets"
			assertTrue "$it contains assets", data[it].size() > 0
		}

		assertTrue "dependencyType contains 'Runs On'", findAssetOptionInList(data['dependencyType'], AssetOptions.AssetOptionsType.DEPENDENCY_TYPE, 'Runs On') != null
		assertTrue "dependencyStatus contains 'Validated'", findAssetOptionInList(data['dependencyStatus'], AssetOptions.AssetOptionsType.DEPENDENCY_STATUS, 'Validated') != null
	}

	void testGetDependencyTypes() {
		def data = assetEntityService.getDependencyTypes()
		println "data=$data"
		assertTrue "dependencyType contains 'Runs On'", findAssetOptionInList(data, AssetOptions.AssetOptionsType.DEPENDENCY_TYPE, 'Runs On') != null
		assertTrue "Has multiple values", data.size() > 1
	}

	void testGetDependencyStatuses() {
		def data = assetEntityService.getDependencyStatuses()
		println "data=$data"
		assertTrue "dependencyStatus contains 'Validated'", findAssetOptionInList(data, AssetOptions.AssetOptionsType.DEPENDENCY_STATUS, 'Validated') != null
		assertTrue "Has multiple values", data.size() > 1
	}


	void testParseParamsForDependencyAssetIds() {
		Map params = [
			name:'test',
			'asset_support_1': 100,
			'asset_support_2': 200,
			'asset_support_0': 10,
			'asset_support_-1': 20,
			'asset_support_-3': 30,
			'asset_dependent_3': 300,
			'asset_dependent_0': 40,
			'asset_dependent_-102': 50,
			'asset_supportelse_1': 999,
			'somethingelse': 'blah'
			 ]

		def (existingDep, newDep) = assetEntityService.parseParamsForDependencyAssetIds('support', params)

		assertEquals 'Suport existing', 2, existingDep.size()
		assertEquals 'Support 1', 100,  existingDep.get(1)
		assertEquals 'Support 2', 200,  existingDep.get(2)
		assertEquals 'Support new', 3, newDep.size()
		assertEquals 'Support 0', 20, newDep.get(0)
		assertEquals 'Support -1', 20, newDep.get(-1)
		assertEquals 'Support -3', 20, newDep.get(-3)

		(existingDep, newDep) = assetEntityService.parseParamsForDependencyAssetIds('dependent', params)

		assertEquals 'Dependent new', 2, newDep.size()
		assertEquals 'Dependent existing', 1, existingDep.size()
		assertEquals 'Dependent 3', 300,  existingDep.get(3)
		assertEquals 'Dependent 2', 200,  existingDep.get(2)
		assertEquals 'Dependent new', 3, newDep.size()
		assertEquals 'Dependent 0', 40, newDep.get(0)
		assertEquals 'Dependent -102', 50, newDep.get(-1)

		assertFalse 'Support else existing',  existingDep.values.contains(999)
		assertFalse 'Support else new',  newDep.values.contains(999)

	}

	// Helper function
	boolean findAssetOptionInList(List list, AssetOptions.AssetOptionsType type, String key) {
		list.find { it.type == type && it.value == key }
	}


    protected void setUp() {
        super.setUp()

		assetEntityService = new AssetEntityService()

		project = Project.read(2445)
		assertNotNull 'Loading the Marketing Demo Project', project
	}

    protected void tearDown() {
        super.tearDown()
    }

}