package com.tdsops.etl

import getl.data.Field
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.AssetEntity

class TDSExcelDriverSpec extends ETLBaseSpec implements DataTest {

	void setupSpec() {
		mockDomains AssetEntity
	}

	void 'test can read fields using the default sheet with 0 ordinal position'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetXLSXDataSet(
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
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)

	}


	void 'test can read fields using a particular sheet defined by a sheet name'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetXLSXDataSet(
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
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)

	}


	static final String ApplicationDataSet = """
		application id,vendor name,technology,location
		152254,Microsoft,(xlsx updated),ACME Data Center
		152255,Mozilla,NGM,ACME Data Center""".stripIndent().trim()
}
