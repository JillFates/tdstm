package version.v4_7_2

databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20190930 TM-15833") {
		comment('Update dataviews to be similar.')

		//database
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "database" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" },  { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" }, { "domain" : "common", "property" : "environment", "width" : 140, "locked" : false, "edit" : false, "label" : "Environment", "filter" : "" }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" }, { "domain" : "database", "property" : "dbFormat", "width" : 140, "locked" : false, "edit" : false, "label" : "Format", "filter" : "" } ]}'
					WHERE id = 2""")

		//devices
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "device" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" }, { "domain" : "device", "property" : "assetType", "width" : 140, "locked" : true, "edit" : false, "label" : "Device Type", "filter" : "" }, { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" },{ "domain" : "common", "edit" : false, "filter" : "", "label" : "Environment", "locked" : false, "property" : "environment", "width" : 140 }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" },{ "domain" : "device", "property" : "os", "width" : 140, "locked" : false, "edit" : false, "label" : "OS", "filter" : "" }, { "domain" : "device", "property" : "manufacturer", "width" : 140, "locked" : false, "edit" : false, "label" : "Manufacturer", "filter" : "" }, { "domain" : "device", "property" : "model", "width" : 140, "locked" : false, "edit" : false, "label" : "Model", "filter" : "" }, { "domain" : "device", "property" : "ipAddress", "width" : 140, "locked" : false, "edit" : false, "label" : "IP Address", "filter" : "" }]}'
					WHERE id = 3""")

		//servers
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "device" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" }, { "domain" : "device", "property" : "assetType", "width" : 140, "locked" : true, "edit" : false, "label" : "Device Type", "filter" : "Server|Appliance|Blade|VM|Virtual" }, { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" }, { "domain" : "common", "edit" : false, "filter" : "", "label" : "Environment", "locked" : false, "property" : "environment", "width" : 140 }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" }, { "domain" : "device", "property" : "os", "width" : 140, "locked" : false, "edit" : false, "label" : "OS", "filter" : "" }, { "domain" : "device", "property" : "manufacturer", "width" : 140, "locked" : false, "edit" : false, "label" : "Manufacturer", "filter" : "" }, { "domain" : "device", "property" : "model", "width" : 140, "locked" : false, "edit" : false, "label" : "Model", "filter" : "" }, { "domain" : "device", "property" : "ipAddress", "width" : 140, "locked" : false, "edit" : false, "label" : "IP Address", "filter" : "" } ]}'
					WHERE id = 4""")

		//physical storage
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "device" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" }, { "domain" : "device", "property" : "assetType", "width" : 140, "locked" : true, "edit" : false, "label" : "Device Type", "filter" : "Array|Disk|NAS|SAN|SAN Switch|Storage|Tape|Tape Library|Virtual Tape Library" }, { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" }, { "domain" : "common", "edit" : false, "filter" : "", "label" : "Environment", "locked" : false, "property" : "environment", "width" : 140 }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" }, { "domain" : "device", "property" : "os", "width" : 140, "locked" : false, "edit" : false, "label" : "OS", "filter" : "" }, { "domain" : "device", "property" : "manufacturer", "width" : 140, "locked" : false, "edit" : false, "label" : "Manufacturer", "filter" : "" }, { "domain" : "device", "property" : "model", "width" : 140, "locked" : false, "edit" : false, "label" : "Model", "filter" : "" }, { "domain" : "device", "property" : "ipAddress", "width" : 140, "locked" : false, "edit" : false, "label" : "IP Address", "filter" : "" }]}'
					WHERE id = 5""")

		//logical storage
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "storage" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" }, { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" }, {"domain" : "common", "property" : "environment", "width" : 140, "locked" : false, "edit" : false, "label" : "Environment", "filter" : "" }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" }, { "domain" : "storage", "property" : "fileFormat", "width" : 140, "locked" : false, "edit" : false, "label" : "Format", "filter" : "" } ]}'
					WHERE id = 6""")

		//application
		sql("""UPDATE dataview 
					SET report_schema ='{ "domains" : [ "common", "application" ], "sort" : { "domain" : "common", "order" : "a", "property" : "assetName" }, "columns" : [ { "domain" : "common", "property" : "assetName", "width" : 220, "locked" : true, "edit" : false, "label" : "Name", "filter" : "" }, { "domain" : "common", "property" : "moveBundle", "width" : 140, "locked" : false, "edit" : false, "label" : "Bundle", "filter" : "" }, { "domain" : "common", "property" : "environment", "width" : 140, "locked" : false, "edit" : false, "label" : "Environment", "filter" : "" }, { "domain" : "common", "property" : "tagAssets", "width" : 220, "locked" : false, "edit" : false, "label" : "Tags", "filter" : "" }, { "domain" : "common", "property" : "validation", "width" : 140, "locked" : false, "edit" : false, "label" : "Validation", "filter" : "" }, { "domain" : "common", "property" : "planStatus", "width" : 140, "locked" : false, "edit" : false, "label" : "Plan Status", "filter" : "" }, { "domain" : "application", "property" : "sme", "width" : 140, "locked" : false, "edit" : false, "label" : "SME1", "filter" : "" }, { "domain" : "application", "property" : "sme2", "width" : 140, "locked" : false, "edit" : false, "label" : "SME2", "filter" : "" }, { "domain" : "application", "property" : "appOwner", "width" : 140, "locked" : false, "edit" : false, "label" : "App Owner", "filter" : "" } ]}'
					WHERE id = 7""")


	}
}
