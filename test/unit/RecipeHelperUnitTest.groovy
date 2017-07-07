import com.tds.asset.AssetEntity
import com.tdsops.common.exceptions.RecipeException
import com.tdsops.tm.domain.RecipeHelper
import spock.lang.Shared
import spock.lang.Specification

class RecipeHelperUnitTest extends Specification {

    @Shared
    AssetEntity asset = new AssetEntity("custom1": "http://www.domain.com", "custom2": "some value", "retireDate": new Date())

    void "test validateDocLinkSyntax"() {
        when: "requesting a valid string field from AssetEntity"
            String docLink = "#custom1"
        then:
            RecipeHelper.validateDocLinkSyntax(docLink) == null
        when: "using a correct markup"
            docLink = "The Company | http://thecompany.com"
        then:
            RecipeHelper.validateDocLinkSyntax(docLink) == null
        when: "requesting a valid field but with the wrong type"
            docLink = "#manufacturer"
        then:
            RecipeHelper.validateDocLinkSyntax(docLink) != null
        when: "using an incorrect markup"
            docLink = "My Company"
        then:
            RecipeHelper.validateDocLinkSyntax(docLink) != null
    }

    void "test resolveDocLink for valid scenarios"(){
        expect: "The correct dockLink is resolved"
            docLink == RecipeHelper.resolveDocLink(value, anAsset)
        where:
            docLink                     |   value                   |   anAsset
            "http://www.domain.com"     |   "http://www.domain.com" | null
            "http://www.domain2.com"    |   "http://www.domain2.com"| asset
            "http://www.domain.com"     |   "#custom1"              | asset
    }

    void "test resolveDocLink for invalid scenarios"() {
        when: "requesting a field that doesn't exist for the asset"
            RecipeHelper.resolveDocLink("#invalidField", asset)
        then: "an exception is thrown"
            thrown(RecipeException)
        when: "requesting a valid field, but no asset is given"
            RecipeHelper.resolveDocLink("#custom1")
        then: "an exception is thrown"
            thrown(RecipeException)
        when: "requesting a field that isn't a string"
            RecipeHelper.resolveDocLink("#retireDate", asset)
        then: "an exception is thrown"
            thrown(RecipeException)
        when: "requesting a field that doesn't have a valid markup"
            RecipeHelper.resolveDocLink("#custom2", asset)
        then: "an exception is thrown"
            thrown(RecipeException)

    }

}