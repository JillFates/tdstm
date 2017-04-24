import com.tdsops.tm.domain.AssetEntityHelper
import grails.test.spock.IntegrationSpec
import com.tds.asset.Application
import com.tds.asset.Database
import net.transitionmanager.domain.Project
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


	void "test getAppCustomQuery with different app preference values"(){
		when: "No app pref are given."
			Map queries = assetEntityService.getAppCustomQuery([:])
		
		then: "No fields are requested."
			queries["query"] == ""
		
		and: "No additional join is required."
			queries["joinQuery"] == ""

		when: "Empty preference map is passed."
			queries = assetEntityService.getAppCustomQuery(null)

		then: "No fields are requested."
			queries["query"] == ""
		
		and: "No additional join is required."
			queries["joinQuery"] == ""

		when: "There's only a custom field."
			queries = assetEntityService.getAppCustomQuery([1: "custom10"])

		then: "The field is retieved from the asset_entity table."
			queries["query"] == "ae.custom10 AS custom10,"	
		
		and: "No additional join is required."
			queries["joinQuery"] == ""

		when: "Multiple values are given (sme and modifiedBy)."
			queries = assetEntityService.getAppCustomQuery([1: "sme", 2: "modifiedBy"])

		then: "The application retrieves the name of the individuals."
			queries["query"] == "CONCAT_WS(' ', p.first_name, p.middle_name, p.last_name) AS sme," +
								"CONCAT(CONCAT(p2.first_name, ' '), IFNULL(p2.last_name,'')) AS modifiedBy,"
		and: "The person table is queries twice."	
			queries["joinQuery"] == "\n LEFT OUTER JOIN person p ON p.person_id=a.sme_id \n" +
									"\n LEFT OUTER JOIN person p2 ON p2.person_id=ae.modified_by \n"

		when: "Multiple values are given (sme2, lastUpdated and event)."
			queries = assetEntityService.getAppCustomQuery([1: "sme2", 2: "lastUpdated", 3: "event"])

		then: "The application retrieves the name of the individual for sme2, the last_updated value and the id of the event."
			queries["query"] == "CONCAT_WS(' ', p1.first_name, p1.middle_name, p1.last_name) AS sme2," +
								"ee.last_updated AS lastUpdated," +
								"me.move_event_id AS event,"
		and: "The person, eav_entity and move_event tables are queried."
			queries["joinQuery"] == "\n LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id \n" +
									"\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n" +
									"\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id \n"

		when: "Multiple values are given (appOwner, appVendor and validation)."
			queries = assetEntityService.getAppCustomQuery([1: "appOwner", 2: "appVendor", 3: "validation"])

		then: "The application retrieves the name of the owner and the app_vendor field. Nothing is done for 'validation'."
			queries["query"] == "CONCAT_WS(' ', p3.first_name, p3.middle_name, p3.last_name) AS appOwner," +
								"a.app_vendor AS appVendor,"
		and: "Only one join (with the person table) is required."
			queries["joinQuery"] == "\n LEFT OUTER JOIN person p3 ON p3.person_id= ae.app_owner_id \n"

		when: "Multiple 'by' properties are requested."
			queries = assetEntityService.getAppCustomQuery([1: "shutdownBy", 2: "testingBy", 3: "startupBy"])

		then: "If the properties reference an individual, then the his name is retrieved."
			queries["query"] == "(IF(shutdown_by REGEXP '^[0-9]{1,}\$', TRIM( REPLACE( CONCAT(sdb.first_name,' '," +
								"sdb.middle_name, ' ', sdb.last_name), '  ', ' ')),a.shutdown_by)) as shutdownBy," +
								"(IF(testing_by REGEXP '^[0-9]{1,}\$', TRIM( REPLACE( CONCAT(teb.first_name,' '," +
								"teb.middle_name, ' ', teb.last_name), '  ', ' ')),a.testing_by)) as testingBy," +
								"(IF(startup_by REGEXP '^[0-9]{1,}\$', TRIM( REPLACE( CONCAT(sub.first_name,' '," +
								"sub.middle_name, ' ', sub.last_name), '  ', ' ')),a.startup_by)) as startupBy,"

		and: "The person table is queried three times"
			queries["joinQuery"] == "\n LEFT OUTER JOIN person sdb ON sdb.person_id=a.shutdown_by \n" +
									"\n LEFT OUTER JOIN person teb ON teb.person_id=a.testing_by \n" +
									"\n LEFT OUTER JOIN person sub ON sub.person_id=a.startup_by \n"

		when: "Some other property is given."
			queries = assetEntityService.getAppCustomQuery([1: "someProperty", 2: "someOtherProperty"])

		then: "The property is pulled from the asset_entity table."
			queries["query"] == "ae.some_property AS someProperty," +
								"ae.some_other_property AS someOtherProperty,"

	}

	// Helper functions ////////////////////
	protected boolean findAssetOptionInList(List list, String key) {
		list.contains(key)
	}

}
