package com.tdsops.etl

import com.tdssrc.grails.WorkbookUtil
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDriver
import getl.exception.ExceptionGETL
import getl.json.JSONDataset
import getl.json.JSONDriver
import getl.utils.BoolUtils
import getl.utils.FileUtils
import getl.utils.ListUtils
import groovy.json.JsonSlurper
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

/**
 * Custom implementation of ExcelDriver. It adds support for several ETL script commands.
 * For example:
 * <pre>
 *  sheet 'Active Applications'
 *  .....
 *  sheet 4
 *  .....
 *  sheet '4'
 *  ....
 * </pre>
 *  Is validated by @see TDSExcelDriver#hasSheet
 * <br>
 *  After that, reading workbook column headers
 * <pre>
 *  sheet 'Active Applications'
 *  read labels
 * </pre>
 * It is implemented TDSExcelDriver#fields
 *
 * @see JSONDriver
 */
class TDSJSONDriver extends JSONDriver {

	/**
	 * Maps of fields base on listName param.
	 * <pre>
	 * [
	 *      0: [{ ..field1.. }, { ..field1.. }, ..., { ..fieldN.. }]
	 * ]
	 * </pre>
	 * <pre>
	 * [
	 *      'Applications': [{ ..field1.. }, { ..field1.. }, ..., { ..fieldN.. }]
	 * ]
	 * </pre>
	 */
	Map<Object, List<Field>> fieldsMap = [:]

	@Override
	List<Field> fields(Dataset dataset) {
		dataset.params.listName = dataset.params.listName?:0
		if(!dataset.field) {
			File file = new File( ((JSONDataset)dataset).fileName )
			String jsonStr = file.text

			def jsonSlurper = new JsonSlurper()
			def json = jsonSlurper.parseText( jsonStr )

			HashMap<String, Field> fields = [:]

			json.each { node ->
				node.each { k, v ->
					if(! fields.containsKey(k) ) {
						fields[k] = new Field(name: k, type: Field.Type.STRING)
					}
				}
			}

			dataset.field.addAll( fields.values() )
		}
		return dataset.field
	}

}
