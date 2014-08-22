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