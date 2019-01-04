import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
    changeSet(author: "oluna", id: "20171109 TM-8094-0") {
        comment('Delete all Favorite Dataview dependencies')

        sql("""
             delete from favorite_dataview;
			""")
    }

    changeSet(author: "dcorrea", id: "20171109 TM-8094-1") {
        comment('Increments current dataview ids above 1000 ')

        sql("""
             update dataview set id=id+1001;
			""")
    }

    changeSet(author: "dcorrea", id: "20171109 TM-8094-2") {
        comment('Updates AUTO_INCREMENT value for dataview table ')

        sql("""
             ALTER TABLE dataview AUTO_INCREMENT=1001;
			""")
    }

    changeSet(author: "dcorrea", id: "20171109 TM-8094-3") {
        comment("Add a All Assets ")
        grailsChange {
            change {

                String viewSpec = '''
                    {
                        "domains":["common","application","database","device","storage"],
                        "sort":{"domain":"common","order":"a","property":"assetName"},
                        "columns":[
                            {"domain":"common","edit":false,"filter":"","label":"Name","locked":true,"property":"assetName","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Asset Class","locked":true,"property":"assetClass","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Description","locked":false,"property":"description","width":299},
                            {"domain":"common","edit":false,"filter":"","label":"Environment","locked":false,"property":"environment","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Plan Status","locked":false,"property":"planStatus","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Validation","locked":false,"property":"validation","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Bundle","locked":false,"property":"moveBundle","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Support","locked":false,"property":"supportType","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"External Ref Id","locked":false,"property":"externalRefId","width":200},
                            {"domain":"common","edit":false,"filter":"","label":"Modified Date","locked":false,"property":"lastUpdated","width":200}
                        ]
                    }
                    '''.stripIndent()

                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                // Update the different settings with the Asset Class fields.
                databaseMigrationService.addSystemView(sql, 1l, 'All assets', viewSpec)

            }
        }
    }

}
