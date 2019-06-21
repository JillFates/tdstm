package net.transitionmanager.service.dataview

import spock.lang.Shared

/**
 * This trait defines the "All Assets" request used by the client side tries to filter its content.
 * It can manages the {@code allAssetsDataviewMap] with a Map object that contains "All Assets" UI body request.
 * It can manages the {@code allAssetsDataviewReportSchema] with a String object that contains "All Assets" UI body JSON request.
 */
trait AllAssetsFilterUnitTest {

	@Shared
	Map<String, ?> allAssetsDataviewMap = [
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
				["domain": "common", "edit": false, "filter": "", "label": "Validation", "locked": false, "property": "validation", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Plan Status", "locked": false, "property": "planStatus", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Support", "locked": false, "property": "supportType", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "External Ref Id", "locked": false, "property": "externalRefId", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Modified Date", "locked": false, "property": "lastUpdated", "width": 140]
			]
		]
	]

	@Shared
	Map<String, ?> applicationsDataviewMap = [
		"offset"      : 0,
		"limit"       : 25,
		"sortDomain"  : "common",
		"sortProperty": "assetName",
		"sortOrder"   : "a",
		"filters"     : [
			"domains": ["common", "application"],
			"columns": [
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Name",
					"locked"  : true,
					"property": "assetName",
					"width"   : 220
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Description",
					"locked"  : false,
					"property": "description",
					"width"   : 220
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Environment",
					"locked"  : false,
					"property": "environment",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Bundle",
					"locked"  : false,
					"property": "moveBundle",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Validation",
					"locked"  : false,
					"property": "validation",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Plan Status",
					"locked"  : false,
					"property": "planStatus",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "External Ref Id",
					"locked"  : false,
					"property": "externalRefId",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Modified Date",
					"locked"  : false,
					"property": "lastUpdated",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Vendor",
					"locked"  : false,
					"property": "appVendor",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Version",
					"locked"  : false,
					"property": "appVersion",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Technology",
					"locked"  : false,
					"property": "appTech",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Source",
					"locked"  : false,
					"property": "appSource",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "License",
					"locked"  : false,
					"property": "license",
					"width"   : 140
				],
				[
					"domain"  : "common",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Support",
					"locked"  : false,
					"property": "supportType",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Business Unit",
					"locked"  : false,
					"property": "businessUnit",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "App Owner",
					"locked"  : false,
					"property": "appOwner",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "SME1",
					"locked"  : false,
					"property": "sme",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "SME2",
					"locked"  : false,
					"property": "sme2",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Retire Date",
					"locked"  : false,
					"property": "retireDate",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Maint Expiration",
					"locked"  : false,
					"property": "maintExpDate",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Function",
					"locked"  : false,
					"property": "appFunction",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Criticality",
					"locked"  : false,
					"property": "criticality",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "User Count",
					"locked"  : false,
					"property": "userCount",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "User Locations",
					"locked"  : false,
					"property": "userLocations",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Use Frequency",
					"locked"  : false,
					"property": "useFrequency",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "DR RPO",
					"locked"  : false,
					"property": "drRpoDesc",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "DR RTO",
					"locked"  : false,
					"property": "drRtoDesc",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Latency OK",
					"locked"  : false,
					"property": "latency",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Test Proc OK",
					"locked"  : false,
					"property": "testProc",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Startup Proc OK",
					"locked"  : false,
					"property": "startupProc",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "URL",
					"locked"  : false,
					"property": "url",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Shutdown By",
					"locked"  : false,
					"property": "shutdownBy",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Shutdown Fixed",
					"locked"  : false,
					"property": "shutdownFixed",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Shutdown Duration",
					"locked"  : false,
					"property": "shutdownDuration",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Startup By",
					"locked"  : false,
					"property": "startupBy",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Startup Fixed",
					"locked"  : false,
					"property": "startupFixed",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Startup Duration",
					"locked"  : false,
					"property": "startupDuration",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Testing By",
					"locked"  : false,
					"property": "testingBy",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Testing Fixed",
					"locked"  : false,
					"property": "testingFixed",
					"width"   : 140
				],
				[
					"domain"  : "application",
					"edit"    : false,
					"filter"  : "",
					"label"   : "Testing Duration",
					"locked"  : false,
					"property": "testingDuration",
					"width"   : 140
				]
			]
		]
	]

	@Shared
	Map<String, ?> devicesDataviewMap = [
		"offset"      : 0,
		"limit"       : 25,
		"sortDomain"  : "common",
		"sortProperty": "assetName",
		"sortOrder"   : "a",
		"filters"     : [
			"domains": [
				"common",
				"device"
			],
			"columns": [
				["domain": "common", "edit": false, "filter": "", "label": "Name", "locked": true, "property": "assetName", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Description", "locked": false, "property": "description", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Environment", "locked": false, "property": "environment", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Bundle", "locked": false, "property": "moveBundle", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Validation", "locked": false, "property": "validation", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Plan Status", "locked": false, "property": "planStatus", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "External Ref Id", "locked": false, "property": "externalRefId", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Modified Date", "locked": false, "property": "lastUpdated", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Alternate Name", "locked": false, "property": "shortName", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Serial #", "locked": false, "property": "serialNumber", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Asset Tag", "locked": false, "property": "assetTag", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Device Type", "locked": false, "property": "assetType", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Manufacturer", "locked": false, "property": "manufacturer", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Model", "locked": false, "property": "model", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "IP Address", "locked": false, "property": "ipAddress", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "OS", "locked": false, "property": "os", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Location", "locked": false, "property": "locationSource", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Room", "locked": false, "property": "roomSource", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Rack", "locked": false, "property": "rackSource", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Position", "locked": false, "property": "sourceRackPosition", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Chassis", "locked": false, "property": "sourceChassis", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Source Blade Position", "locked": false, "property": "sourceBladePosition", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Location", "locked": false, "property": "locationTarget", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Room", "locked": false, "property": "roomTarget", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Rack", "locked": false, "property": "rackTarget", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Position", "locked": false, "property": "targetRackPosition", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Chassis", "locked": false, "property": "targetChassis", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Target Blade Position", "locked": false, "property": "targetBladePosition", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Support", "locked": false, "property": "supportType", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Retire Date", "locked": false, "property": "retireDate", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Maint Expiration", "locked": false, "property": "maintExpDate", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Priority", "locked": false, "property": "priority", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Truck", "locked": false, "property": "truck", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Cart", "locked": false, "property": "cart", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Shelf", "locked": false, "property": "shelf", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Rail Type", "locked": false, "property": "railType", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Size", "locked": false, "property": "size", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Scale", "locked": false, "property": "scale", "width": 140],
				["domain": "device", "edit": false, "filter": "", "label": "Rate Of Change", "locked": false, "property": "rateOfChange", "width": 140]
			]
		]
	]

	@Shared
	String allAssetsDataviewReportSchema = """
		{
		   "domains":[
			  "common",
			  "application",
			  "database",
			  "device",
			  "storage"
		   ],
		   "sort":{
			  "domain":"common",
			  "order":"a",
			  "property":"assetName"
		   },
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
		}"""

}
