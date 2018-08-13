package com.tdsops.etl

import com.tdsops.common.lang.CollectionUtils
import com.tdssrc.grails.JsonUtil
import getl.data.Dataset
import getl.data.Field
import getl.exception.ExceptionGETL
import getl.json.JSONDataset
import getl.json.JSONDriver
import getl.utils.GenerationUtils
import getl.utils.Logs
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Custom implementation of JSONDriver. It adds support for several ETL script commands.
 *
 * @see JSONDriver
 */
class TDSJSONDriver extends JSONDriver {
	private Object json

	/**
	 * Return the Value where the 'rootNode' variable is pointing
	 * it can be a Map (Node), a list, or a value
	 * @param dataset variable containing the dataset of the JSON
	 * @param rootNode gpath where we are navigating
	 * @return  a Map, List or Object Value
	 */
	Object getRootNode(Dataset dataset, String rootNode) {
		if ( !json ) {
			json = JsonUtil.parseFilePath(((JSONDataset) dataset).fullFileName())
		}
		// println "getRootNod() json isa ${json.getClass().getName()} \n\n$json"
		Object data = JsonUtil.gpathAt(json, rootNode)
		// println "getRootNode() data isa ${data.getClass().getName()}\n\n$data"
		return data
	}

	/**
	 * Gets the fields schema specification or try to guess it
	 * @param dataset
	 * @return
	 */
	@Override
	List<Field> fields(Dataset dataset) {

		if (!dataset.field) {
			Object json = getRootNode(dataset, dataset.rootNode)
			List nodeList

			switch (json) {
				case List:
					nodeList = json as List
					break
				case Map:
					nodeList = [json]
					break
				case null:
					throw new ExceptionGETL("Unable to parse attribute names due to empty results at rootNode '${dataset.rootNode}'")
				default:
					log.warn "fields() encountered unexpected type ${json.getClass().getName()}, rootNode=${dataset.rootNode}"
					throw new ExceptionGETL("Unable to parse attribute names due to JSON structure at rootNode '${dataset.rootNode}")
			}

			// Sorted Map when creating from the nodes
			TreeMap<String, Field> fields = [:]

			// println "\n\n**** nodeList isa ${nodeList}\n\n"
			nodeList.each { node ->
				// println "\n\n**** node isa ${node}\n\n"
				node.each { k, v ->
					if(! fields.containsKey(k) ) {
						//TODO: Set Field Type properly based v class type
						fields[k] = new Field(name: k, type: Field.Type.STRING)
					}
				}
			}
			dataset.field.addAll( fields.values() )
		}
		return dataset.field
	}

	/**
	 * Overided function that reads each node in the 'rootNode' level of the JSON object
	 * @param dataset
	 * @param params
	 * @param prepareCode
	 * @param code
	 * @return
	 */
	@Override
	long eachRow(Dataset dataset, Map params, Closure prepareCode, Closure code) {
		Closure filter = params."filter"

		long countRec = 0

		// closure that evaluates each row and apply filters
		def rowEvaluator = { row ->
			if (filter != null && !filter(row)) return

			countRec++
			code(row)
		}

		doRead(dataset, params, prepareCode, rowEvaluator)

		return countRec
	}

	// HELPER METHODS ///////////////////////////////////////////////////////////////////////////////////
	/**
	 * performs the actual read of the nodes and evaluates the closures defined to load the information
	 * @param dataset
	 * @param params
	 * @param prepareCode
	 * @param code
	 */
	private void doRead(Dataset dataset, Map params, Closure prepareCode, Closure code) {

		if (dataset.field.isEmpty()) {
			throw new ExceptionGETL('Fields schema is required along with the dataset')
		}

		String rootNode = (dataset.params.rootNode) ?: ''

		String fn = fullFileNameDataset(dataset)
		if (fn == null) throw new ExceptionGETL('"fileName" parameter must be defined in the dataset')
		File f = new File(fn)
		if (!f.exists()) {
			throw new ExceptionGETL("ETL Import JSON file was not found")
		}

		long limit = (params.limit != null) ? params.limit : 0

		def data = readData(dataset, params)

		List<String> fields = []
		if (prepareCode != null) {
			prepareCode(fields)
		}
		else if (params.fields != null) fields = params.fields

		readNodes(dataset, fields, rootNode, limit, data, params.initAttr, code)
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
			attrValue[d.name] = JsonUtil.gpathAt(data, path)
		}
		dataset.params.attributeValue = attrValue
		if ( initAttr && !initAttr(dataset) ) {
			throw new ExceptionGETL("Could not init Attributes")
		}
	}

	/**
	 * This does the heavby lifting of applying filters for offset and limit, uses the fields schema to look for the data
	 * @param dataset
	 * @param listFields
	 * @param rootNode
	 * @param limit
	 * @param data
	 * @param initAttr
	 * @param code
	 */
	private void readNodes (Dataset dataset, List<String> listFields, String rootNode, long limit, Object data, Closure initAttr, Closure code) {
		// TODO: @oluna I'm commenting this as stated in the PR#1257, as far as I can see this is used to generate user specific attributes using a closure
		// we need to review further impact in the ETL API
		// generateAttrRead(dataset, data, initAttr)

		long cur = 0
		int offsetRows = dataset.params.currentRowIndex?:0


		if ( limit > 0 ) {
			limit = limit + offsetRows
		}

		def nodeList = JsonUtil.gpathAt(data, rootNode)

		// if is not a Collection wrap it in a single element Array
		if ( !(nodeList instanceof Collection) ) {
			nodeList = [nodeList]
		}

		nodeList.each { struct ->
			cur++
			if ((cur < offsetRows) || (limit > 0 && cur > limit) ) {
				return
			}

			Map row = [:]

			dataset.field.each { Field d ->
				if (listFields.isEmpty() || listFields.find { it == d.name }) {
					String path = GenerationUtils.Field2Alias(d, false)
					row[d.name.toLowerCase()] = JsonUtil.gpathAt(struct, path)
				}
			}
			code(row)
		}
	}

}
