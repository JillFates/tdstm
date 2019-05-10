package net.transitionmanager.service.dataview

import spock.lang.Shared

/**
 * This trait defines the "All Assets" request used by the client side tries to filter its content.
 */
trait AllAssetsFilterUnitTest {

	@Shared
	Map<String, ?> allAssetsFilterMap = [
		"offset"      : 0,
		"limit"       : 25,
		"sortDomain"  : "common",
		"sortProperty": "assetName",
		"sortOrder"   : "a",
		"filters"     : [
			"domains": ["common", "application", "database", "device", "storage"],
			"columns": [
				["domain": "common", "edit": false, "filter": "", "label": "Name", "locked": true, "property": "assetName", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Asset Class", "locked": true, "property": "assetClass", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Description", "locked": false, "property": "description", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Environment", "locked": false, "property": "environment", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Bundle", "locked": false, "property": "moveBundle", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Tags", "locked": false, "property": "tagAssets", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Validation", "locked": false, "property": "validation", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Plan Status", "locked": false, "property": "planStatus", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Support", "locked": false, "property": "supportType", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "External Ref Id", "locked": false, "property": "externalRefId", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Modified Date", "locked": false, "property": "lastUpdated", "width": 140]
			]
		]
	]

	@Shared
	String allAssetsFilterJsonContent = """
		{
		   "offset":0,
		   "limit":25,
		   "sortDomain":"common",
		   "sortProperty":"assetName",
		   "sortOrder":"a",
		   "filters":{
			  "domains":[
				 "common",
				 "application",
				 "database",
				 "device",
				 "storage"
			  ],
			  "columns":[
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Name",
					"locked":true,
					"property":"assetName",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Asset Class",
					"locked":true,
					"property":"assetClass",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Description",
					"locked":false,
					"property":"description",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Environment",
					"locked":false,
					"property":"environment",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Bundle",
					"locked":false,
					"property":"moveBundle",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Tags",
					"locked":false,
					"property":"tagAssets",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Validation",
					"locked":false,
					"property":"validation",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Plan Status",
					"locked":false,
					"property":"planStatus",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Support",
					"locked":false,
					"property":"supportType",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"External Ref Id",
					"locked":false,
					"property":"externalRefId",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Modified Date",
					"locked":false,
					"property":"lastUpdated",
					"width":140
				 }
			  ]
		   }
		}"""

}
