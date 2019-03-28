import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ProjectService
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.RandomStringUtils as RSU
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Integration
@Rollback
class CustomDomainServiceTests extends Specification {

    CustomDomainService customDomainService
    ProjectService projectService

    // Note that this JSON file is managed by the /misc/generateDomainFieldSpecs.groovy script
    // After generating the file it needs to be copied to the /grails-app/conf/ directory so it can be read
    // as a resource for the application.
    private static final String fieldSpecDefinitionJson = 'CustomDomainServiceTests_FieldSpec.json'
    private static JSONObject fieldSpecJson

    private AssetTestHelper assetHelper
    private CustomDomainTestHelper customDomainTestHelper
    private ProjectTestHelper projectHelper

    void setup(){
        assetHelper = new AssetTestHelper()
        customDomainTestHelper = new CustomDomainTestHelper()
        projectHelper = new ProjectTestHelper()
    }

    private static final String CUSTOM1_LABEL = 'Description'

    /**
     * This will load the JSON file that accompanies this test suite and will return
     * it as a JSONObject.
     */
        private JSONObject loadFieldSpecJson() {
        // Determine where we can write the resource file for testing
        // this.class.classLoader.rootLoader.URLs.each{ println it }
        // We'll only load it the first time and cache it into fieldSpecJson
        JSONObject fieldSpecJson
        if (!fieldSpecJson) {
            String jsonText = this.getClass().getResource( fieldSpecDefinitionJson ).text
            fieldSpecJson = new JSONObject(jsonText)
            assert fieldSpecJson
            // println "\n\n$fieldSpecJson\n\n"
        }
        return fieldSpecJson
    }

    // A list of assets that will be used by a few test cases
    private static final List ASSETS = [
        [name:RSU.randomAlphabetic(10), description: 'Red'],
        [name:RSU.randomAlphabetic(10), description: 'Green'],
        [name:RSU.randomAlphabetic(10), description: 'Blue'],
        [name:RSU.randomAlphabetic(10), description: 'Blue']
    ]

    /**
     * Used to create a list of assets for a project that will be used by several tests
     * @param project - the project to associate the assets to
     */
    private void createAssets(Project project) {
        for (asset in ASSETS) {
            assetHelper.createDevice(project, 'Server', [assetName:asset.name, description: asset.description])
        }
    }

    void 'Scenario 1: Retrieve custom field specs of any AssetClass'() {
        given:
            Project project = projectHelper.createProjectWithDefaultBundle()
            def domain = AssetClass.DATABASE as String
            loadFieldSpecJson()
            def fieldSpec = loadFieldSpecJson()
            def customFieldSpecsMap
            Setting.findAllByProject(project)*.delete(flush:true)
            customDomainService.saveFieldSpecs(project, CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)
        when: 'Database custom field specs are requested'
            customFieldSpecsMap = customDomainService.customFieldSpecs(project, domain)
        then: 'Database domain fields are returned'
            customFieldSpecsMap[domain]["domain"] == domain.toLowerCase()
        then: 'Only database udf fields are returned'
            [] == customFieldSpecsMap[domain]["fields"].findAll({field -> field.udf == 0})
    }

    void 'Scenario 2: Retrieve standard field specs of any AssetClass'() {
        given:
            Project project = projectHelper.createProjectWithDefaultBundle()
            def domain = AssetClass.APPLICATION as String
            def fieldSpec = loadFieldSpecJson()
            def standardFieldSpecsMap
            Setting.findAllByProject(project)*.delete(flush:true)
            customDomainService.saveFieldSpecs(project, CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)
        when: 'Application standard field specs are requested'
            standardFieldSpecsMap = customDomainService.standardFieldSpecsByField(project, domain)
        then: 'Application domain fields are returned'
            //standardFieldSpecsMap[domain]["domain"] == domain.toLowerCase()
            null != standardFieldSpecsMap
        then: 'Only database udf fields are returned'
            //[] == standardFieldSpecsMap[domain]["fields"].findAll({field -> field.udf == 1})
            [:] == standardFieldSpecsMap.findAll({ k,v -> v['udf'] == 1})
    }

