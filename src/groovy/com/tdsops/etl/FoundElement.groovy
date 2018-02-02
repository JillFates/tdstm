package com.tdsops.etl


class FoundElement {

	private ETLFindElement
	/**
	 * Defines if the found element instance
	 * is 'update' or 'create'
	 */
	private String action
	private Map<String, ?> properties = [:]

	FoundElement(ETLFindElement) {
		this.ETLFindElement = ETLFindElement
	}

	/**
	 * WhenFound ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenFound asset create {
	 *			assetClass: Application
	 *			assetName: primaryName
	 *			assetType: primaryType
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param dependentId
	 * @return the current find Element
	 */
	FoundElement create(Closure closure) {
		closure(this)
		return this
	}

	FoundElement update(Closure closure) {
		closure(this)
		return this
	}


	def methodMissing(String name, def args) {

		if(args){
			properties[name] = args.collect{it}.jsoin
		}




	}
}
