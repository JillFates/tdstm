package net.transitionmanager.service

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
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import com.tds.asset.AssetEntity
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper
import com.tdssrc.grails.StringUtil
import com.tdsops.tm.enums.domain.AssetClass


class DataImportServiceIntegrationSpec extends IntegrationSpec {
    DataImportService dataImportService
    //GrailsApplication grailsApplication
    //SessionFactory sessionFactory

    ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	MoveBundleTestHelper moveBundleTestHelper = new MoveBundleTestHelper()
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	Project project
	MoveBundle moveBundle
	AssetEntity device

	void setup() {
		project = projectTestHelper.createProject(null)
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
	}

    void '1. testing the snarts out of the lookupDomainRecordByFieldMetaData method'() {
        setup:
			Map rowEtl = [
				"op": "I",
				"warn": false,
				"duplicate": false,
				"errors": [],
				"fields": [
					"asset": [
						"value": "",
						"originalValue": "",
						"error": false,
						"warn": false,
						"find": [
							"query": []
						]
					]
				]
			]

			Map context = [cache: [:]]
			String property = 'asset'
			String domainName = 'Device'
			String noFindElseFind = "Reference lookup for property $property has no find/elseFind criteria"
			String referenceNotFound = "Reference lookup by ID for property $property was not found"

        when: 'called with no id and an empty query section'
			def (entity, error) = dataImportService.lookupDomainRecordByFieldMetaData(project, domainName, property, rowEtl, context)
        then:
			noFindElseFind == error
		// and: 'The cache should have a reference to empty find/query'
		// 	context.cache.containsKey( StringUtil.md5Hex( rowEtl.fields[property].find.query.toString() ) )

		// when: 'Calling a second time the same error should be returned but the cache should have been used'
		// 	(entity, error) = dataImportService.lookupDomainRecordByFieldMetaData(project, domainName, property, rowEtl, context)
        // then:
		// 	noFindElseFind == error

		when: 'called with the value containing the asset id'
			rowEtl.fields.asset.value = device.id.toString()
			(entity, error) = dataImportService.lookupDomainRecordByFieldMetaData(project, domainName, property, rowEtl, context)
		then: 'the asset should be found'
			entity
		and: 'the asset should match the one attempting to be found'
			device.id == entity.id

		when: 'called where the query has then name of the asset'
			rowEtl.fields.asset.value = ''
			rowEtl.fields.asset.find.query = [
				[	domain: 'Device',
					kv: [
						assetName: device.assetName
					]
				]
			]
		then: 'the asset should be returned'
			entity

		when: 'there are two entities that have a common attribute'
			def device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetType = 'Server'
			device2.assetType = 'Server'
			device.save()
			device2.save()
		and: 'the query is based on the common attribute'
			rowEtl.fields.asset.value = ''
			rowEtl.fields.asset.find.query = [
				[	domain: 'Device',
					kv: [
						assetType: device.assetType
					]
				]
			]
		and: 'the lookupDomainRecordByFieldMetaData method is called'
			(entity, error) = dataImportService.lookupDomainRecordByFieldMetaData(project, domainName, property, rowEtl, context)
		then: 'an error should be reported that there were multiple references'
			"Found multiple references for property $property" == error


		when: 'there is another project with an asset'
			def otherProject = projectTestHelper.createProject()
			def otherMoveBundle = moveBundleTestHelper.createBundle(otherProject, null)
			def otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, otherMoveBundle)
		and: 'the ETL has the id of the domain in the other project'
			rowEtl.fields.asset.value = otherProjectDevice.id.toString()
		and: 'the lookupDomainRecordByFieldMetaData method is called'
			(entity, error) = dataImportService.lookupDomainRecordByFieldMetaData(project, domainName, property, rowEtl, context)
		then: 'an error should be reported that there were multiple references'
			referenceNotFound == error
    }
}