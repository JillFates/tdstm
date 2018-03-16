package net.transitionmanager.service

import spock.lang.Unroll
import spock.lang.Ignore

import grails.test.spock.IntegrationSpec
import grails.validation.ValidationException
// import net.transitionmanager.service.InvalidParamException
// import net.transitionmanager.service.ProjectRequiredException
// import net.transitionmanager.command.CredentialCommand
// import net.transitionmanager.domain.Credential
// import net.transitionmanager.domain.Provider
// import net.transitionmanager.service.CredentialService
// import net.transitionmanager.service.DomainUpdateException
// import net.transitionmanager.service.EmptyResultException
// import net.transitionmanager.service.SecurityService
// import org.codehaus.groovy.grails.commons.GrailsApplication
// import org.hibernate.SessionFactory
// import test.helper.CredentialTestHelper
import net.transitionmanager.service.DataImportService
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper
import com.tdssrc.grails.StringUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.etl.ETLDomain


class DataImportServiceIntegrationSpec extends IntegrationSpec {
    DataImportService dataImportService
    //GrailsApplication grailsApplication
    //SessionFactory sessionFactory

    ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	Project project, otherProject
	MoveBundle moveBundle
	AssetEntity device, device2, otherProjectDevice
	Map fieldsInfo
	Map context

	void setup() {
		// Setup primary project & bundle
		project = projectTestHelper.createProject(null)
		moveBundle = moveBundleTestHelper.createBundle(project, null)

		// Create device and device2
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device.assetType = 'Server'
		device.save()

		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2.assetType = 'Server'
		device2.save()

		// Create a second project with a device with the same name and type as device above
		otherProject = projectTestHelper.createProject()
		def otherMoveBundle = moveBundleTestHelper.createBundle(otherProject, null)
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, otherMoveBundle)
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.save()

		// Setup the context that the DataImportService utilizes for most methods
		context = dataImportService.initContextForProcessBatch( ETLDomain.Dependency )
		context.record = new ImportBatchRecord(sourceRowId:5)

		// A fields map for an AssetDependency
		fieldsInfo = [
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
			"createdBy": [
				"value": "Bill Z. Bubb",
				"originalValue": "",
				"error": false,
				"warn": false,
				"errors": [],
				"find": [
					"query": []
				]
			],
			"c1": [
				"value": "",
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

	void '1. give the performQueryAndUpdateFindElement method a spin'() {
		// performQueryAndUpdateFindElement(Project project, String propertyName, Map fieldsInfo, Map context)
		given:
			Object entity
			String propertyName = 'asset'
			String assetType = 'Server'

		when: 'the query is by the assetName of an existing asset'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName
					]
				]
			]
		then: 'calling performQueryAndUpdateFindElement should return the expected record'
			[device] ==  dataImportService.performQueryAndUpdateFindElement(project, propertyName, fieldsInfo, context)


