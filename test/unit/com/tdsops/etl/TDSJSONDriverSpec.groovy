package com.tdsops.etl

import getl.data.Field
import grails.test.mixin.TestFor
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService

@TestFor(FileSystemService)
class TDSJSONDriverSpec extends ETLBaseSpec {

	FileSystemService fileSystemService

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	def setup() {

	}

	void 'test can read fields from a plain JSON array'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildJSONDataSet(
				ApplicationDataSet
			)
			TDSJSONDriver tdsJSONDriver = (TDSJSONDriver)dataSetFacade.dataSet.connection.driver
		when:
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications sheet'
			fields.size() == 4

		and:
			tdsJSONDriver.fields() != null

		/*
			tdsJSONDriver.fieldsMap.keySet().size() == 1
			tdsJSONDriver.fieldsMap.containsKey(0)
			tdsJSONDriver.fieldsMap[0][0].name == 'application id'
			tdsJSONDriver.fieldsMap[0][1].name == 'vendor name'
			tdsJSONDriver.fieldsMap[0][2].name == 'technology'
			tdsJSONDriver.fieldsMap[0][3].name == 'location'
		*/

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

/*
	void 'test can read fields using a particular sheet defined by a sheet name'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetDataSet(
				ApplicationDataSet
			)
			TDSJSONDriver tdsJSONDriver = (TDSJSONDriver)dataSetFacade.dataSet.connection.driver
		when:
			dataSetFacade.setSheetName('Applications')
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications sheet'
			fields.size() == 4

		and:
			tdsJSONDriver.workbook != null

			tdsJSONDriver.sheetsMap.keySet().size() == 2
			tdsJSONDriver.sheetsMap.containsKey('Applications')
			tdsJSONDriver.sheetsMap.containsKey(0)

			tdsJSONDriver.fieldsMap.keySet().size() == 1
			tdsJSONDriver.fieldsMap.containsKey('Applications')
			tdsJSONDriver.fieldsMap['Applications'][0].name == 'application id'
			tdsJSONDriver.fieldsMap['Applications'][1].name == 'vendor name'
			tdsJSONDriver.fieldsMap['Applications'][2].name == 'technology'
			tdsJSONDriver.fieldsMap['Applications'][3].name == 'location'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}
*/


	static final String ApplicationDataSet = """
		[
			{
				"application id":152254,
				"vendor name":"Microsoft",
				"technology":"(xlsx updated)",
				"location":"ACME Data Center"
			},
			{
				"application id":152255,
				"vendor name":"Mozilla",
				"technology":"NGM",
				"location":"ACME Data Center"
			},
		]	
	""".stripIndent().trim()
}
