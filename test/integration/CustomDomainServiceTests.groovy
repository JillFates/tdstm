import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.StringUtil
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class CustomDomainServiceTests extends Specification {

    CustomDomainService customDomainService

    private JSONObject createFieldSpecObject() {
        String base64EncodedJson = "ew0KICAiQVBQTElDQVRJT04iOiB7DQogICAgImRvbWFpbiI6ICJhcHBsaWNhdGlvbiIsDQogICAgImZpZWxkcyI6IFsNCiAgICAgIHsNCiAgICAgICAgImNvbnRyb2wiOiAiIiwNCiAgICAgICAgImRlZmF1bHQiOiAiIiwNCiAgICAgICAgImZpZWxkIjogImFwcEFjY2VzcyIsDQogICAgICAgICJpbXAiOiAiSSIsDQogICAgICAgICJsYWJlbCI6ICJBY2Nlc3NUeXBlIiwNCiAgICAgICAgIm9yZGVyIjogMSwNCiAgICAgICAgInJlcXVpcmVkIjogMCwNCiAgICAgICAgInNoYXJlZCI6IDAsDQogICAgICAgICJzaG93IjogMSwNCiAgICAgICAgInRpcCI6ICJUaGlzIGZpZWxkIGlzIHRoZSBBY2Nlc3NUeXBlIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDANCiAgICAgIH0sDQogICAgICB7DQogICAgICAgICJjb250cm9sIjogIiIsDQogICAgICAgICJkZWZhdWx0IjogIiIsDQogICAgICAgICJmaWVsZCI6ICJjdXN0b20xIiwNCiAgICAgICAgImltcCI6ICJJIiwNCiAgICAgICAgImxhYmVsIjogIkN1c3RvbTEiLA0KICAgICAgICAib3JkZXIiOiAyLA0KICAgICAgICAicmVxdWlyZWQiOiAwLA0KICAgICAgICAic2hhcmVkIjogMCwNCiAgICAgICAgInNob3ciOiAxLA0KICAgICAgICAidGlwIjogIlRoaXMgZmllbGQgaXMgdGhlIEN1c3RvbSAxIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDENCiAgICAgIH0NCiAgICBdLA0KICAgICJ2ZXJzaW9uIjogMA0KICB9LA0KICAiREFUQUJBU0UiOiB7DQogICAgImRvbWFpbiI6ICJkYXRhYmFzZSIsDQogICAgImZpZWxkcyI6IFsNCiAgICAgIHsNCiAgICAgICAgImNvbnRyb2wiOiAiIiwNCiAgICAgICAgImRlZmF1bHQiOiAiIiwNCiAgICAgICAgImZpZWxkIjogImFzc2V0TmFtZSIsDQogICAgICAgICJpbXAiOiAiSSIsDQogICAgICAgICJsYWJlbCI6ICJOYW1lIiwNCiAgICAgICAgIm9yZGVyIjogMSwNCiAgICAgICAgInJlcXVpcmVkIjogMSwNCiAgICAgICAgInNoYXJlZCI6IDAsDQogICAgICAgICJzaG93IjogMSwNCiAgICAgICAgInRpcCI6ICJUaGlzIGZpZWxkIGlzIHRoZSBOYW1lIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDANCiAgICAgIH0sDQogICAgICB7DQogICAgICAgICJjb250cm9sIjogIiIsDQogICAgICAgICJkZWZhdWx0IjogIiIsDQogICAgICAgICJmaWVsZCI6ICJjdXN0b20xIiwNCiAgICAgICAgImltcCI6ICJJIiwNCiAgICAgICAgImxhYmVsIjogIkN1c3RvbTEiLA0KICAgICAgICAib3JkZXIiOiAyLA0KICAgICAgICAicmVxdWlyZWQiOiAwLA0KICAgICAgICAic2hhcmVkIjogMCwNCiAgICAgICAgInNob3ciOiAxLA0KICAgICAgICAidGlwIjogIlRoaXMgZmllbGQgaXMgdGhlIEN1c3RvbSAxIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDENCiAgICAgIH0NCiAgICBdLA0KICAgICJ2ZXJzaW9uIjogMA0KICB9LA0KICAiREVWSUNFIjogew0KICAgICJkb21haW4iOiAiZGV2aWNlIiwNCiAgICAiZmllbGRzIjogWw0KICAgICAgew0KICAgICAgICAiY29udHJvbCI6ICIiLA0KICAgICAgICAiZGVmYXVsdCI6ICIiLA0KICAgICAgICAiZmllbGQiOiAiYXBwbGljYXRpb24iLA0KICAgICAgICAiaW1wIjogIkkiLA0KICAgICAgICAibGFiZWwiOiAiQXBwbGljYXRpb24iLA0KICAgICAgICAib3JkZXIiOiAxLA0KICAgICAgICAicmVxdWlyZWQiOiAwLA0KICAgICAgICAic2hhcmVkIjogMCwNCiAgICAgICAgInNob3ciOiAxLA0KICAgICAgICAidGlwIjogIlRoaXMgZmllbGQgaXMgdGhlIEFwcGxpY2F0aW9uIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDANCiAgICAgIH0sDQogICAgICB7DQogICAgICAgICJjb250cm9sIjogIiIsDQogICAgICAgICJkZWZhdWx0IjogIiIsDQogICAgICAgICJmaWVsZCI6ICJjdXN0b20xIiwNCiAgICAgICAgImltcCI6ICJJIiwNCiAgICAgICAgImxhYmVsIjogIkN1c3RvbTEiLA0KICAgICAgICAib3JkZXIiOiAyLA0KICAgICAgICAicmVxdWlyZWQiOiAwLA0KICAgICAgICAic2hhcmVkIjogMCwNCiAgICAgICAgInNob3ciOiAxLA0KICAgICAgICAidGlwIjogIlRoaXMgZmllbGQgaXMgdGhlIEN1c3RvbSAxIiwNCiAgICAgICAgInR5cGUiOiAiU3RyaW5nIiwNCiAgICAgICAgInVkZiI6IDENCiAgICAgIH0NCiAgICBdLA0KICAgICJ2ZXJzaW9uIjogMA0KICB9LA0KICAiU1RPUkFHRSI6IHsNCiAgICAiZG9tYWluIjogInN0b3JhZ2UiLA0KICAgICJmaWVsZHMiOiBbDQogICAgICB7DQogICAgICAgICJjb250cm9sIjogIiIsDQogICAgICAgICJkZWZhdWx0IjogIiIsDQogICAgICAgICJmaWVsZCI6ICJhc3NldE5hbWUiLA0KICAgICAgICAiaW1wIjogIkkiLA0KICAgICAgICAibGFiZWwiOiAiTmFtZSIsDQogICAgICAgICJvcmRlciI6IDEsDQogICAgICAgICJyZXF1aXJlZCI6IDEsDQogICAgICAgICJzaGFyZWQiOiAwLA0KICAgICAgICAic2hvdyI6IDEsDQogICAgICAgICJ0aXAiOiAiVGhpcyBmaWVsZCBpcyB0aGUgTmFtZSIsDQogICAgICAgICJ0eXBlIjogIlN0cmluZyIsDQogICAgICAgICJ1ZGYiOiAwDQogICAgICB9LA0KICAgICAgew0KICAgICAgICAiY29udHJvbCI6ICIiLA0KICAgICAgICAiZGVmYXVsdCI6ICIiLA0KICAgICAgICAiZmllbGQiOiAiY3VzdG9tMSIsDQogICAgICAgICJpbXAiOiAiSSIsDQogICAgICAgICJsYWJlbCI6ICJDdXN0b20xIiwNCiAgICAgICAgIm9yZGVyIjogMiwNCiAgICAgICAgInJlcXVpcmVkIjogMCwNCiAgICAgICAgInNoYXJlZCI6IDAsDQogICAgICAgICJzaG93IjogMSwNCiAgICAgICAgInRpcCI6ICJUaGlzIGZpZWxkIGlzIHRoZSBDdXN0b20gMSIsDQogICAgICAgICJ0eXBlIjogIlN0cmluZyIsDQogICAgICAgICJ1ZGYiOiAxDQogICAgICB9DQogICAgXSwNCiAgICAidmVyc2lvbiI6IDANCiAgfQ0KfQ=="
        JSONObject parsedJson = new JSONObject(StringUtil.base64DecodeToString(base64EncodedJson))
        return parsedJson
    }

    void 'Scenario 1: Retrieve custom field specs of any AssetClass'() {
        given:
            def domain = AssetClass.DATABASE as String
            def fieldSpec = createFieldSpecObject()
            def customFieldSpecsMap
            customDomainService.saveFieldSpecs(CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)
        when: 'Database custom field specs are requested'
            customFieldSpecsMap = customDomainService.customFieldSpecs(domain)
        then: 'Database domain fields are returned'
            customFieldSpecsMap[domain]["domain"] == domain.toLowerCase()
        then: 'Only database udf fields are returned'
            [] == customFieldSpecsMap[domain]["fields"].findAll({field -> field.udf == 0})
    }

    void 'Scenario 2: Retrieve standard field specs of any AssetClass'() {
        given:
            def domain = AssetClass.APPLICATION as String
            def fieldSpec = createFieldSpecObject()
            def standardFieldSpecsMap
            customDomainService.saveFieldSpecs(CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)
        when: 'Application standard field specs are requested'
            standardFieldSpecsMap = customDomainService.standardFieldSpecs(domain)
        then: 'Application domain fields are returned'
            standardFieldSpecsMap[domain]["domain"] == domain.toLowerCase()
        then: 'Only database udf fields are returned'
            [] == standardFieldSpecsMap[domain]["fields"].findAll({field -> field.udf == 1})
    }

    void 'Scenario 3: Saving field specs providing unexisting domain type should throw InvalidParamException'() {
        given:
            def domain = "-invalid-"
            def fieldSpec = createFieldSpecObject()
        when: 'Save fields specs providing invalid AssetClass should throw an exception'
            customDomainService.saveFieldSpecs(domain, fieldSpec)
        then: 'InvalidParamException should be thrown'
            thrown InvalidParamException
    }

    void 'Scenario 4: Saving field specs providing all asset classes as domain type should save custom fields specs'() {
        given:
            def domain = CustomDomainService.ALL_ASSET_CLASSES
            def fieldSpec = createFieldSpecObject()
        when: 'Save fields specs should save without errors'
            customDomainService.saveFieldSpecs(domain, fieldSpec)
        then: 'Saved fields specs should exists'
            null != customDomainService.allFieldSpecs(domain)
    }

    void 'Scenario 5: Saving field specs and retrieving them from database should return them'() {
        given:
            def domain = CustomDomainService.ALL_ASSET_CLASSES
            def fieldSpec = createFieldSpecObject()
            customDomainService.saveFieldSpecs(domain, fieldSpec)
            def foundFieldSpec
        when: 'Retrieving fields specs from database should return them'
            foundFieldSpec = customDomainService.allFieldSpecs(domain)
        then: 'Found fields specs are not null'
            null != foundFieldSpec
        then:
            foundFieldSpec instanceof Map
    }

}
