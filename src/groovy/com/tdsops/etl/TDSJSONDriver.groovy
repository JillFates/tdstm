package com.tdsops.etl

import com.tdssrc.grails.JsonUtil
import getl.data.Dataset
import getl.data.Field
import getl.exception.ExceptionGETL
import getl.json.JSONDataset
import getl.json.JSONDriver
import getl.utils.GenerationUtils
import getl.utils.Logs
import org.codehaus.groovy.grails.web.json.JSONElement

/**
 * Custom implementation of JSONDriver. It adds support for several ETL script commands.
 *
 * @see JSONDriver
 */
class TDSJSONDriver extends JSONDriver {
	private JSONElement json

	JSONElement getRootNode(Dataset dataset, String rootNode) {
		if ( !json ) {
			json = JsonUtil.parseFilePath(((JSONDataset) dataset).fullFileName())
		}

		return JsonUtil.xpathAt(json, rootNode)
	}

	@Override
	List<Field> fields(Dataset dataset) {
		if(!dataset.field) {
			JSONElement json = getRootNode(dataset, dataset.rootNode)

			// Sorted Map when creating from the nodes
			TreeMap<String, Field> fields = [:]

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

	@Override
	long eachRow(Dataset dataset, Map params, Closure prepareCode, Closure code) {
		Closure filter = params."filter"

		long countRec = 0
		doRead(dataset, params, prepareCode) { row ->
			if (filter != null && !filter(row)) return

			countRec++
			code(row)
		}

		countRec
	}

	// HELPER METHODS ///////////////////////////////////////////////////////////////////////////////////
	private void doRead(Dataset dataset, Map params, Closure prepareCode, Closure code) {
		if (dataset.field.isEmpty()) throw new ExceptionGETL("Required fields description with dataset")
		if (dataset.params.rootNode == null) throw new ExceptionGETL("Required \"rootNode\" parameter with dataset")
		String rootNode = dataset.params.rootNode

		String fn = fullFileNameDataset(dataset)
		if (fn == null) throw new ExceptionGETL("Required \"fileName\" parameter with dataset")
		File f = new File(fn)
		if (!f.exists()) throw new ExceptionGETL("File \"${fn}\" not found")

		long limit = (params.limit != null)?params.limit:0

		def data = readData(dataset, params)

		List<String> fields = []
		if (prepareCode != null) {
			prepareCode(fields)
		}
		else if (params.fields != null) fields = params.fields

		readRows(dataset, fields, rootNode, limit, data, params.initAttr, code)
	}

	/**
	 * Read only attributes from dataset
	 * @param dataset
	 * @param initAttr
	 * @param sb
	 */
	private void generateAttrRead (Dataset dataset, Map data, Closure initAttr) {
		List<Field> attrs = (dataset.params.attributeField != null)?dataset.params.attributeField:[]
		if (attrs.isEmpty()) return

		Map<String, Object> attrValue = [:]

		attrs.each { Field d ->
			String path = GenerationUtils.Field2Alias(d, false)
			attrValue[d.name.toLowerCase()] = JsonUtil.gpathAt(data, path)
		}
		dataset.params.attributeValue = attrValue
		if ( initAttr && !initAttr(dataset) ) {
			throw new ExceptionGETL("Could not init Attributes")
		}
	}

	private void readRows (Dataset dataset, List<String> listFields, String rootNode, long limit, Map data, Closure initAttr, Closure code) {
		generateAttrRead(dataset, data, initAttr)

		long cur = 0
		int offsetRows = dataset.params.currentRowIndex?:0


		if ( limit > 0 ) {
			limit = limit + offsetRows
		}

		def node = JsonUtil.gpathAt(data, rootNode)
		node.each { struct ->
			cur++
			if ((cur < offsetRows) || (limit > 0 && cur > limit) ) {
				return
			}

			Map row = [:]

			dataset.field.each { Field d ->
				if (listFields.isEmpty() || listFields.find { it.toLowerCase() == d.name.toLowerCase() }) {
					String path = GenerationUtils.Field2Alias(d, false)
					row[d.name.toLowerCase()] = JsonUtil.gpathAt(struct, path)
				}
			}
			code(row)
		}
	}

}
