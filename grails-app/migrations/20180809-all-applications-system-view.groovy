import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
    changeSet(author: "slopez", id: "20180809 TM-10210-1") {
        comment("Add an All Applications system view ")
        grailsChange {
            change {

                String viewSpec = '''
                    {
                        "domains":["common","application"],
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
                databaseMigrationService.addSystemView(sql, 7l, 'All Applications', viewSpec)
            }
        }
    }

}