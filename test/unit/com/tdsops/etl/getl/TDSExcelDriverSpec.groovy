package com.tdsops.etl.getl

import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.TDSExcelDriver
import grails.test.mixin.TestFor
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification

@TestFor(FileSystemService)
class TDSExcelDriverSpec extends ETLBaseSpec {

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

	void 'test can read the sheet name list'() {
		when:
			def (String fileName, DataSetFacade dataSetFacade) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)
		    TDSExcelDriver tdsExcelDriver = (TDSExcelDriver)dataSetFacade.dataSet.connection.driver

		then:
			tdsExcelDriver.getSheetNames(dataSetFacade.dataSet) == ['Applications']

		and:
			tdsExcelDriver.sheetNamesMap == [0: 'Applications']

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}


	void 'test can read sheet names map'() {
		when:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		then:
			dataSet.driver.getSheetNames() == ['Applications']

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}
	static final String ApplicationDataSet = """
		application id,vendor name,technology,location
		152254,Microsoft,(xlsx updated),ACME Data Center
		152255,Mozilla,NGM,ACME Data Center""".stripIndent().trim()
}
