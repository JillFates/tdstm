import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.dataImport.SearchQueryHelper
import net.transitionmanager.imports.DataScript
import net.transitionmanager.imports.ImportBatchRecord
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelAlias
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.action.Provider
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.security.SecurityService
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper

@Integration
@Rollback
class DataImportServiceIntegrationSpec extends Specification {
	@Shared
	AssetEntityTestHelper assetEntityTestHelper

	@Shared
    DataImportService dataImportService

	@Shared
    SecurityService securityService

	@Shared
	DataScriptTestHelper dataScriptTestHelper

	@Shared
	FileSystemService fileSystemService

	@Shared
	MoveBundleTestHelper moveBundleTestHelper

	@Shared
	PersonTestHelper personTestHelper

	@Shared
    ProjectTestHelper projectTestHelper

	@Shared
	ProviderTestHelper providerTestHelper

	@Shared
	Project project
	@Shared
	Project otherProject
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

	@Shared
	Person whom

	@Shared
	boolean initialized = false

	void setupSpec() {

	}

	void setup() {
		assetEntityTestHelper = new AssetEntityTestHelper()
		dataScriptTestHelper = new DataScriptTestHelper()
		moveBundleTestHelper = new MoveBundleTestHelper()
		personTestHelper = new PersonTestHelper()
		projectTestHelper = new ProjectTestHelper()
		providerTestHelper = new ProviderTestHelper()
		whom = personTestHelper.createPerson()
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject,
																	 moveBundleTestHelper.createBundle(otherProject, null))

		def adminUser = personTestHelper.createUserLoginWithRoles(whom, ["${SecurityRole.ROLE_ADMIN}"])
		securityService.assumeUserIdentity(adminUser.username, false)

		context = dataImportService.initContextForProcessBatch(project, ETLDomain.Dependency)
		context.record = new ImportBatchRecord(sourceRowId: 1)

		device.assetType = 'Server'
		device.priority = 6
		device.purchasePrice = 1.25
		device.retireDate = new Date()

		device.save()

		device2.assetType = 'Server'
		device2.save()

