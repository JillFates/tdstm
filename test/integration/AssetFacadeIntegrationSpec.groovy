import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.JsonUtil
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import spock.lang.Specification
import test.helper.ProjectTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.AssetEntityTestHelper

class AssetFacadeIntegrationSpec extends Specification {

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()
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

	void "test setting an asset property should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting asset property value'
			assetFacade.shortName = 'test short name'
		then: 'the value should be able to be read'
			'test short name' == assetFacade.shortName
		and:
			project.toString() == assetFacade.project
	}

	void "test getting an asset non java type property should return the string representation"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		expect: 'when getting asset non java property the string representation must be returned'
			project.toString() == assetFacade.project
	}

	void "test setting an asset non java type property should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, false)
		when: 'setting a non java type property should fail'
			assetFacade.manufacturer = new Manufacturer(name: 'test setting non java property')
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: manufacturer for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test setting an asset property when the facade is readonly should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			AssetFacade assetFacade = new AssetFacade(assetEntity, null, true)
		when: 'setting asset readonly property value'
			assetFacade.priority = 1
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: priority for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test setting an asset custom field should be able to set new value and read it"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			Map<String, ?> fieldSettings = JsonUtil.convertJsonToMap(databaseFieldSettings)
			AssetFacade assetFacade = new AssetFacade(assetEntity, fieldSettings, false)
		when: 'setting asset custom field value'
			assetFacade.'Network Interfaces' = 'Any'
		then: 'the value should be able to be read'
			'Any' == assetFacade.'Network Interfaces'
	}

	void "test setting an asset custom field value should throw a ReadOnlyPropertyException"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			Map<String, ?> fieldSettings = JsonUtil.convertJsonToMap(databaseFieldSettings)
			AssetFacade assetFacade = new AssetFacade(assetEntity, fieldSettings, true)
		when: 'setting asset readonly custom field value'
			assetFacade.'Cost Basis' = 'Any'
		then: 'a ReadOnlyPropertyException should be thrown'
			ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
			'Cannot set readonly property: Cost Basis for class: net.transitionmanager.asset.AssetFacade' == e.message
	}

	void "test getting asset unknown property should throw a MissingPropertyException"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			Map<String, ?> fieldSettings = JsonUtil.convertJsonToMap(databaseFieldSettings)
			AssetFacade assetFacade = new AssetFacade(assetEntity, fieldSettings, true)
		when: 'getting an asset unknown property'
			assetFacade.unknownField
		then: 'a MissingPropertyException should be thrown'
			MissingPropertyException e = thrown MissingPropertyException
			'No such property: unknownField' == e.message
	}

	void "test getting asset unknown custom field should throw a MissingPropertyException"() {
		setup: 'giving an asset with basic properties'
			Project project = projectTestHelper.createProject(null)
			MoveBundle moveBundle = moveBundleTestHelper.createBundle(project, null)
			AssetEntity assetEntity = assetEntityTestHelper.createAssetEntity(AssetClass.DATABASE, project, moveBundle)
			Map<String, ?> fieldSettings = JsonUtil.convertJsonToMap(databaseFieldSettings)
			AssetFacade assetFacade = new AssetFacade(assetEntity, fieldSettings, true)
		when: 'getting an asset unknown custom field'
			assetFacade.'Unknown Custom Field'
		then: 'a MissingPropertyException should be thrown'
			MissingPropertyException e = thrown MissingPropertyException
			'No such property: Unknown Custom Field' == e.message
	}

}
