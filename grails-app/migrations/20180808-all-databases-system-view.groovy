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
                          	{"domain":"common","property":"id","width":200,"locked":false,"edit":false,"label":"Id","filter":""},
							{"domain":"common","property":"assetName","width":200,"locked":false,"edit":false,"label":"Name","filter":""},
							{"domain":"common","property":"description","width":200,"locked":false,"edit":false,"label":"Description","filter":""},
							{"domain":"common","property":"environment","width":200,"locked":false,"edit":false,"label":"Environment","filter":""},
							{"domain":"common","property":"moveBundle","width":200,"locked":false,"edit":false,"label":"Bundle","filter":""},
							{"domain":"common","property":"tagAssets","width":200,"locked":false,"edit":false,"label":"Tags","filter":""},
							{"domain":"common","property":"validation","width":200,"locked":false,"edit":false,"label":"Validation","filter":""},
							{"domain":"common","property":"planStatus","width":200,"locked":false,"edit":false,"label":"Plan Status","filter":""},
							{"domain":"common","property":"externalRefId","width":200,"locked":false,"edit":false,"label":"External Ref Id","filter":""},
							{"domain":"common","property":"lastUpdated","width":200,"locked":false,"edit":false,"label":"Modified Date","filter":""},
							{"domain":"database","property":"dbFormat","width":200,"locked":false,"edit":false,"label":"Format","filter":""},
							{"domain":"database","property":"size","width":200,"locked":false,"edit":false,"label":"Size","filter":""},
							{"domain":"database","property":"scale","width":200,"locked":false,"edit":false,"label":"Scale","filter":""},
							{"domain":"database","property":"rateOfChange","width":200,"locked":false,"edit":false,"label":"Rate Of Change","filter":""},
							{"domain":"common","property":"supportType","width":200,"locked":false,"edit":false,"label":"Support","filter":""},
							{"domain":"database","property":"retireDate","width":200,"locked":false,"edit":false,"label":"Retire Date","filter":""},
							{"domain":"database","property":"maintExpDate","width":200,"locked":false,"edit":false,"label":"Maint Expiration","filter":""}
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