		// Create a second project with a device with the same name and type as device above
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.save()
	}

	void '1. give the performQueryAndUpdateFindElement method a spin'() {
		// performQueryAndUpdateFindElement(String propertyName, Map fieldsInfo, Map context)
		given:
			Object entity
			String propertyName = 'asset'
			String assetType = 'Server'
			Map fieldsInfo = initFieldsInfoForDependency()

		when: 'the query is by the assetName of an existing asset'
			initializeFindElement(propertyName, fieldsInfo)
			setQueryElement(propertyName, fieldsInfo, 'Device', [assetName: device.assetName], true, device.id)
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			device.id ==  SearchQueryHelper.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)[0].id

		when: 'the query is by the assetName of an non-existing asset'
			setQueryElement(propertyName, fieldsInfo, 'Device', [assetName:'A bogus asset name that does not exist for certain!'], false)
			fieldsInfo.value = ''
		then: 'calling performQueryAndUpdateFindElement should return an empty list'
			[] ==  SearchQueryHelper.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)

		when: 'there is an asset with the same assetName in a different project'
			// Done in spec setup
		and: 'the query is by assetName only'
			setQueryElement(propertyName, fieldsInfo, 'Device', [assetName:device.assetName], true, device.id)
			fieldsInfo.value = ''
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			device.id ==  SearchQueryHelper.performQueryAndUpdateFindElement(propertyName, fieldsInfo, context)[0].id

		when: 'there is a second asset with the same assetType'
			// Done in spec setup
		and: 'the query is by the assetType only'
			setQueryElement(propertyName, fieldsInfo, 'Device', [assetType: assetType], false)
		and: 'calling performQueryAndUpdateFindElement that should return multiple records'
			List entities =  SearchQueryHelper.performQueryAndUpdateFindElement(propertyName, new JSONObject(fieldsInfo), context)
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

	}

	void '2. beat up on classOfDomainProperty'() {
		// classOfDomainProperty(String propertyName, Map fieldsInfo, Map clazz)

		given:
			Class clazz
			//JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
			Map fieldsInfo = initFieldsInfoForDependency()
			String errMsg

		when: 'called for the identity of the object'
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty('id', fieldsInfo, AssetDependency)
		then:
			AssetDependency == clazz
		when: 'called for the asset reference'
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty('asset', fieldsInfo, AssetDependency)
		then: 'an AssetEntity should be returned'
			AssetEntity == clazz

		when: 'the first find query was for the Application domain'
			fieldsInfo.asset.find.query = [ [
				domain: 'Application',
				assetName: 'foo'
			] ]
		and: 'and calling for the asset reference'
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty('asset', fieldsInfo, AssetDependency)
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
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty('asset', fieldsInfo, AssetDependency)
		then: 'a Database class should be returned'
			Database == clazz


		when: 'called for an invalid property name'
			String propertyName = 'xyzzy'
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty(propertyName, fieldsInfo, AssetDependency)
			String expectedError = StringUtil.replacePlaceholders(SearchQueryHelper.PROPERTY_NAME_NOT_IN_DOMAIN, [propertyName:propertyName])
		then: 'an error should be reported'
			errMsg
		and: 'the error message should be what is expected'
			expectedError == errMsg

		// when: 'called for a non-asset reference property (Person)'
		// 	clazz = SearchQueryHelper.classOfDomainProperty('createdBy', fieldsInfo, AssetDependency)
		// then: 'a Person domain class should be returned'
		// 	clazz == Person

		when: 'called for the a non-identity or reference field'
			fieldsInfo['c1'] = [errors:[]]
			(clazz,errMsg) = SearchQueryHelper.classOfDomainProperty('c1', fieldsInfo, AssetDependency)
		then: 'no domain class should be returned'
			clazz == null
		and: 'there should be an error on the field'
			errMsg
		and: 'the error message should be what is expected'
			SearchQueryHelper.WHEN_NOT_FOUND_PROPER_USE_MSG == errMsg

	}

	// Need to decide if there is any information to deal with de-dupping and error if not
	@Ignore
    void 'Test fetchEntityByFieldMetaData for no find.query specified'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

        when: 'called with no id and an empty query section'
			def entity = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
        then: 'no entity should be returned'
			! entity
		and: 'a particular error message should be recorded in the fieldsInfo'
			String errMsg = fieldsInfo[property].errors[0]
			SearchQueryHelper.NO_FIND_QUERY_SPECIFIED_MSG == errMsg
	}

    void 'Test fetchEntityByFieldMetaData for find by field.value set to asset ID number'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field value contains the asset id as a numeric value'
			fieldsInfo[property].value = device.id
		and: 'the method is called'
			def entity = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the asset should be found'
			entity
		and: 'the asset should match the one attempting to be found'
			device.id == entity.id
	}

	void 'Test fetchEntityByFieldMetaData for find by field.value set to alternate key'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field value contains the alternate key value (assetName)'
			fieldsInfo[property].value = device.assetName
		and: 'the method is called'
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the asset should be found'
			result
		and: 'the asset should match the one attempting to be found'
			device.id == result.id
	}

    void 'Test fetchEntityByFieldMetaData for #2 - id method'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field value contains the id of the entity'
			fieldsInfo[property].value = device.id
		and: 'the method is called'
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the expected asset should be returned'
			device.id == result.id
	}

    void 'Test fetchEntityByFieldMetaData for #3 - single result'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field query contains one result with the id of the asset'
			fieldsInfo[property].value = device.id
		and: 'the method is called'
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the expected asset should be returned'
			device.id == result.id
	}

    void 'Test fetchEntityByFieldMetaData for #4 re-execute queries'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field query contains two searches with 2nd by the assetName and assetType'
			setQueryElement(property, fieldsInfo, 'Device', [assetName: 'BOGUS NAME - ' + new Date()], false)
			addQueryElement(property, fieldsInfo, 'Device', [assetName: device.assetName, assetType: device.assetType], false)
		and: 'field.value is empty'
			fieldsInfo[property].value = ''
		and: 'the method is called'
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the expected asset should be returned'
			device.id == result.id
		and: 'the matchOn should be for the 2nd query'
			1 == fieldsInfo[property].find.matchOn
	}

    void 'Test fetchEntityByFieldMetaData for #5 alternateKey method'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()

		when: 'the field query contains the search by the assetName and assetType'
			fieldsInfo[property].value = device.assetName
		and: 'the method is called'
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the expected asset should be returned'
			device.id == result.id
	}

	// Need to implement
	@Ignore
    void 'Test fetchEntityByFieldMetaData for #6 - AssetDependency'() {
	}

    void 'Test fetchEntityByFieldMetaData for caching'() {
        setup:
			String property = 'asset'
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
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
			def result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
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
			result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
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
		// 	result = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		// then: 'the object returned should by the override number indicating that the cache is working correctly'
		// 	overwritten == result
	}

    void 'Test fetchEntityByFieldMetaData method finding duplicates'() {
		setup:
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
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
			def entity = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'the return negative value indicating an error'
			NumberUtil.isaNumber(entity)
		and: 'the property should have an error message abouth there being multiple references'
			SearchQueryHelper.FIND_FOUND_MULTIPLE_REFERENCES_MSG == context.searchQueryHelperErrors[0]
	}

    void 'Test fetchEntityByFieldMetaData method with id reference to another project asset'() {
		setup:
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
			String property = 'asset'

		when: 'the ETL field.value contains a numeric id of a domain entity belonging to another project'
			fieldsInfo[property].value = otherProjectDevice.id
		and: 'the fetchEntityByFieldMetaData method is called'
			def entity = SearchQueryHelper.findEntityByMetaData(property, fieldsInfo, context)
		then: 'a -1 should be returned indicating that an explicit ID lookup failed'
			-1 == entity
    }

	void '4. test fetchEntityById method'() {
		// fetchEntityById(String propertyName, JSONObject fieldsInfo, Map context)
		setup:
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
			String property = 'asset'

		when: 'calling the method field.value for the id set the property'
			def entity = SearchQueryHelper.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		then: 'then nothing should be returned'
			!entity

		when: 'the ETL property value contains the id of the asset'
			fieldsInfo[property].value = device.id
		and: 'calling the method'
			entity = SearchQueryHelper.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		then: 'the device should be returned'
			device.id == entity.id

		when: 'the ETL field.value contains the id of device'
			fieldsInfo[property].value = device2.id
		and: 'calling the method'
			entity = SearchQueryHelper.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		then: 'device2 from the find results should be returned'
			device2.id == entity.id

		when: 'the ETL field.value contains an invalid id for a device'
			fieldsInfo[property].value = 9999999999999
		and: 'calling the method'
			entity = SearchQueryHelper.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		then: 'device2 from the find results should be returned'
			SearchQueryHelper.NOT_FOUND_BY_ID == entity

		when: 'the ETL field.value contains the id of the otherProjectDevice'
			fieldsInfo[property].value = otherProjectDevice.id
		and: 'calling the method'
			entity = SearchQueryHelper.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		then: 'no device should be returned'
			SearchQueryHelper.NOT_FOUND_BY_ID == entity

		// when: 'the ETL find.result is empty'
		// 	fieldsInfo[property].find.results = []
		// and: 'the ETL field.value contains the id of the device'
		// 	fieldsInfo[property].value = device.id
		// and: 'calling the method'
		// 	entity = dataImportService.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		// then: 'the device should be returned'
		// 	device == entity

		// when: 'the ETL find.result contains the id of the otherProjectDevice'
		// 	fieldsInfo[property].find.results = [otherProjectDevice.id]
		// and: 'the ETL field.value is empty'
		// 	fieldsInfo[property].value = ''
		// and: 'calling the method'
		// 	entity = dataImportService.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		// then: 'no device should be returned'
		// 	SearchQueryHelper.NOT_FOUND_BY_ID == entity

		// when: 'the ETL find.result is empty'
		// 	fieldsInfo[property].find.results = []
		// and: 'the ETL field.value contains the id of the otherProjectDevice'
		// 	fieldsInfo[property].value = otherProjectDevice.id
		// and: 'calling the method'
		// 	entity = dataImportService.fetchEntityById(AssetEntity, property, fieldsInfo, context)
		// then: 'no device should be returned'
		// 	SearchQueryHelper.NOT_FOUND_BY_ID == entity
	}

	// This method was removed - check to see if there is an alternative that we are going to use
	@Ignore
	void '5. test findDomainByAlternateProperty method'() {
		// findDomainByAlternateProperty(String propertyName, JSONObject fieldsInfo, Map context)
		setup:
			JSONObject fieldsInfoJO = initFieldsInfoForDependencyAsJSONObject()
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
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
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

	void '11. test createEntity method for Assets'() {
		// private Object createEntity(Class domainClass, Map fieldsInfo, Map context) {
		setup:
			Map assetFieldsInfo = [:]

		when: 'calling the createEntity for a device'
			Object entity = dataImportService.createEntity(Application, assetFieldsInfo, context)
		then: 'we should get an AssetEntity'
			(entity instanceof AssetEntity)
		and: 'the moveBundle is set to the TBD'
			'TBD' == entity.moveBundle.name
		and: 'the owner is the client of the project'
			context.project.client.id == entity.owner.id
		and: 'the project is assigned'
			context.project.id == entity.project.id
		and: 'the modifiedBy is the person running the import'
			context.whom
			entity.modifiedBy
			context.whom.id == entity.modifiedBy.id

		when: 'the fieldsInfo has a bundle defined'
			initializeFieldElement('moveBundle', assetFieldsInfo, moveBundle.name)
		and: 'calling createEntity'
			entity = dataImportService.createEntity(Application, assetFieldsInfo, context)
		then: 'the default bundle should not be assigned'
			null == entity.moveBundle
	}

	void '12. test tallyNumberOfErrors method'() {
		// tallyNumberOfErrors(ImportBatchRecord record, Map fieldsInfo)
		setup:
			JSONObject fieldsInfo = initFieldsInfoForDependencyAsJSONObject()
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
			Provider provider = providerTestHelper.createProvider(project)
			DataScript dataScript = dataScriptTestHelper.createDataScript(project, provider, whom, etlSourceCode)

			// Create the data file to be processed
			String originalFilename = 'test.csv'
			def (fileUploadName, os) = fileSystemService.createTemporaryFile('intTest', 'csv')
			os << sampleData
			os.close()

		when: 'calling to transform the data with the ETL script'
			Map transformResults = dataImportService.transformEtlData(project.id, dataScript.id, fileUploadName)
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
			transformJson.domains.size() == 1
		and: 'the Domain is Dependency'
			transformJson.domains[0].domain == 'Dependency'
		and: 'the data has only one element'
			transformJson.domains[0].data.size() == 1
		cleanup: 'Delete test files'
			fileSystemService.deleteTemporaryFile(fileUploadName)
			fileSystemService.deleteTemporaryFile(transformedFileName)

	}

	// This method was replaced with bind...?
	@Ignore
	void '15. hammer the setDomainPropertyWithValue method'() {
		setup:
			Application application = new Application()
			// context = dataImportService.initContextForProcessBatch( ETLDomain.Dependency )
			String parentProperty = 'asset'
			Map fieldsInfo = initFieldsInfoForDependency()

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

	// This method was replaced with something else
	@Ignore
	void '17. Test the findReferenceDomainByAlternateKey method'() {
		// 	List findReferenceDomainByAlternateKey(Object entity, String refDomainPropName, String searchValue, String parentPropertyName, Map fieldsInfo, Map context)
		setup:
			AssetEntity domainObject = new AssetEntity()
			List results
			Map fieldsInfo = initFieldsInfoForDependency()

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

	void '18 Test the _hasSingleFindResult method'() {
		setup:
			Map fi = initFieldsInfoForDependency()
			String fieldName = 'asset'

		when: 'a field does not have any find results'
			initializeFindElement(fieldName, fi)
			SearchQueryHelper.hasSingleFindResult(fieldName, fi)
		then: 'calling _hasSingleFindResult should return false'
			! SearchQueryHelper.hasSingleFindResult(fieldName, fi)

		when: 'the field has one result'
			fi[fieldName].find.results << 123
		then:  'calling _hasSingleFindResult should return true'
			SearchQueryHelper.hasSingleFindResult(fieldName, fi)

		when: 'the field has more than one result'
			fi[fieldName].find.results << 456
		then:  'calling _hasSingleFindResult should return false'
			! SearchQueryHelper.hasSingleFindResult(fieldName, fi)
	}

	void '19 Test the SearchQueryHelper.hasFindQuery method'() {
		setup:
			Map fieldsInfo = initFieldsInfoForDependency()
			String fieldName = 'asset'

		when: 'a field does not have any query specified'
			initializeFindElement(fieldName, fieldsInfo)
		then: 'calling _hasFindQuery should return false'
			! SearchQueryHelper.hasFindQuery(fieldName, fieldsInfo)

		when: 'a field has one query specified'
			addQueryElement(fieldName, fieldsInfo, 'Application', [id:123], true, 123)
		then: 'calling _hasFindQuery should return true'
			SearchQueryHelper.hasFindQuery(fieldName, fieldsInfo)

		when: 'a field has more than one query specified'
			addQueryElement(fieldName, fieldsInfo, 'Application', [assetName: 'abc123'], false)
		then: 'calling _hasFindQuery should still return true'
			SearchQueryHelper.hasFindQuery(fieldName, fieldsInfo)
	}

	@Ignore
	void '20 Test the bindFieldsInfoValuesToEntity method for bugs'() {
		given: 'a fieldsInfo for a device'
			Map fieldsInfo = initFieldsInfoForDevice()
			AssetType assetType = AssetType.VM
			Integer priority = 6
			Double price = 1.25
			Date retire = new Date()
			SizeScale scale = SizeScale.TB
			Person clientStaff1 = personTestHelper.createPerson(whom, project.client, project)
			Person clientStaff2 = personTestHelper.createPerson(whom, project.client, project)

		and: 'a new asset is instanciated'
			AssetEntity server = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		and: 'an import context is created for Devices'
			context = dataImportService.initContextForProcessBatch( project, ETLDomain.Device )
			context.record = new ImportBatchRecord(sourceRowId:1)

		when: 'the fieldsInfo field values are set'
			initializeFieldElement('assetName', fieldsInfo, server.assetName)
		and: 'String, Integer, Double, Enum and Date fields are set'
			initializeFieldElement('assetType', fieldsInfo, assetType)
			initializeFieldElement('priority', fieldsInfo, priority)
			initializeFieldElement('purchasePrice', fieldsInfo, price)
			initializeFieldElement('retireDate', fieldsInfo, retire)
			initializeFieldElement('scale', fieldsInfo, scale)
			initializeFieldElement('modifiedBy', fieldsInfo, clientStaff1.toString())
			/*
			room/location
			rack
			manufacturer
			model
			modifiedBy (person)
			*/

		then: 'calling bindFieldsInfoValuesToEntity should not error'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, [])
		and: 'there should be dirty fields'
			GormUtil.hasUnsavedChanges(server)

		when: 'the server is saved'
			server.save(flush:true)
			server.refresh()
		then: 'there should be no unsaved changes'
			! GormUtil.hasUnsavedChanges(server)
		and: 'the device values should be as were set'
			server.assetType == assetType.toString()
			server.priority == priority
			server.purchasePrice == price
			server.retireDate == retire
			server.scale == scale
			server.modifiedBy == clientStaff1
		and: 'calling bindFieldsInfoValuesToEntity should not error'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, [])
		and: 'there should be NO dirty fields'
			! GormUtil.hasUnsavedChanges(server)

		when: 'a field is changed'
			fieldsInfo.priority.value = 2
		and: 'calling bindFieldsInfoValuesToEntity with that field being ignored'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, ['priority'])
		then: 'there should be NO dirty fields'
			! GormUtil.hasUnsavedChanges(server)

		when: 'calling again without the ignore field'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, [])
		then: 'there should be a dirty field'
			GormUtil.hasUnsavedChanges(server)
		and: 'the field that is dirty should be the priority field'
			['priority'] == server.dirtyPropertyNames

		when: 'setting the bundle to a different bundle name'
			MoveBundle mb2 = moveBundleTestHelper.createBundle(project, null)
			initializeFieldElement('moveBundle', fieldsInfo, mb2.name)
		and: 'changing a reference to another person by name'
			initializeFieldElement('modifiedBy', fieldsInfo, clientStaff2.toString())
		and: 'calling bindFieldsInfoValuesToEntity'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, [])
		then: 'the server move bundle should now reference the new bundle'
			server.moveBundle.id == mb2.id
		and: 'the priority should be changed as well'
			server.priority == 2
			server.modifiedBy == clientStaff2

		when: 'saving the changes after the latest changes'
			server.save(flush:true)
			server.refresh()
		then: 'the server move bundle should now reference the new bundle'
			server.moveBundle.id == mb2.id
		and: 'the modifiedBy should be set to the 2nd staff'
			server.modifiedBy == clientStaff2

		when: 'changing a reference to a Person by email (uppercase to validate case-insensitive support)'
			initializeFieldElement('modifiedBy', fieldsInfo, clientStaff1.email.toUpperCase())
		and: 'calling bindFieldsInfoValuesToEntity'
			dataImportService.bindFieldsInfoValuesToEntity(server, fieldsInfo, context, [])
		then: 'the modifiedBy should be assigned to the first staff again'
			server.modifiedBy == clientStaff1


	}

	void '21 Test the SearchQueryHelper.fetchEntityByAlternateKey method'() {
		setup:
			String manufacturerAliasName = 'Hewlett Packard'
			String manufacturerName = 'HP'
			String modelAliasName = 'BL460C G1'
			String modelName = 'ProLiant BL460c G1'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			initializeModel(modelName, modelAliasName, manufacturer)

			def context = [
					  domainClass: AssetEntity
			]

			def fieldsInfo = [
					  manufacturer: [
								 value: manufacturerName
					  ]
			]

		when: 'A Manufacturer is seek by alias'
			Map retVal = SearchQueryHelper.fetchEntityByAlternateKey(Manufacturer, manufacturerAliasName, '', [:], context)

		then: 'the Manufacturer is found'
			retVal != null
			retVal.error == ''
			retVal.entities.size() > 0
			((Manufacturer)retVal.entities[0]).name == manufacturerName

		when: 'A Model from a Manufacturer is seek by alias'
			retVal = SearchQueryHelper.fetchEntityByAlternateKey(Model, modelAliasName, 'model', fieldsInfo, context)

		then: 'the Manufacturer is found'
			retVal != null
			retVal.error == ''
			retVal.entities.size() > 0
			((Model)retVal.entities[0]).modelName == modelName
	}

	// void '20 Test the _fetchEntityByFindResults method'() {
	// 	_fetchEntityByFindResults(fieldName, fieldsInfo, context)
	// }

	/******************************************************
	 * Utility variables and methods for the Spec
	 ******************************************************/

	// TODO get from ETL logic
	static final Map FIND_DEF = [
		query: [],
		results: [],
		errors: [],
		matchOn: null
	]

	static final Map FIELD_DEF = [
		value: null,
		originalValue: null,
		errors: [],
		find: CollectionUtils.deepClone(FIND_DEF)
	]

	/**
	 * Used to initialize the find element for a field
	 */
	private void initializeFieldElement(String fieldName, Map fieldsInfo, Object value) {
		// Clone the Map
		fieldsInfo.put(fieldName, CollectionUtils.deepClone(FIELD_DEF))
		fieldsInfo[fieldName].value = value
	}

	/**
	 * Used to initialize the find element for a field
	 */
	private void initializeFindElement(String fieldName, Map fieldsInfo) {
		fieldsInfo[fieldName].find = CollectionUtils.deepClone(FIND_DEF)
	}

	/**
	 * Used to reset the find/query element for a given property and the set a new query
	 */
	private void setQueryElement(String fieldName, Map fieldsInfo, String domain, Map kv, boolean matchOn, Long result=null) {
		initializeFieldElement(fieldName, fieldsInfo, null)
		addQueryElement(fieldName, fieldsInfo, domain, kv, matchOn, result)
	}

	/**
	 * Used to populate the find query with a particular query
	 */
	private void addQueryElement(String fieldName, Map fieldsInfo, String domain, Map kv, boolean matchOn, Long result=null) {
		if (result) {
			fieldsInfo[fieldName].find.results << result
		}
		fieldsInfo[fieldName].find.query << [
			domain: domain,
			kv: kv
		]
		if (matchOn) {
			fieldsInfo[fieldName].find.matchOn = fieldsInfo[fieldName].find.query.size()
		}
	}

	private JSONObject initFieldsInfoForDependencyAsJSONObject() {
		new JSONObject(initFieldsInfoForDependency())
	}

	/**
	 * Used to generate the necessary initFieldsInfoForDependency map
	 */
	private Map initFieldsInfoForDependency() {
		Map fieldsInfo = [:]
		initializeFieldElement('id', fieldsInfo, '')
		initializeFieldElement('asset', fieldsInfo, '')
		initializeFieldElement('dependent', fieldsInfo, '')
		initializeFieldElement('type', fieldsInfo, 'Hosts')
		initializeFieldElement('status', fieldsInfo, 'Archived')
		initializeFieldElement('dataFlowFreq', fieldsInfo, 'weekly')
		initializeFieldElement('c2', fieldsInfo, 'This is awesome')
		return fieldsInfo
	}

	/**
	 * Used to generate the necessary initFieldsInfoForDevice map
	 */
	private Map initFieldsInfoForDevice() {
		Map fieldsInfo = [:]
		initializeFieldElement('id', fieldsInfo, '')
		initializeFieldElement('assetName', fieldsInfo, '')
		return fieldsInfo
	}

	/**
	 * Creates a Manufacturer and an alias in case that it doesn't exist for testing
	 * @param manufacturerName
	 * @param manufacturerAliasName
	 * @return
	 */
	private Manufacturer initializeManufacturer(String manufacturerName, String manufacturerAliasName) {
		ManufacturerAlias manufacturerAlias = ManufacturerAlias.findByName(manufacturerAliasName)

		Manufacturer manufacturer
		if (manufacturerAlias) {
			manufacturer = manufacturerAlias.manufacturer
		} else {
			manufacturer = Manufacturer.findOrSaveWhere( name: manufacturerName )
			new ManufacturerAlias(
					  name: manufacturerAliasName, manufacturer: manufacturer
			).save(flush: true)
		}

		return manufacturer
	}

	/**
	 * Create a Model and alias linked to a Manufacturer in case that it doesn't exist for testing
	 * @param modelName
	 * @param modelAliasName
	 * @param manufacturer
	 * @return
	 */
	private Model initializeModel(String modelName, String modelAliasName, Manufacturer manufacturer) {
		ModelAlias modelAlias = ModelAlias.findByName(modelAliasName)

		Model model
		if ( modelAlias ) {
			model = modelAlias.model
		} else {
			model = Model.findOrSaveWhere( modelName:modelName , manufacturer: manufacturer )
			new ModelAlias(
					  name: modelAliasName, model: model, manufacturer: manufacturer
			).save(flush: true)
		}

		return model
	}

}