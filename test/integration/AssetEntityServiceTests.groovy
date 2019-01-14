import com.tds.asset.Application
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.Color
import grails.test.spock.IntegrationSpec
import net.transitionmanager.command.CloneAssetCommand
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.TagAssetService
import spock.lang.See

class AssetEntityServiceTests extends IntegrationSpec {

	AssetEntityService assetEntityService
	TagAssetService tagAssetService

	private AssetTestHelper assetHelper = new AssetTestHelper()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private MoveBundleTestHelper moveBundleHelper = new MoveBundleTestHelper()

	void "1. test Entity Info Method"() {
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
			hasAssetOptionInList(data['dependencyType'], 'Runs On')

		and: "dependencyStatus contains 'Validated'"
			hasAssetOptionInList(data['dependencyStatus'], 'Validated')
	}

	void "2. test get dependency types"() {
		when:
			def data = assetEntityService.getDependencyTypes()

		then:
			data.size() > 1
		and: "dependencyType contains 'Runs On'"
			hasAssetOptionInList(data, 'Runs On')
	}

	void "3. test get dependency statuses"() {
		when:
		def data = assetEntityService.getDependencyStatuses()

		then:
		data.size() > 1

		and: "dependencyStatus contains 'Validated'"
		hasAssetOptionInList(data, 'Validated')
	}

	void "4. test parse params for dependecy asset ids"() {
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

	// TODO - This method is too tightly coupled with the implemetation and needs to refactored
	void "5. test getAppCustomQuery with different app preference values"(){
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

		/*
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
		*/

		when: "Some other property is given."
			queries = assetEntityService.getAppCustomQuery([1: "someProperty", 2: "someOtherProperty"])

		then: "The property is pulled from the asset_entity table."
			queries["query"] == "ae.some_property AS someProperty," +
								"ae.some_other_property AS someOtherProperty,"

		and: "No additional table is joined"
			queries["joinQuery"] == ""
	}

	@See('TM-11480')
	void '6. Test cloning of assets'() {
		setup: 'Create a test project and person'
			Project project = projectHelper.createProject()
			Person person = personHelper.createPerson()
			List<String> errors = []
			boolean cloneDependencies = true
			String assetName = 'clone asset name'
		when: 'an asset of type application is created'
			Application app = assetHelper.createApplication(person, project)
			assert app
		and: 'some tags are added to the asset'
			Tag tag1 = new Tag(name: 'first tag', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
			Tag tag2 = new Tag(name: 'second tag', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
			Tag tag3 = new Tag(name: 'third tag', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
			tagAssetService.applyTags(project, [tag2.id, tag3.id, tag1.id], app.id)
			app.refresh() // needed to refresh because of Grails test transaction caching entities
		and: 'the clone method is called'
			Long newAssetId = assetEntityService.clone(project, new CloneAssetCommand(assetId: app.id, name: assetName, cloneDependencies: cloneDependencies), errors)
		then: 'a new asset id should be returned'
			newAssetId
		and: 'there should be no errors reported'
			!errors
		and: 'the cloned asset should be able to be retrieved'
			Application clone = Application.read(newAssetId)
			clone.refresh()
		and: 'the cloned asset name should be set'
			assetName == clone.assetName
		and: 'the cloned asset should have three tags'
			clone.tagAssets.size() == 3
		and: 'other properties should match the original asset'
			List<String> props = ['appVendor', 'appVersion', 'sme', 'sme2']
			for (prop in props) {
				assert app[prop] == clone[prop]
			}
	}

	void '7. Test getAssetSummary under different scenarios'() {
		setup: 'Create two projects, a person and some MoveBundles'
			Project project1 = projectHelper.createProject()
			Project project2 = projectHelper.createProject()
			Person person = personHelper.createPerson()
			MoveBundle planningBundle1 = moveBundleHelper.createBundle(project1)
			MoveBundle planningBundle2 = moveBundleHelper.createBundle(project2)
			MoveBundle nonPlanningBundle1 = moveBundleHelper.createBundle(project1, null, false)
			List bundlesAndProjects = [
				[project: project1, moveBundle: planningBundle1],
				[project: project1, moveBundle: nonPlanningBundle1],
				[project: project2, moveBundle: planningBundle2]
			]

		when: 'requesting the Asset Summary and the project has no assets'
			Map assetSummaryProject1 = assetEntityService.getAssetSummary(project1, true)
		then: 'The summary list is empty'
			assetSummaryProject1['assetSummaryList'].size() == 0
		and: 'the total number of assets is zero'
			assetSummaryProject1['totalServer'] == 0

		when: 'requesting the Asset Summary for a project with no bundles'
			Map assetSummaryProject2 = assetEntityService.getAssetSummary(project2, true)
		then: 'the summary list is empty'
			assetSummaryProject2['assetSummaryList'].size() == 0
		and: 'the total number of assets is zero'
			assetSummaryProject2['totalServer'] == 0

		when: 'requesting the Asset Summary for a project with planning and non-planning assets (justPlanning set to true)'
			Map planningDeviceParams = ['moveBundle': planningBundle1]
			Map nonPlanningDeviceParams = ['moveBundle': nonPlanningBundle1]
			bundlesAndProjects.each { Map bundleInfo ->
				Project project = bundleInfo['project']
				MoveBundle moveBundle = bundleInfo['moveBundle']
				assetHelper.createApplication(person, project, moveBundle)
				assetHelper.createDatabase(project, moveBundle)
				assetHelper.createStorage(project, moveBundle)
				assetHelper.createDevice(project, moveBundle, AssetType.BLADE)
				assetHelper.createDevice(project, moveBundle, AssetType.VM)
			}
			assetSummaryProject1 = assetEntityService.getAssetSummary(project1, true)
		then: 'there should be one element and totals should be 1, except for servers, which should be 2'
			assetSummaryProject1['assetSummaryList'].size() == 1
			assetSummaryProject1['totalServer'] == 2
			assetSummaryProject1['totalApplication'] == 1
			assetSummaryProject1['totalDatabase'] == 1
			assetSummaryProject1['totalPhysical'] == 1
			assetSummaryProject1['totalFiles'] == 1

		when: 'retrieving the Asset Summary Table for planning and non-planning bundles'
			assetSummaryProject1 = assetEntityService.getAssetSummary(project1, false)
		then: 'there should be 3 bundles and the totals should be 2, except for servers, which should be 4'
			assetSummaryProject1['assetSummaryList'].size() == 2
			assetSummaryProject1['totalServer'] == 4
			assetSummaryProject1['totalApplication'] == 2
			assetSummaryProject1['totalDatabase'] == 2
			assetSummaryProject1['totalPhysical'] == 2
			assetSummaryProject1['totalFiles'] == 2

	}

	// Helper functions ////////////////////
	protected boolean hasAssetOptionInList(List list, String key) {
		list.contains(key)
	}

}
