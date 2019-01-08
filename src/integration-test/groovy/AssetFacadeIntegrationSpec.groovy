import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.JsonUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import spock.lang.Specification
import test.helper.ProjectTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.AssetEntityTestHelper

@Integration
@Rollback
class AssetFacadeIntegrationSpec extends Specification {

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	Project project
	MoveBundle moveBundle
	AssetEntity assetEntity

	void setup() {
		project = projectTestHelper.createProject(null)
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
	}

	void "test setting an asset property should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting asset property value'
			assetFacade.shortName = 'test short name'
		then: 'the value should be able to be read'
			'test short name' == assetFacade.shortName
		and: 'facade project non java property should be a String instance'
			assetFacade.project instanceof String
	}

	void "test setting an asset integer property should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting asset property value'
			assetFacade.dependencyBundle = Integer.valueOf('1')
		then: 'the value should be able to be read'
			assetFacade.dependencyBundle instanceof Integer
	}

	void "test setting an asset date property should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
			Date now = new Date()
		when: 'setting asset property value'
			assetFacade.purchaseDate = now
		then: 'the value should be able to be read'
			assetFacade.purchaseDate
		and: 'and the value should be an instance of Date'
			now == assetFacade.purchaseDate
	}

	void "test setting an asset enum property should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting asset property value'
			assetFacade.assetClass = AssetClass.DEVICE
		then: 'the value should be able to be read'
			AssetClass.DEVICE == assetFacade.assetClass
		and: 'and the value should be an instance of AssetClass'
			assetFacade.assetClass instanceof AssetClass
	}

	void "test getting an asset non java type property should return the string representation"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		expect: 'when getting asset non java property the string representation must be returned'
			assetFacade.project instanceof String
	}

	void "test setting an asset non java type property should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting a non java type property should fail'
			assetFacade.manufacturer = new Manufacturer(name: 'test setting non java property')
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: manufacturer for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test setting an asset property when the facade is readonly should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, true)
		when: 'setting asset readonly property value'
			assetFacade.priority = 1
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: priority for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test setting an asset custom field should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, getDatabaseFieldSettings(), false)
		when: 'setting asset custom field value'
			assetFacade.'Network Interfaces' = 'Any'
		then: 'the value should be able to be read'
			'Any' == assetFacade.'Network Interfaces'
	}

	void "test setting an asset custom field value should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, getDatabaseFieldSettings(), true)
		when: 'setting asset readonly custom field value'
			assetFacade.'Cost Basis' = 'Any'
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: Cost Basis for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test getting asset unknown property should throw a MissingPropertyException"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, getDatabaseFieldSettings(), true)
		when: 'getting an asset unknown property'
			assetFacade.unknownField
		then: 'a MissingPropertyException should be thrown'
			MissingPropertyException e = thrown MissingPropertyException
			'No such property: unknownField' == e.message
	}

	void "test getting asset unknown custom field should throw a MissingPropertyException"() {
		setup: 'giving an asset with basic properties'
			AssetFacade assetFacade = new AssetFacade(assetEntity, getDatabaseFieldSettings(), true)
		when: 'getting an asset unknown custom field'
			assetFacade.'Unknown Custom Field'
		then: 'a MissingPropertyException should be thrown'
			MissingPropertyException e = thrown MissingPropertyException
			'No such property: Unknown Custom Field' == e.message
	}

	/**
	 * Returns a database type field settings representation for the test
	 * @return
	 */
	private Map<String, ?> getDatabaseFieldSettings() {
		String databaseFieldSettings = """
			{
				"DATABASE": {
					"domain": "DATABASE",
					"fields": [
						{
							"constraints": {
								"maxSize": 255,
								"minSize": 0,
								"required": 0
							},
							"control": "String",
							"default": "",
							"field": "custom1",
							"imp": "U",
							"label": "Network Interfaces",
							"order": 101,
							"shared": 0,
							"show": 0,
							"tip": "custom1",
							"udf": 1
						},
						{
							"constraints": {
								"maxSize": 255,
								"minSize": 0,
								"required": 0
							},
							"control": "String",
							"default": "",
							"field": "custom2",
							"imp": "U",
							"label": "Cost Basis",
							"order": 102,
							"shared": 0,
							"show": 0,
							"tip": "custom2",
							"udf": 1
						}
					]
				}
			}
			"""
		return JsonUtil.convertJsonToMap(databaseFieldSettings)
	}
}