    void 'Scenario 3: Saving field specs providing unexisting domain type should throw InvalidParamException'() {
        given:
            Project project = projectHelper.createProjectWithDefaultBundle()
            def domain = "-invalid-"
            def fieldSpec = loadFieldSpecJson()
            Setting.findAllByProject(project)*.delete(flush:true)
        when: 'Save fields specs providing invalid AssetClass should throw an exception'
            customDomainService.saveFieldSpecs(project, domain, fieldSpec)
        then: 'InvalidParamException should be thrown'
            thrown InvalidParamException
    }

    void 'Scenario 4: Saving field specs providing all asset classes as domain type should save custom fields specs'() {
        given:
            Project project = projectHelper.createProjectWithDefaultBundle()
            def domain = CustomDomainService.ALL_ASSET_CLASSES
            def fieldSpec = loadFieldSpecJson()
            Setting.findAllByProject(project)*.delete(flush:true)
        when: 'Save fields specs should save without errors'
            customDomainService.saveFieldSpecs(project, domain, fieldSpec)
        then: 'Saved fields specs should exists'
            null != customDomainService.allFieldSpecs(project, domain)
    }

    void 'Scenario 5: Saving field specs and retrieving them from database should return them'() {
        given:
            Project project = projectHelper.createProjectWithDefaultBundle()
            def domain = CustomDomainService.ALL_ASSET_CLASSES
            def fieldSpec = loadFieldSpecJson()
            Setting.findAllByProject(project)*.delete(flush:true)
            customDomainService.saveFieldSpecs(project, domain, fieldSpec)
            def foundFieldSpec
        when: 'Retrieving fields specs from database should return them'
            foundFieldSpec = customDomainService.allFieldSpecs(project, domain)
        then: 'Found fields specs are not null'
            null != foundFieldSpec
        then:
            foundFieldSpec instanceof Map
    }

    void 'Scenario 6: Test fieldNamesAsMap for expected values'() {
        given: 'a project'
            Project project = projectHelper.createProjectWithDefaultBundle()
            Setting.findAllByProjectAndType(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC)*.delete(flush: true)
        and: 'the project has field settings specifications'
            projectService.cloneDefaultSettings(project)

        when: 'calling the fieldNamesAsMap method'
            String deviceClass = AssetClass.DEVICE.toString()
            Map fields = customDomainService.fieldNamesAsMap(project, deviceClass)
        then: 'the map should contain values'
            fields
        and: 'the map should contain certain values'
            fields.containsKey('assetName')
            fields.containsKey('description')
            fields.containsKey('planStatus')
            fields.containsKey('environment')
    }

    void 'Scenario 7: Test distinctValues returns expected values'() {
        given: 'a project'
            Project project = projectHelper.createProjectWithDefaultBundle()
            Setting.findAllByProjectAndType(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC)*.delete(flush: true)
        and: 'the project has field settings specifications'
            projectService.cloneDefaultSettings(project)
        and: 'the project has assets with existing data values'
            createAssets(project)
            List assetList = AssetEntity.findAllByProject(project)
            assert ASSETS.size() == assetList.size()
        and: 'there is an individual field specification for DEVICE.custom1'
            JSONObject allDomainSpecs = loadFieldSpecJson()
            String deviceClass = AssetClass.DEVICE.toString()
            JSONObject fieldSpec = [fieldSpec: allDomainSpecs[deviceClass]['fields'].find { it.field == 'description' } ]
            assert fieldSpec

        when: 'the distinctValues method is called'
            List list = customDomainService.distinctValues(project, deviceClass, fieldSpec)
        then: 'it should return expected values'
            list
            3 == list.size()
            list.contains('Red')
            list.contains('Blue')
            list.contains('Green')
    }

