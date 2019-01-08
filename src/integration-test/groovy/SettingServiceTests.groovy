import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SettingService
import org.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Integration
@Rollback
class SettingServiceTests extends Specification {

    SettingService settingService

    private Project createProject() {
        ProjectTestHelper projectTestHelper = new ProjectTestHelper()
        return projectTestHelper.createProject()
    }

    private String parseJson(String json) {
        JsonSlurper slurper = new JsonSlurper()
        JSONObject parsedJson = slurper.parseText(json) as JSONObject
        JsonBuilder jsonBuilder = new JsonBuilder(parsedJson)
        return jsonBuilder.toString()
    }

    private String createSampleJson(boolean withUpdatedField=false) {
        String base64EncodedJson = "ew0KICAic2V0dGluZ19rZXlfMSI6ICJ2YWx1ZSIsDQogICJzZXR0aW5nX2tleV8yIjogMSwNCiAgInNldHRpbmdfa2V5XzMiOiAidXBkYXRlZCINCn0="
        if (withUpdatedField) {
            base64EncodedJson = "ew0KICAic2V0dGluZ19rZXlfMSI6ICJ2YWx1ZSIsDQogICJzZXR0aW5nX2tleV8yIjogMiwNCiAgInNldHRpbmdfa2V5XzMiOiAidXBkYXRlZCINCn0="
        }
        return StringUtil.base64DecodeToString(base64EncodedJson)
    }

    void 'Scenario 1: Calling getAsJson to get a project specific setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'getAsJson is called specifying the project, type and key'
            setting = settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting should be returned and the result has content'
            null != setting
    }

    void 'Scenario 2: Calling getAsJson for a setting overridden by project'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'getAsJson is called specifying the project, type and key'
            setting = settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting for the project should be returned and the result has content'
            null != setting
    }

    void 'Scenario 3: Calling getAsJson for a global setting when there is no overridden setting'() {
        given:
            def json = createSampleJson()
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsJson is called specifying the project, type and key'
            setting = settingService.getAsJson(null, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the globally saved setting should be returned and the result has content'
            null != setting
    }

    void 'Scenario 4: Calling getAsJson for non-existent setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsJson is called specifying the project, type and key'
            setting = settingService.getAsJson(project, SettingType.MAIL, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then a null value is returned'
            null == setting
    }

    void 'Scenario 1: Calling getAsMap to get a project specific setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsMap is called specifying the project, type and key'
            setting = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting should be returned and the result is valid JSON'
            null != setting
    }

    void 'Scenario 2: Calling getAsMap to get a project overridden setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsMap is called specifying the project, type and key'
            setting = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting for the project should be returned and the result has content'
            null != setting
    }

    void 'Scenario 3: Get a Global Setting when there is no overridden setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsMap is called specifying the project, type and key'
            setting = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the globally saved setting should be returned and the result is valid JSON'
            null == setting
    }

    void 'Scenario 1: Calling getAsJson to get a global setting'() {
        given:
            def json = createSampleJson()
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsJson is called specifying the type and key'
            setting = settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting should be returned and the result has content'
            null != setting
    }

    void 'Scenario 2: Calling getAsJson referencing a non-existent global setting'() {
        given:
            def setting
        when: 'When getAsJson is called specifying the type and key'
            setting = settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then a null value should be returned'
            null == setting
    }

    void 'Scenario 1: Calling getAsMap to get a global setting'() {
        given:
            def json = createSampleJson()
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            def setting
        when: 'When getAsMap is called specifying the type and key'
            setting = settingService.getAsMap(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then the saved setting should be returned and the result has content'
            null != setting
    }

    void 'Scenario 2: Calling getAsMap referencing a non-existent global setting'() {
        given:
            def setting
        when: 'When getAsMap is called specifying the type and key'
            setting = settingService.getAsMap(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        then: 'Then a null value should be returned'
            null == setting
    }

    void 'Scenario 1: Calling save to create a new project scoped setting'() {
        given:
            def json = createSampleJson()
            def project = createProject()
        when: 'When the save method is called with a valid project, type, key and version=0'
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
        then: 'Then the setting should be saved'
        and: 'and can be retrieved with the getAsJson method'
            null != settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        and: 'and the resulting JSON should match the original JSON'
            parseJson(json) == settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
    }

    @Ignore
    // GRAILS-9862
    void 'Scenario 2: Calling save to update an existing project scoped setting'() {
        given:
            def json = createSampleJson()
            def jsonWithUpdatedField = createSampleJson(true)
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, jsonWithUpdatedField, 0)
        when: 'When the save method is called with a valid project, type, key and version=1'
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 1)
        then: 'Then the setting should be updated'
        and: 'and can be retrieved with the getAsJson method'
            null != settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        and: 'and the resulting JSON should match the original JSON'
            parseJson(json) == settingService.getAsJson(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
    }

    void 'Scenario 3: Calling save to update an existing project scoped setting with outdated version'() {
        given:
            def json = createSampleJson()
            def project = createProject()
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
        when: 'When the save method is called with a valid project, type, key and version=0'
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 1)
        then: 'Then the call should throw the DomainUpdateException'
            final DomainUpdateException exception = thrown()
        and:
            "The data has been changed by someone else" == exception.message
    }

    void 'Scenario 4: Calling save to create a setting with invalid formatted JSON'() {
        given:
            def json = "{not_json_in_here}"
            def project = createProject()
        when: 'When the save method is called with a valid project, type, key and version=0'
            settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
        then: 'Then the call should throw the InvalidParamException'
            final InvalidParamException exception = thrown()
        and:
            exception.message.startsWith('Invalid JSON')
    }

    void 'Scenario 1: Calling save to create a new globally scoped setting'() {
        given:
            def json = createSampleJson()
        when: 'When the save method is called with a type, key and version=0'
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
        then: 'Then the setting should be saved'
        and: 'and can be retrieved with the getAsJson method'
            null != settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        and: 'and the resulting JSON should match the original JSON'
            parseJson(json) == settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
    }

    void 'Scenario 2: Calling save to create a new globally scoped setting that optimizes the JSON'() {
        given:
            def json = createSampleJson()
        when: 'When the save method is called with a type, key and version=0'
            settingService.save(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES, json, 0)
        then: 'Then the setting should be saved'
            println "Unoptimized JSON: ${json}"
        and: 'and can be retrieved with the getAsJson method'
            null != settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
        and: 'and the resulting JSON should match the original JSON that has been optimized'
            println "Optimized JSON: ${parseJson(json)}"
            parseJson(json) == settingService.getAsJson(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, CustomDomainService.ALL_ASSET_CLASSES)
    }

}
