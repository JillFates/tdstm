package com.tdsops.etl

import net.transitionmanager.asset.AssetEntity
import getl.data.Field
import grails.test.mixin.Mock
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService

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
			fields[0].name == 'Application Id'
			fields[1].name == 'Location'
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
			fields[0].name == 'Application Id'
			fields[1].name == 'Location'
			fields[2].name == 'technology'
			fields[3].name == 'vendor name'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}


	static final String RootApplicationDataSet = """
		[
			{
				"Application Id":152254,
				"vendor name":"Microsoft",
				"Location":"ACME Data Center"
			},
			{
				"Application Id":152255,
				"technology":"NGM",
				"Location":"ACME Data Center"
			}
		]	
	""".stripIndent().trim()

	static final String NodeApplicationDataSet = """
		{
			"Applications" : [
				{
					"Application Id":152254,
					"vendor name":"Microsoft",
					"Location":"ACME Data Center"
				},
				{
					"Application Id":152255,
					"technology":"NGM",
					"Location":"ACME Data Center"
				}
			],
			"Device" : []
		}
	""".stripIndent().trim()
}