    void 'Scenario 8: Test COMMON field specs'() {
		String APPLICATION = AssetClass.APPLICATION as String

        given: 'a project'
            Project project = projectHelper.createProjectWithDefaultBundle()
            Setting.findAllByProjectAndType(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC)*.delete(flush: true)
        and: 'the project has field settings specifications'
            projectService.cloneDefaultSettings(project)
        and: 'the project has assets with existing data values'
            createAssets(project)
            List assetList = AssetEntity.findAllByProject(project)
            assert ASSETS.size() == assetList.size()

        when: 'the fieldSpecsWithCommon method is called'
            Map fieldSpecs = customDomainService.fieldSpecsWithCommon(project)
        then: "it should contains a '${CustomDomainService.COMMON}' domain"
            fieldSpecs.containsKey(CustomDomainService.COMMON)
        and: "'${CustomDomainService.COMMON}' fields not 'shared' are contained in AssetEntity.COMMON_FIELD_LIST"
            def commonSpecs = fieldSpecs[CustomDomainService.COMMON]
            def fieldsCommon = commonSpecs.fields

			fieldsCommon.each { spec ->
				if (BooleanUtils.toBoolean(spec.shared) == false) {
					assert 	AssetEntity.COMMON_FIELD_LIST.contains(spec.field)
				}
			}

		and: "'${APPLICATION}' Domian fields are not contained in '${CustomDomainService.COMMON}' fields and there are not shared ones"
			def applicationSpecs = fieldSpecs[APPLICATION]
			def fieldsApplication = applicationSpecs.fields

			fieldsApplication.each { appField ->
				assert BooleanUtils.toBoolean(appField.shared) == false

				def found = fieldsCommon.find { commonField ->
					commonField.field == appField.field
				}

				assert ! found, "Field '${found?.field}' found in domain '${CustomDomainService.COMMON}'"
			}

    }

    void '9. Test the getFieldSpecsForAssetExport method for different scenarios'() {
        setup: 'Create a project'
            Project project = projectHelper.createProject()
            String domain = AssetClass.APPLICATION.toString()
        when: 'retrieving the fields to be exported and all available fields for the project'
            Map exportFields = customDomainService.getFieldSpecsForAssetExport(project, domain, [])
            Map allFields = customDomainService.allFieldSpecs(project, domain)
            Map assetClassField = allFields[domain].fields.find {it.field == 'assetClass'}
            Map custom1Field = exportFields[domain].fields.find {it.field == 'custom1'}
        then: 'the export fields do not contain the assetClass, as it is not to be displayed'
            exportFields[domain].fields.find {it.field == 'assetClass'} == null
        and: 'the assetClass field is included in the all fields map'
            assetClassField != null
        and: 'assetClass is not included in the fields to be exported as it is a standard field not visible'
            exportFields[domain].fields.find {it.field == 'assetClass'} == null
        and: 'the field is not user defined'
            assetClassField.udf == 0
        and: 'the field is marked as not displayed'
            assetClassField.show == 0
        and: 'the custom1 field is included for export'
            custom1Field != null
        and: 'the field is user defined'
            custom1Field.udf == 1
        and: 'the field is to be displayed'
            custom1Field.show == 1
        when: 'setting the custom1 field as not to be displayed'
            Closure updateClosure = { JSONObject fieldSpecJson ->
                (fieldSpecJson.fields.find{it.field == 'custom1'}).show = 0
            }
            customDomainTestHelper.updateFieldSpec(project, domain, updateClosure)
            exportFields = customDomainService.getFieldSpecsForAssetExport(project, domain, [])
            custom1Field = exportFields[domain].fields.find {it.field == 'custom1'}
        then: 'the custom field is present in the fields to be exported even though is not marked as displayed'
            custom1Field != null
        when: 'using the template headers to ask for a field that would not be included otherwise'
            exportFields = customDomainService.getFieldSpecsForAssetExport(project, domain, ['Asset Class'])
            Map assetClassFieldMap = exportFields[domain].fields.find {it.field == 'assetClass'}
        then: 'the assetClass field is included in the result'
            assetClassField != null
    }

}
