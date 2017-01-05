import com.tdsops.tm.domain.AssetEntityHelper
import com.tds.asset.Database
import com.tds.asset.Application
import grails.test.spock.IntegrationSpec
import net.transitionmanager.service.AssetEntityService


class AssetEntityServiceTests extends IntegrationSpec {

	AssetEntityService assetEntityService

	void "test Entity Info Method"() {
		setup:
		def project = Project.read(2445)
		def keys = ['servers', 'applications', 'dbs', 'files', 'networks', 'dependencyType', 'dependencyStatus']

		when:
		def data = assetEntityService.entityInfo(project)

		then: "keys exists in results and contains assets"
		keys.each {
			data.containsKey(it) && data[it].size() > 0
		}

		and: "dependencyType contains 'Runs On'"
		findAssetOptionInList(data['dependencyType'], 'Runs On')

		and: "dependencyStatus contains 'Validated'"
		findAssetOptionInList(data['dependencyStatus'], 'Validated')
	}

	void "test get dependency types"() {
		when:
		def data = assetEntityService.getDependencyTypes()

		then:
		data.size() > 1

		and: "dependencyType contains 'Runs On'"
		findAssetOptionInList(data, 'Runs On')
	}

	void "test get dependency statuses"() {
		when:
		def data = assetEntityService.getDependencyStatuses()

		then:
		data.size() > 1

		and: "dependencyStatus contains 'Validated'"
		findAssetOptionInList(data, 'Validated')
	}


	void "test AssetEntityHelper getPropertyNameByHashReference"(){
		setup:
			Application app = new Application()
			Database db = new Database()
		expect:
			AssetEntityHelper.getPropertyNameByHashReference(app, "sme1") == "#SME1"
			AssetEntityHelper.getPropertyNameByHashReference(app, "#sme1") == "#SME1"
			AssetEntityHelper.getPropertyNameByHashReference(db, "#sme2") == null
			AssetEntityHelper.getPropertyNameByHashReference(null, "#sme2") == null
			AssetEntityHelper.getPropertyNameByHashReference(app, null) == null
			AssetEntityHelper.getPropertyNameByHashReference(app, "") == null
			AssetEntityHelper.getPropertyNameByHashReference(app, "#") == null
			AssetEntityHelper.getPropertyNameByHashReference(app, "#license") == "#license"
			AssetEntityHelper.getPropertyNameByHashReference(app, "#custom1") == "#custom1"
	}


	void "test parse params for dependecy asset ids"() {
		setup:
		Map params = [
				name                  : 'test',
				'asset_support_1'     : 100,
				'asset_support_2'     : 200,
				'asset_support_0'     : 10,
				'asset_support_-1'    : 20,
				'asset_support_-3'    : 30,
				'asset_dependent_3'   : 300,
				'asset_dependent_0'   : 40,
				'asset_dependent_-102': 50,
				'asset_supportelse_1' : 999,
				'somethingelse'       : 'blah'
		]

		when:
		def (existingDep, newDep) = assetEntityService.parseParamsForDependencyAssetIds('support', params)

		then: 'Suport existing'
		2 == existingDep.size()
		and: 'Support 1'
		100 == existingDep[1L]
		and: 'Support 2'
		200 == existingDep[2L]
		and: 'Support new'
		3 == newDep.size()
		and: 'Support 0'
		10 == newDep[0L]
		and: 'Support -1'
		20 == newDep[-1L]
		and: 'Support -3'
		30 == newDep[-3L]

		when:
		(existingDep, newDep) = assetEntityService.parseParamsForDependencyAssetIds('dependent', params)

		then: 'Dependent new'
		2 == newDep.size()
		and: 'Dependent existing'
		1 == existingDep.size()
		and: 'Dependent 3'
		300 == existingDep[3L]
		and: 'Dependent new'
		2 == newDep.size()
		and: 'Dependent 0'
		40 == newDep[0L]
		and: 'Dependent -102'
		50 == newDep[-102L]

		and: 'Support else existing'
		!existingDep.values().contains(999)
		and: 'Support else new'
		!newDep.values().contains(999)

	}

	// Helper functions ////////////////////
	protected boolean findAssetOptionInList(List list, String key) {
		list.contains(key)
	}

}