		when: 'the query is by the assetName of an non-existing asset'
			fieldsInfo[propertyName].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: 'A bogus asset name that does not exist for certain!'
					]
				]
			]
		then: 'calling performQueryAndUpdateFindElement should return an empty list'
			[] ==  dataImportService.performQueryAndUpdateFindElement(project, propertyName, fieldsInfo, context)


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
			[device] ==  dataImportService.performQueryAndUpdateFindElement(project, propertyName, fieldsInfo, context)


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
			List entities =  dataImportService.performQueryAndUpdateFindElement(project, propertyName, fieldsInfo, context)
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
			[device] ==  dataImportService.performQueryAndUpdateFindElement(project, propertyName, fieldsInfo, context)
		and: 'the find.matchOn should indicate the third criteria found the result'
			3 == fieldsInfo[propertyName].find.matchOn
		and: 'the find.results should have the id of the expected record'
			[device.id] == fieldsInfo[propertyName].find.results

	}

	void '2. beat up on classOfDomainProperty'() {
		// classOfDomainProperty(String propertyName, Map fieldsInfo, Map context)

		given:
			Class clazz

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

		when: 'called for a non-asset reference property (Person)'
			clazz = dataImportService.classOfDomainProperty('createdBy', fieldsInfo, context)
		then: 'a Person domain class should be returned'
			clazz == Person

		when: 'called for the a non-identity or reference field'
			clazz = dataImportService.classOfDomainProperty('c1', fieldsInfo, context)
			List errors = dataImportService.getFieldsInfoFieldErrors('c1', fieldsInfo)
		then: 'no domain class should be returned'
			clazz == null
		and: 'there should be an error on the field'
			errors.size() == 1
		and: 'the error message should be what is expected'
			dataImportService.WHEN_NOT_FOUND_PROPER_USE_MSG == errors[0]

	}

    void '3. testing the snarts out of the lookupDomainRecordByFieldMetaData method'() {
        setup:
			String domainName = context.domainName
			String property = 'asset'
		and: 'any previous error messages have been cleared out'
			dataImportService.resetRecordAndFieldsInfoErrors(context.record, fieldsInfo)

        when: 'called with no id and an empty query section'
			def entity = dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)
        then: 'no entity should be returned'
			! entity
		and: 'a particular error message should be recorded in the fieldsInfo'
			dataImportService.NO_FIND_QUERY_SPECIFIED_MSG == fieldsInfo[property].errors[0]

		when: 'called with the value containing the asset id'
			fieldsInfo[property].value = device.id
		and: 'any previous error messages have been cleared out'
			dataImportService.resetRecordAndFieldsInfoErrors(context.record, fieldsInfo)
			entity = dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)
		then: 'the asset should be found'
			entity
		and: 'the asset should match the one attempting to be found'
			device.id == entity.id

		when: 'record has a query with the asset name'
			fieldsInfo[property].find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName
					]
				]
			]
		and: 'has no id value'
			fieldsInfo[property].value = ''
		then: 'the calling lookupDomainRecordByFieldMetaData should should return the access'
			device == dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)

		when: 'the cache values are overwritten so that we can tell the results came from cache'
			String overwritten = 'Cache entry was overwritten'
			Set keys = context.cache.keySet()
			keys.each { key -> context.cache[key] = overwritten }
		and: 'lookupDomainRecordByFieldMetaData is called again with the same query information'
			entity = dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)
		then: 'the object returned should have come from found the cache with the overwritten value'
			overwritten == entity

		when: 'there are two entities that have a common attribute'
			// done in the spec setup
		and: 'the query is based on the common attribute'
			fieldsInfo[property].value = ''
			fieldsInfo[property].find.query = [
				[	domain: 'Device',
					kv: [
						assetType: device.assetType
					]
				]
			]
		and: 'the field has no id value'
			fieldsInfo[property].value = ''
		and: 'any previous error messages have been cleared out'
			dataImportService.resetRecordAndFieldsInfoErrors(context.record, fieldsInfo)
		and: 'the lookupDomainRecordByFieldMetaData method is called'
			entity = dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)
		then: 'the return value should be -2 indicating multiple entities found'
			-2 == entity
		and: 'an error should be reported that there were multiple references'
			dataImportService.FIND_FOUND_MULTIPLE_REFERENCES_MSG == fieldsInfo[property].errors[0]

		when: 'the ETL has the id of the domain entity from the other project'
			fieldsInfo[property].value = otherProjectDevice.id
		and: 'we have cleared out previous error messages'
			dataImportService.resetRecordAndFieldsInfoErrors(context.record, fieldsInfo)
		and: 'the lookupDomainRecordByFieldMetaData method is called'
			entity = dataImportService.lookupDomainRecordByFieldMetaData(project, property, fieldsInfo, context)
		then: 'a -1 should be returned indicating that an explicit ID lookup failed (in this case because asset was on another project)'
			-1 == entity
    }

	@Ignore
	void '4. test findDomainById method'() {
		// findDomainById(Project project, Class domainClass, String propertyName, JSONObject fieldsInfo, Map context)
	}

	@Ignore
	void '5. test findDomainByAlternateProperty method'() {
		// (Project project, Class domainClass, String propertyName, JSONObject fieldsInfo)
	}

	@Ignore
	void '6. test lookupDomainRecordByFieldMetaData method'() {
		// lookupDomainRecordByFieldMetaData(Project project, String domainClassName, String propertyName, Map fieldsInfo, Map context)
	}

	@Ignore
	void '7. test recordDomainConstraintErrorsToFieldsInfoOrRecord method'() {
		// recordDomainConstraintErrorsToFieldsInfoOrRecord(Object domain, ImportBatchRecord record, Map fieldsInfo)
	}

	@Ignore
	void '8. test bindFieldsInfoValuesToEntity method'() {
		// bindFieldsInfoValuesToEntity(Object domain, Map fieldsInfo, List fieldsToIgnore=[] )
	}

	@Ignore
	void '9. test createReferenceDomain method'() {
		// createReferenceDomain(Project project, String propertyName, Map fieldsInfo, Map context)
	}

	@Ignore
	void '10. test addErrorToFieldsInfoOrRecord method'() {
		// addErrorToFieldsInfoOrRecord(String propertyName, JSONObject fieldsInfo, ImportBatchRecord record, String errorMsg)
	}

	@Ignore
	void '11. test findAndUpdateOrCreateDependency method'() {
		// findAndUpdateOrCreateDependency(AssetDependency dependency, AssetEntity primary, AssetEntity supporting )
	}

	@Ignore
	void '12. test tallyNumberOfErrors method'() {
		// tallyNumberOfErrors(ImportBatchRecord record, Map fieldsInfo)
	}

	@Ignore
		// generateMd5OfQuery
	void '13. test findAndUpdateOrCreateDependency method'() {
		// generateMd5OfQuery
	}
}