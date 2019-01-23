import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.tm.domain.AssetEntityHelper
import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification

class AssetEntityHelperUnitTests extends Specification implements  DataTest {

	@Shared Application app = new Application()
	@Shared Database db = new Database()
	@Shared AssetEntity asset = new AssetEntity()

	void setupSpec(){
		mockDomains AssetEntity, Application, Database
	}

	void 'test getPropertyNameByHashReference'(){
		expect: 'Iterate over the various test cases to get property names reference by hash tag'
			result == AssetEntityHelper.getPropertyNameByHashReference(clazz, param)
		where:
			clazz	| param 		| result		| note
			app 	| '#SME1'		| 'sme'			| 'sme gets renamed'
			app 	| '#SME2'		| 'sme2'		| 'sme and sme2 are upper cased for some unknown reason'
			app 	| '#Owner'		| 'appOwner'	| 'appOwner gets renamed'
			app 	| '#url'		| 'url'			| 'expected property'
			app 	| '#badProp'	| null 			| 'check for invalid property'
			app 	| 'noHashtag'	| null 			| 'missing #'
			app 	| ''			| null 			| 'blank parameter name'
			app 	| null 			| null 			| 'null property name'
			null 	| '#custom1'	| null 			| 'a null class'
			db 		| '#dbFormat' 	| 'dbFormat'	| 'valid property in a different domain class'
			db 		| '#badProp' 	| null			| 'check for invalid property again'
			asset 	| '#custom1'	| 'custom1' 	| 'get custom1 from the AssetEntity domain'
			app 	| '#custom1'	| 'custom1' 	| 'get custom1 from inheritance'
			db 		| '#custom1'	| 'custom1' 	| 'get custom1 from inheritance again'
			app 	| 'custom1'		| 'custom1'		| 'without the hash works the same way'
	}

	void 'test getIndirectPropertyRef method'() {
		when: 'custom1 points to custom2 for its value'
			AssetEntity asset = new AssetEntity(custom1:'#custom2', custom2:'12')
		then:
			AssetEntityHelper.getIndirectPropertyRef(asset, 'custom1') == '12'
			AssetEntityHelper.getIndirectPropertyRef(asset, '#custom1') == '12'
		and: 'referencing custom2 directly returns same results'
			AssetEntityHelper.getIndirectPropertyRef(asset, 'custom2') == '12'
			AssetEntityHelper.getIndirectPropertyRef(asset, '#custom2') == '12'

		when: 'referencing an invalid property should throw an exception'
			AssetEntityHelper.getIndirectPropertyRef(asset, '#bogusPropertyName')
		then:
			thrown(RuntimeException)

		when: 'indirect references are nested to deep an exception should occur'
			asset = new AssetEntity(custom1:'#custom2', custom2:'#custom3',
				custom3:'#custom4', custom4:'#custom5', custom5:'Yeah Baby')
			AssetEntityHelper.getIndirectPropertyRef(asset, '#custom1')
		then:
			thrown(RuntimeException)


		and: 'called with depth param it should return value and no exception'
			AssetEntityHelper.getIndirectPropertyRef(asset, '#custom1', 5) == 'Yeah Baby'

	}

	void 'test fixupHashtag method'() {
		expect: 'Iterate over the various test cases to get property names reference by hash tag'
			AssetEntityHelper.fixupHashtag(property) == expected
		where:
			property	| expected
			'#sme' 		| '#SME1'
			'#sme2' 	| '#SME2'
			'#appOwner' | '#Owner'
			'#custom1' 	| '#custom1'
	}
}
