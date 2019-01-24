package com.tdsops.etl

import com.tds.asset.AssetEntity
import getl.data.Field
import grails.test.mixin.Mock
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService

@Mock([AssetEntity])
class TDSExcelDriverSpec extends ETLBaseSpec {

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

	void 'test can read fields using the default sheet with 0 ordinal position'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetDataSet(
				'Applications',
				ApplicationDataSet
			)
			TDSExcelDriver tdsExcelDriver = (TDSExcelDriver) dataSetFacade.dataSet.connection.driver
		when:
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications sheet'
			fields.size() == 4

		and:
			tdsExcelDriver.workbook != null

			tdsExcelDriver.sheetsMap.keySet().size() == 2
			tdsExcelDriver.sheetsMap.containsKey('Applications')
			tdsExcelDriver.sheetsMap.containsKey(0)

			tdsExcelDriver.fieldsMap.keySet().size() == 1
			tdsExcelDriver.fieldsMap.containsKey(0)
			tdsExcelDriver.fieldsMap[0][0].name == 'application id'
			tdsExcelDriver.fieldsMap[0][1].name == 'vendor name'
			tdsExcelDriver.fieldsMap[0][2].name == 'technology'
			tdsExcelDriver.fieldsMap[0][3].name == 'location'

		cleanup:
			if (fileName) service.deleteTemporaryFile(fileName)

	}


	void 'test can read fields using a particular sheet defined by a sheet name'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetDataSet(
				'Applications',
				ApplicationDataSet
			)
			TDSExcelDriver tdsExcelDriver = (TDSExcelDriver) dataSetFacade.dataSet.connection.driver
		when:
			dataSetFacade.setSheetName('Applications')
			List<Field> fields = dataSetFacade.fields()

		then: 'Fields are read from the Applications sheet'
			fields.size() == 4

		and:
			tdsExcelDriver.workbook != null

			tdsExcelDriver.sheetsMap.keySet().size() == 2
			tdsExcelDriver.sheetsMap.containsKey('Applications')
			tdsExcelDriver.sheetsMap.containsKey(0)

			tdsExcelDriver.fieldsMap.keySet().size() == 1
			tdsExcelDriver.fieldsMap.containsKey('Applications')
			tdsExcelDriver.fieldsMap['Applications'][0].name == 'application id'
			tdsExcelDriver.fieldsMap['Applications'][1].name == 'vendor name'
			tdsExcelDriver.fieldsMap['Applications'][2].name == 'technology'
			tdsExcelDriver.fieldsMap['Applications'][3].name == 'location'

		cleanup:
			if (fileName) service.deleteTemporaryFile(fileName)

	}


	static final String ApplicationDataSet = """
		application id,vendor name,technology,location
		152254,Microsoft,(xlsx updated),ACME Data Center
		152255,Mozilla,NGM,ACME Data Center""".stripIndent().trim()
}
