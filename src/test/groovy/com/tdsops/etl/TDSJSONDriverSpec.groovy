package com.tdsops.etl

import getl.data.Field
import getl.exception.ExceptionGETL
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.exception.EmptyResultException
import spock.lang.See

class TDSJSONDriverSpec extends ETLBaseSpec implements DataTest{

	void setupSpec(){
		mockDomains AssetEntity
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
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)

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
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)

	}

	@See('TM-16277')
	void 'test can throw an exception if json content is incorrect'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildJSONDataSet(
				"""
					"devices": [
					   { 
						  "vm id": 123,
						  "attribs": {
							 "memory": 4096,
							 "cpu": 2,
							 "hostname": 'zulu01',
						  }
					   }
					]"""
			)
		when:
			dataSetFacade.fields()

		then: 'An ETLProcessorException is thrown'
			ExceptionGETL e = thrown ExceptionGETL
			e.message == "Unable to parse attribute names due to JSON structure at rootNode '"

		cleanup:
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)
	}

	@See('TM-16277')
	void 'test can throw an exception id json content is empty'() {
		given:
			def (String fileName, DataSetFacade dataSetFacade) = buildJSONDataSet(
				''
			)
		when:
			dataSetFacade.fields()

		then: 'An ETLProcessorException is thrown'
			EmptyResultException e = thrown EmptyResultException
			e.message == "JSON data file contains no data"

		cleanup:
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
