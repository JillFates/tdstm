import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import grails.test.spock.IntegrationSpec
import com.tdssrc.grails.StringUtil
import net.transitionmanager.command.UploadFileCommand
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.FileSystemService
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Ignore
import spock.lang.Shared
import test.helper.AssetEntityTestHelper

class DataImportServiceIntegrationSpec extends IntegrationSpec {
	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
    DataImportService dataImportService

	@Shared
	DataScriptTestHelper dataScriptTestHelper = new DataScriptTestHelper()

	@Shared
	FileSystemService fileSystemService

	@Shared
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()

	@Shared
	PersonTestHelper personTestHelper = new PersonTestHelper()

	@Shared
    ProjectTestHelper projectTestHelper = new ProjectTestHelper()

	@Shared
	ProviderTestHelper providerTestHelper = new ProviderTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()
	@Shared
	Project otherProject = projectTestHelper.createProject()
	@Shared
	MoveBundle moveBundle
	@Shared
	AssetEntity device
	@Shared
	AssetEntity device2
	@Shared
	AssetEntity otherProjectDevice
	@Shared
	Map context

	void setupSpec() {

	}
	void setup() {
		// project = projectTestHelper.createProject(null)
		// otherProject = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject,
			moveBundleTestHelper.createBundle(otherProject, null))
		context = dataImportService.initContextForProcessBatch( project, ETLDomain.Dependency )
		context.record = new ImportBatchRecord(sourceRowId:1)

		device.assetType = 'Server'
		device.save()

		device2.assetType = 'Server'
		device2.save()

