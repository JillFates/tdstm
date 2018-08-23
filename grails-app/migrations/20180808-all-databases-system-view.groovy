import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: "slopez", id: "20180808 TM-9670-1") {
		comment("Add a All Databases system view ")
		grailsChange {
			change {

				String viewSpec = '''
                    {
                        "domains":["common", "database"],
                        "sort":{"domain":"common","order":"a","property":"assetName"},
                        "columns": [
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Name",
                            "locked": true,
                            "property": "assetName",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Asset Class",
                            "locked": true,
                            "property": "assetClass",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Description",
                            "locked": false,
                            "property": "description",
                            "width": 299
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Environment",
                            "locked": false,
                            "property": "environment",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Plan Status",
                            "locked": false,
                            "property": "planStatus",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Validation",
                            "locked": false,
                            "property": "validation",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Bundle",
                            "locked": false,
                            "property": "moveBundle",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Support",
                            "locked": false,
                            "property": "supportType",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "External Ref Id",
                            "locked": false,
                            "property": "externalRefId",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "edit": false,
                            "filter": "",
                            "label": "Modified Date",
                            "locked": false,
                            "property": "lastUpdated",
                            "width": 200
                          },
                          {
                            "domain": "common",
                            "property": "custom7",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "CNN",
                            "filter": ""
                          },
                          {
                            "domain": "common",
                            "property": "custom9",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "IPs",
                            "filter": ""
                          },
                          {
                            "domain": "common",
                            "property": "id",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Id",
                            "filter": ""
                          },
                          {
                            "domain": "common",
                            "property": "tagAssets",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Tags",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom3",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Cost Basis",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom2",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Custom2",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom4",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Custom4",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom5",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Custom5",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom6",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Custom6",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom8",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Custom8",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "dbFormat",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Format",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "maintExpDate",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Maint Expiration",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "custom1",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Network Interfaces",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "rateOfChange",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Rate Of Change",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "retireDate",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Retire Date",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "scale",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Scale",
                            "filter": ""
                          },
                          {
                            "domain": "database",
                            "property": "size",
                            "width": 200,
                            "locked": false,
                            "edit": false,
                            "label": "Size",
                            "filter": ""
                          }
                        ]
                    }
                    '''.stripIndent()

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different settings with the Asset Class fields.
				databaseMigrationService.addSystemView(sql, 2l, 'All Databases', viewSpec)

			}
		}
	}

}