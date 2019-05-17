package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass

/**
 * The {@code DataviewUserParamsCommand} represents the various parameters that make up the properties
 * that the user can override in the view.
 * It is defined by a nested {@code CommandObject} to refer validations of input data.
 */

class DataviewUserParamsCommand implements CommandObject {

	String sortDomain
	String sortProperty
	String sortOrder = 'a'
	Boolean justPlanning
	Boolean forExport

	int offset = 0
	int limit = 25

	/*
		{
		  "filters": {
			"domains": [
			  "common",
			  "application",
			  "database",
			  "device",
			  "storage"
			],
			"columns": [
			  {
				"domain": "common",
				"edit": false,
				"filter": "",
				"label": "Name",
				"locked": true,
				"property": "assetName",
				"width": 220
			  }
			],
			"named": "physicalServer,toValidate",
			"extra": [
			  {
				"domain": "common",
				"filter": "FOO",
				"property": "assetName"
			  }
			]
		  }
		}
	 */
	DataviewUserFilterParamsCommand filters

	static constraints = {
		sortDomain blank: false, inList: AssetClass.domainAssetTypeList
		sortProperty blank: false
		sortOrder blank: false, inList: ['a', 'd']
		offset min: 0
		limit min: 0
		justPlanning nullable: true
		forExport nullable: true
		filters nullable: false, validator: { val, obj ->
			return val.validate() ?: 'not.valid'
		}
	}
}