		// Create a second project with a device with the same name and type as device above
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.save()
	}

	/**
	 * Used to generate the necessary initFieldsInfo map
	 */
	private Map initFieldsInfo() {
		[
			"id": [
				"value": "",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"asset": [
				"value": "",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"dependent": [
				"value": "",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			// "createdBy": [
			// 	"value": "Bill Z. Bubb",
			// 	"originalValue": "",
			// 	"error": false,
			// 	"warn": false,
			// 	"errors": [],
			// 	"find": [
			// 		"query": []
			// 	]
			// ],
			"type": [
				"value": "Hosts",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"status": [
				"value": "Archived",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"dataFlowFreq": [
				"value": "weekly",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"c2": [
				"value": "This is awesome",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			]
		]
	}

	private JSONObject initFieldsInfoAsJSONObject() {
		new JSONObject(initFieldsInfo() )
	}

	void '1. give the performQueryAndUpdateFindElement method a spin'() {
		// performQueryAndUpdateFindElement(String propertyName, Map fieldsInfo, Map context)
		given:
			Object entity
			String propertyName = 'asset'
			String assetType = 'Server'
			Map fieldsInfo = initFieldsInfo()

		when: 'the query is by the assetName of an existing asset'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName
					]
				]
			]
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			[device] ==  dataImportService.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)


		when: 'the query is by the assetName of an non-existing asset'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: 'A bogus asset name that does not exist for certain!'
					]
				]
			]
		then: 'calling performQueryAndUpdateFindElement should return an empty list'
			[] ==  dataImportService.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)


		when: 'there is an asset with the same assetName in a different project'
			// Done in spec setup
		and: 'the query is by assetName only'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName
					]
				]
			]
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			[device] ==  dataImportService.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)


		when: 'there is a second asset with the same assetType'
			// Done in spec setup
		and: 'the query is by the assetType only'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetType: assetType
					]
				]
			]
		and: 'calling performQueryAndUpdateFindElement that should return multiple records'
			List entities =  dataImportService.performQueryAndUpdateFindElement(propertyName, new JSONObject(fieldsInfo), context)
		then: 'the list of entities should have 2 entities'
			2 == entities.size()
		and: 'the returned list should have the ids of the expected records'
			entities.find { it.id == device.id }
			entities.find { it.id == device2.id }
		and: 'the find.results should have ids of 2 records'
			2 == fieldsInfo[propertyName].find.results.size()
		and: 'the find.results should have the ids of the expected records'
			(device.id in fieldsInfo[propertyName].find.results)
			(device2.id in fieldsInfo[propertyName].find.results)


		when: 'the query has multiple query criteria'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: 'bogus name so will not be found by this one',
						assetType: assetType
					]
				],
				[	domain: 'Device',
					kv: [
						assetName: device.assetName,
						assetType: 'Wrong AssetType'
					]
				],
				[	domain: 'Asset',
					kv: [
						assetName: device.assetName
					]
				],
			]
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			[device] ==  dataImportService.performQueryAndUpdateFindElement(propertyName, new JSONObject(fieldsInfo), context)
		and: 'the find.matchOn should indicate the third criteria found the result'
			3 == fieldsInfo[propertyName].find.matchOn
		and: 'the find.results should have the id of the expected record'
			[device.id] == fieldsInfo[propertyName].find.results

	}

	void '2. beat up on classOfDomainProperty'() {
		// classOfDomainProperty(String propertyName, Map fieldsInfo, Map context)

		given:
			Class clazz
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()

		when: 'called for the identity of the object'
			clazz = dataImportService.classOfDomainProperty('id', fieldsInfo, context)
		then:
			AssetDependency == clazz

		when: 'called for the asset reference'
			clazz = dataImportService.classOfDomainProperty('asset', fieldsInfo, context)
		then: 'an AssetEntity should be returned'
			AssetEntity == clazz

		when: 'the first find query was for the Application domain'
			fieldsInfo.asset.find.query = [ [
				domain: 'Application',
				assetName: 'foo'
			] ]
		and: 'and calling for the asset reference'
			clazz = dataImportService.classOfDomainProperty('asset', fieldsInfo, context)
		then: 'an Application class should be returned'
			Application == clazz

		when: 'the field.create has the assetClass param and refers to assetClass Database'
			fieldsInfo.asset.create = [
				assetClass: 'Database',
				assetName: 'foo'
			]
		and: 'the query section was removed from previous test'
			fieldsInfo.asset.find.query = []
		and: ' classOfDomainProperty is calledfor the asset reference'
			clazz = dataImportService.classOfDomainProperty('asset', fieldsInfo, context)
		then: 'a Database class should be returned'
			Database == clazz


		when: 'called for an invalid property name'
			String propertyName = 'xyzzy'
			clazz = dataImportService.classOfDomainProperty(propertyName, fieldsInfo, context)
			String expectedError = StringUtil.replacePlaceholders(dataImportService.PROPERTY_NAME_NOT_IN_DOMAIN, [propertyName:propertyName])
			List errorsFound = context.record.errorListAsList()
		then: 'an error should be reported'
			1 == errorsFound.size()
		and: 'the error message should be what is expected'
			expectedError == errorsFound[0]

		// when: 'called for a non-asset reference property (Person)'
		// 	clazz = dataImportService.classOfDomainProperty('createdBy', fieldsInfo, context)
		// then: 'a Person domain class should be returned'
		// 	clazz == Person

		when: 'called for the a non-identity or reference field'
			fieldsInfo['c1'] = [errors:[]]
			clazz = dataImportService.classOfDomainProperty('c1', fieldsInfo, context)
			List errors = dataImportService.getFieldsInfoFieldErrors('c1', fieldsInfo)
		then: 'no domain class should be returned'
			clazz == null
		and: 'there should be an error on the field'
			1 == errors.size()
		and: 'the error message should be what is expected'
			dataImportService.WHEN_NOT_FOUND_PROPER_USE_MSG == errors[0]

	}

    void 'Test fetchEntityByFieldMetaData for no find.query specified'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()

        when: 'called with no id and an empty query section'
			def entity = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
        then: 'no entity should be returned'
			! entity
		and: 'a particular error message should be recorded in the fieldsInfo'
			dataImportService.NO_FIND_QUERY_SPECIFIED_MSG == fieldsInfo[property].errors[0]
	}

    void 'Test fetchEntityByFieldMetaData for find by field.value set to asset ID number'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()

		when: 'the field value contains the asset id as a numeric value'
			fieldsInfo[property].value = device.id
		and: 'the method is called'
			def entity = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'the asset should be found'
			entity
		and: 'the asset should match the one attempting to be found'
			device.id == entity.id
	}

	void 'Test fetchEntityByFieldMetaData for find by field.value set to alternate key'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()

		when: 'the field value contains the alternate key value (assetName)'
			fieldsInfo[property].value = device.assetName
		and: 'the method is called'
			def result = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'the asset should be found'
			result
		and: 'the asset should match the one attempting to be found'
			device.id == result.id
	}

    void 'Test fetchEntityByFieldMetaData for find by query'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()

		when: 'the field query contains the search by the assetName and assetType'
			fieldsInfo[property].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName,
						assetType: device.assetType
					]
				]
			]
		and: 'field.value is empty'
			fieldsInfo[property].value = ''
		and: 'the method is called'
			def result = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'the expected asset should be returned'
			device == result
	}

    void 'Test fetchEntityByFieldMetaData for caching'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			String newAssetName =  RandomStringUtils.randomAlphabetic(10)
			String assetType = 'Server'

		when: 'the field query contains the search by the assetName and assetType'
			fieldsInfo[property].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: newAssetName,
						assetType: assetType
					]
				]
			]
		and: 'the method is called'
			def result = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
			String cacheKey = context.cache.lastKey
		then: 'no result should be returned'
			null == result
		and: 'the cache should have one entry'
			1 == context.cache.size()
		and: 'the value is indicates that the asset was not found'
			null == context.cache.get(cacheKey)

		when: 'the asset is subsequently created'
			def device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		and: 'it has the proper name and type'
			device3.assetName = newAssetName
			device3.assetType = assetType
			device3.save(flush:true)
		and: 'the method is called again'
			result = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'the asset should be returned'
			device3 == result
		and: 'the cache should still have a single entry'
			1 == context.cache.size()
		and: 'the cache entry should be the asset'
			device3 == context.cache.get(cacheKey)

		// when: 'the cache for the domain searches is set to a number'
		// 	Long overwritten = 324
		// 	Set keys = context.cache.keySet()
		// 	keys.each { key -> context.cache[key] = overwritten }
		// and: 'fetchEntityByFieldMetaData is called again with the same query'
		// 	result = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		// then: 'the object returned should by the override number indicating that the cache is working correctly'
		// 	overwritten == result
	}

    void 'Test fetchEntityByFieldMetaData method finding duplicates'() {
		setup:
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			String property = 'asset'

		when: 'there are two entities that have a common attribute (assetType=Server)'
			// done in the spec setup
		and: 'the field value is not populated'
			fieldsInfo[property].value = ''
		and: 'the find.query is based on the common attribute'
			fieldsInfo[property].find.query = [
				[	domain: 'Device',
					kv: [
						assetType: device.assetType
					]
				]
			]
		and: 'the fetchEntityByFieldMetaData method is called'
			def entity = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'the return negative value indicating an error'
			NumberUtil.isaNumber(entity)
		and: 'the property should have an error message abouth there being multiple references'
			dataImportService.FIND_FOUND_MULTIPLE_REFERENCES_MSG == fieldsInfo[property].errors[0]
	}

    void 'Test fetchEntityByFieldMetaData method with id reference to another project asset'() {
		setup:
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			String property = 'asset'

		when: 'the ETL field.value contains a numeric id of a domain entity belonging to another project'
			fieldsInfo[property].value = otherProjectDevice.id
		and: 'the fetchEntityByFieldMetaData method is called'
			def entity = dataImportService.fetchEntityByFieldMetaData(property, fieldsInfo, context)
		then: 'a -1 should be returned indicating that an explicit ID lookup failed'
			-1 == entity
    }

	void '4. test searchForDomainById method'() {
		// searchForDomainById(String propertyName, JSONObject fieldsInfo, Map context)
		setup:
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			String property = 'asset'

		when: 'calling the method without any query results or id set in value property'
			def entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'then nothing should be returned'
			!entity

		when: 'the ETL find.result contains the id of the asset'
			fieldsInfo[property].find.results = [device.id]
		and: 'calling the method'
			entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'the device should be returned'
			device == entity

		when: 'the ETL find.result is empty'
			fieldsInfo[property].find.results = []
		and: 'the ETL field.value contains the id of the device'
			fieldsInfo[property].value = device.id
		and: 'calling the method'
			entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'the device should be returned'
			device == entity

		when: 'the ETL find.result contains device2 id'
			fieldsInfo[property].find.results = [device2.id]
		and: 'the ETL field.value contains the id of device'
			fieldsInfo[property].value = device.id
		and: 'calling the method'
			entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'device2 from the find results should be returned'
			device2 == entity

		when: 'the ETL find.result contains the id of the otherProjectDevice'
			fieldsInfo[property].find.results = [otherProjectDevice.id]
		and: 'the ETL field.value is empty'
			fieldsInfo[property].value = ''
		and: 'calling the method'
			entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'no device should be returned'
			dataImportService.NOT_FOUND_BY_ID == entity

		when: 'the ETL find.result is empty'
			fieldsInfo[property].find.results = []
		and: 'the ETL field.value contains the id of the otherProjectDevice'
			fieldsInfo[property].value = otherProjectDevice.id
		and: 'calling the method'
			entity = dataImportService.searchForDomainById(property, fieldsInfo, context)
		then: 'no device should be returned'
			dataImportService.NOT_FOUND_BY_ID == entity
	}

	void '5. test findDomainByAlternateProperty method'() {
		// findDomainByAlternateProperty(String propertyName, JSONObject fieldsInfo, Map context)
		setup:
			JSONObject fieldsInfoJO = initFieldsInfoAsJSONObject()
			fieldsInfoJO.asset.value = device.assetName

		when: 'calling findDomainByAlternateProperty() with name of valid device'
			List entities = dataImportService.findDomainByAlternateProperty('asset', fieldsInfoJO, context)
		then: 'the device should be found'
			1 == entities.size()
		and: 'the device should match the expected one'
			device.assetName == entities[0].assetName

	}

	@Ignore
	void 'test recordDomainConstraintErrorsToFieldsInfoOrRecord method'() {
		// recordDomainConstraintErrorsToFieldsInfoOrRecord(Object domain, ImportBatchRecord record, Map fieldsInfo)
	}

	void '8. test bindFieldsInfoValuesToEntity method'() {
		setup:
			AssetEntity asset = new AssetEntity()
			String assetName = 'rosebud'
			String initValue = 'Value set by init'
			String fieldToIgnore = 'custom1'
			String ignoredFieldValue = 'Apparently I am being ignored'
			Map fieldInfoMap = [
				"id": [
					"value": 42,
					"originalValue": "",
					"error": false,
					"warn": false,
					"errors": [],
					"find": [
						"query": []
					]
				],
				"assetName": [
					"value": assetName,
					"originalValue": "",
					"error": false,
					"warn": false,
					"errors": [],
					"find": [
						"query": []
					]
				],
				"description": [
					"value": null,
					"originalValue": "",
					"error": false,
					"warn": false,
					"errors": [],
					"find": [
						"query": []
					],
					init: initValue
				],
				(fieldToIgnore): [
					"value": ignoredFieldValue,
					"originalValue": "",
					"error": false,
					"warn": false,
					"errors": [],
					"find": [
						"query": []
					],
				],
				maintExpDate: [
					"value": null,
					"originalValue": "",
					"error": false,
					"warn": false,
					"errors": [],
					"find": [
						"query": []
					],
					init: new Date()
				]

			]

		when: 'calling bindFieldsInfoValuesToEntity to set property values while ignoring fields'
			dataImportService.bindFieldsInfoValuesToEntity(asset, fieldInfoMap, context, [fieldToIgnore])
		then: 'the id should not be set because it is the identify and GORM would not be very happy with that'
			null == asset.id
		and: 'the asset name should be set'
			assetName == asset.assetName
		and: 'description should be set with the init value'
			initValue == asset.description
		and: 'ignored field should not be set'
			null == asset[fieldToIgnore]

		when: 'the field to set with init value already has a value'
			String setValue = 'Init should not over write this'
			asset.description = setValue
		and: 'calling bindFieldsInfoValuesToEntity to set property values and NOT ignoring fields'
			dataImportService.bindFieldsInfoValuesToEntity(asset, fieldInfoMap, context)
		then: 'the init value should not have overwritten the domain field value'
			setValue == asset.description
		and: 'the previously ignored field is now set'
			ignoredFieldValue == asset[fieldToIgnore]


		// TODO : JPM 4/2018 : Add test to try init on a reference field which should result in an error on the field
		// TODO : JPM 4/2018 : add tests for setting each data type (Date, Integer, Long, Person, etc)
	}

	@Ignore
	void 'test createReferenceDomain method'() {
		// createReferenceDomain(String propertyName, Map fieldsInfo, Map context)
		// TODO : Augusto - work on killing this one

		// Add the create block to the fieldsInfo to create a device, application, moveBundle, manufacturer
		// Won't test person, model, room/rack yet due to multiple fields in requirements
		// First query should be specify the Asset class to create

		/*
			"find": {
				"query": [
					{
						"domain": "Device",		// Set to Device or Application, etc
						"kv": {
							"assetName": "59admin",
							"manufacturer": "VMWare",
							"model": "VM",
							"serialNumber": "422e2244-f78c-2012-b56a-e435d7519abf"
						}
					},

			"create": {
				"assetName": "59admin", // random string
				"description": "59admin.moredirect.com CPU 2, Memory 16,384",
				"environment": "Production",
				"assetType": "VM",
				"os": "Red Hat Enterprise Linux 6 (64-bit)"
			}
		*/
	}

	void '10. test addErrorToFieldsInfoOrRecord method'() {
		// addErrorToFieldsInfoOrRecord(String propertyName, JSONObject fieldsInfo, ImportBatchRecord record, Map context, String errorMsg)

		when: 'we start with a new environment'
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			context.record = new ImportBatchRecord()
		then: 'there should be no errors in the field'
			[] == dataImportService.getFieldsInfoFieldErrors('asset', fieldsInfo)
		and: 'no errors at the record level'
			[] == context.record.errorListAsList()

		when: 'an error is added to a known field'
			dataImportService.addErrorToFieldsInfoOrRecord('asset', fieldsInfo, context, 'field error')
		then: 'there should be an error in the field'
			['field error'] == dataImportService.getFieldsInfoFieldErrors('asset', fieldsInfo)
		and: 'no errors at the record level'
			[] == context.record.errorListAsList()

		when: 'an error is added to an unknown field'
			dataImportService.addErrorToFieldsInfoOrRecord('fubar', fieldsInfo, context, 'record error')
		then: 'there should be the original error in the field'
			['field error'] == dataImportService.getFieldsInfoFieldErrors('asset', fieldsInfo)
		and: 'the new error at the record level'
			['record error'] == context.record.errorListAsList()
	}

	void '11. test findAndUpdateOrCreateDependency method'() {
		// findAndUpdateOrCreateDependency(AssetEntity primary, AssetEntity supporting, Map fieldsInfo, Map context)
		setup:
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			ImportBatchRecord record = new ImportBatchRecord()
			AssetDependency nullDependency = null
		and: 'the primary asset has a create element'
			fieldsInfo['asset'].create = [
				assetName: device.assetName,
				description: 'CPU 2, Memory 16,384MB',
				environment: 'Production',
				assetType: 'VM',
				manufacturer: 'VMWare',
				model: 'VM',
				os: 'Red Hat Enterprise Linux 6 (64-bit)'
			]
		and: 'the supporting asset has a create element'
			fieldsInfo['dependent'].create = [
				assetName: device2.assetName,
				description: 'CPU 2, Memory 16,384MB',
				environment: 'Production',
				assetType: 'VM',
				manufacturer: 'VMWare',
				model: 'VM',
				os: 'Red Hat Enterprise Linux 6 (64-bit)'
			]

		when: 'the method is called'
			def dependency = dataImportService.findAndUpdateOrCreateDependency(nullDependency, device, device2, fieldsInfo, context)
		then: 'a new dependency should be created'
			dependency
	}

	void '12. test tallyNumberOfErrors method'() {
		// tallyNumberOfErrors(ImportBatchRecord record, Map fieldsInfo)
		setup:
			JSONObject fieldsInfo = initFieldsInfoAsJSONObject()
			ImportBatchRecord record = new ImportBatchRecord()
		when: 'there are no errors at the record or field level'
			// default state
		then: 'there should be no errors'
			0 == dataImportService.tallyNumberOfErrors(record, fieldsInfo)

		when: 'an error is added to one of the fields'
			fieldsInfo['asset'].errors = ['there was a big screw up here']
		then: 'there should be one error'
			1 == dataImportService.tallyNumberOfErrors(record, fieldsInfo)

		when: 'two errors is added to one of the fields'
			fieldsInfo['asset'].errors = ['there was a big screw up here', 'and someone is going to pay']
		and: 'another error is recorded on a different field'
			fieldsInfo['id'].errors = ['what happened here?']
		then: 'there should be three errors'
			3 == dataImportService.tallyNumberOfErrors(record, fieldsInfo)

		when: 'an error is added to the record'
			record.addError('oops I did it again')
		then: 'there should be four errors'
			4 == dataImportService.tallyNumberOfErrors(record, fieldsInfo)

		when: 'a second error is added to the record'
			record.addError('I apparently do not learn from my mistakes')
		then: 'there should be five errors'
			5 == dataImportService.tallyNumberOfErrors(record, fieldsInfo)
	}

	@Ignore
		// generateMd5OfQuery
	void 'test findAndUpdateOrCreateDependency method'() {
		// generateMd5OfQuery
	}

	@Ignore
	// Disabled - see TM-11017
	void '14. test transformData method'() {
		setup: 'Create a DataScript, a Provider and other required data'
			String etlSourceCode = """
				read labels
				domain Dependency
				iterate {
					extract 'serverName' load 'asset' set srvNameVar
					find Device by 'assetName' with 'srvNameVar' into 'asset'
					whenNotFound 'asset' create {
						assetName srvNameVar
					}

					extract 'appName' load 'dependent' set appNameVar
					find Application by 'assetName' with appNameVar into 'dependent'
					whenNotFound 'dependent' create {
						assetName appNameVar
					}

					load 'status' with 'UnknownStatus'
					initialize 'c1' with 'from initialize command'
				}"""

			String sampleData = 'serverName,appName\nxraysrv01,bigapp'

			// Create the DataScript to be used
			Person whom = personTestHelper.createPerson()
			Provider provider = providerTestHelper.createProvider(project)
			DataScript dataScript = dataScriptTestHelper.createDataScript(project, provider, whom, etlSourceCode)

			// Create the data file to be processed
			String originalFilename = 'test.csv'
			def (fileUploadName, os) = fileSystemService.createTemporaryFile('intTest', 'csv')
			os << sampleData
			os.close()

		when: 'calling to transform the data with the ETL script'
			println "Calling dataImportService.transformEtlData()  -- THIS FAILS SILENTLY when running the test"
			Map transformResults = dataImportService.transformEtlData(project.id, dataScript.id, fileUploadName)
			println "If we get here then the issue has been solved"
			String transformedFileName = transformResults['filename']
		then: 'the results should have a filename'
			transformResults.containsKey('filename')

		when: 'parsing the content of the transformed file'
			JSONObject transformJson = JsonUtil.parseFile(fileSystemService.openTemporaryFile(transformedFileName))
		then: 'a JSON object should be created'
			transformJson != null
		and: 'the ETLInfo has the name of the temporary file'
			transformJson.ETLInfo.originalFilename == fileUploadName
		and: 'there is only one domain'
			transformJson.domains.size() == 2
		and: 'the Domain is Dependency'
			transformJson.domains[0].domain == 'Dependency'
		and: 'the data has only one element'
			transformJson.domains[0].data.size() == 1
		cleanup: 'Delete test files'
			fileSystemService.deleteTemporaryFile(fileUploadName)
			fileSystemService.deleteTemporaryFile(transformedFileName)

	}

	void '15. hammer the setDomainPropertyWithValue method'() {
		setup:
			Application application = new Application()
			// context = dataImportService.initContextForProcessBatch( ETLDomain.Dependency )
			String parentProperty = 'asset'
			Map fieldsInfo = initFieldsInfo()

		when: 'calling setDomainPropertyWithValue to set the description'
			String description = 'This is pretty cool'
			String error = dataImportService.setDomainPropertyWithValue(application, 'description', description, parentProperty, fieldsInfo, context)
		then: 'there should be no error'
			! error
		and: 'the description property should be set'
			description == application.description

		when: 'calling setDomainPropertyWithValue trying to set the moveBundle reference property'
			error = dataImportService.setDomainPropertyWithValue(application, 'moveBundle', moveBundle.name, parentProperty, fieldsInfo, context)
		then: 'there should be no errors'
			! error
		and: 'the moveBundle should be set on the domain'
			moveBundle.id == application.moveBundle?.id

		when: 'calling setDomainPropertyWithValue trying to set a blocked property'
			String propertyName = 'version'
			error = dataImportService.setDomainPropertyWithValue(application, 'version', 123, parentProperty, fieldsInfo, context)
		then: 'an appropriate error message should be returned'
			StringUtil.replacePlaceholders(dataImportService.PROPERTY_NAME_CANNOT_BE_SET_MSG, [propertyName:propertyName]) == error

	}

	@Ignore
	// TODO : JPM 4/2018 : This is not working and the code was disabled because the toSet is fucking with the order of the original list
	void '16. Test fixOrderInWhichToProcessFields method'() {
		expect:
			expectedList == dataImportService.fixOrderInWhichToProcessFields(set)
		where:
			set												| expectedList
			['a','b','c'].toSet()							| ['a','b','c']
			['a','manufacturer','b','model','c'].toSet()	| ['a','manufacturer','b','model','c']
			['a','model','b','manufacturer','c'].toSet()	| ['a','manufacturer','b','model','c']
			['model','b','manufacturer','c'].toSet()		| ['manufacturer','b','model','c']
	}

	void '17. Test the findReferenceDomainByAlternateKey method'() {
		// 	List findReferenceDomainByAlternateKey(Object entity, String refDomainPropName, String searchValue, String parentPropertyName, Map fieldsInfo, Map context)
		setup:
			AssetEntity domainObject = new AssetEntity()
			List results
			Map fieldsInfo = initFieldsInfo()

		when: 'Calling for a known Manufacturer'
			results = dataImportService.findReferenceDomainByAlternateKey(domainObject, 'manufacturer', 'HP', 'asset', fieldsInfo, context)
		then: 'one result should be returned'
			1 == results.size()

		when: 'Calling for a known alias of Manufacturer'
			results = dataImportService.findReferenceDomainByAlternateKey(domainObject, 'manufacturer', 'Hewlett Packard', 'asset', fieldsInfo, context)
		then: 'one result should be returned'
			1 == results.size()

		when: 'Calling for a non-existent Manufacturer'
			results = dataImportService.findReferenceDomainByAlternateKey(domainObject, 'manufacturer', 'WillNotFindThisMfg', 'asset', fieldsInfo, context)
		then: 'one result should be returned'
			0 == results.size()
		// and: 'An error should be logged'
	}
}