package com.tdsops.etl

import com.tdssrc.grails.JsonUtil
import getl.data.Dataset
import getl.data.Field
import getl.json.JSONDataset
import getl.json.JSONDriver
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

}
