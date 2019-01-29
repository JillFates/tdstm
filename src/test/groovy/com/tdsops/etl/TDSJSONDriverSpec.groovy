package com.tdsops.etl

import com.tds.asset.AssetEntity
import getl.data.Field
import grails.test.mixin.Mock
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService

@Mock([AssetEntity])
class TDSJSONDriverSpec extends ETLBaseSpec {

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	void 'test can read fields from a plain JSON array'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildJSONDataSet(
				RootApplicationDataSet
			)
		when:
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications node'
			fields.size() == 4

		and:
			fields[0].name == 'application id'
			fields[1].name == 'location'
			fields[2].name == 'technology'
			fields[3].name == 'vendor name'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}


	void 'test can read fields using a particular path defined by rootNode'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildJSONDataSet(
				NodeApplicationDataSet
			)
		when:
			dataSetFacade.setRootNode('Applications')
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications node'
			fields.size() == 4

		and:
			fields[0].name == 'application id'
			fields[1].name == 'location'
			fields[2].name == 'technology'
			fields[3].name == 'vendor name'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}


	static final String RootApplicationDataSet = """
		[
			{
				"application id":152254,
				"vendor name":"Microsoft",
				"location":"ACME Data Center"
			},
			{
				"application id":152255,
				"technology":"NGM",
				"location":"ACME Data Center"
			}
		]	
	""".stripIndent().trim()

	static final String NodeApplicationDataSet = """
		{
			"Applications" : [
				{
					"application id":152254,
					"vendor name":"Microsoft",
					"location":"ACME Data Center"
				},
				{
					"application id":152255,
					"technology":"NGM",
					"location":"ACME Data Center"
				}
			],
			"Device" : []
		}
	""".stripIndent().trim()
}
