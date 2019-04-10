import net.transitionmanager.common.DatabaseMigrationService

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
                            {"domain":"application","property":"appVendor","width":200,"locked":false,"edit":false,"label":"Vendor","filter":""},
                            {"domain":"application","property":"appVersion","width":200,"locked":false,"edit":false,"label":"Version","filter":""},
                            {"domain":"application","property":"appTech","width":200,"locked":false,"edit":false,"label":"Technology","filter":""},
                            {"domain":"application","property":"appSource","width":200,"locked":false,"edit":false,"label":"Source","filter":""},
                            {"domain":"application","property":"license","width":200,"locked":false,"edit":false,"label":"License","filter":""},
                            {"domain":"common","property":"supportType","width":200,"locked":false,"edit":false,"label":"Support","filter":""},
                            {"domain":"application","property":"businessUnit","width":200,"locked":false,"edit":false,"label":"Business Unit","filter":""},
                            {"domain":"application","property":"appOwner","width":200,"locked":false,"edit":false,"label":"App Owner","filter":""},
                            {"domain":"application","property":"sme","width":200,"locked":false,"edit":false,"label":"SME1","filter":""},
                            {"domain":"application","property":"sme2","width":200,"locked":false,"edit":false,"label":"SME2","filter":""},
                            {"domain":"application","property":"retireDate","width":200,"locked":false,"edit":false,"label":"Retire Date","filter":""},
                            {"domain":"application","property":"maintExpDate","width":200,"locked":false,"edit":false,"label":"Maint Expiration","filter":""},
                            {"domain":"application","property":"appFunction","width":200,"locked":false,"edit":false,"label":"Function","filter":""},
                            {"domain":"application","property":"criticality","width":200,"locked":false,"edit":false,"label":"Criticality","filter":""},
                            {"domain":"application","property":"userCount","width":200,"locked":false,"edit":false,"label":"User Count","filter":""},
                            {"domain":"application","property":"userLocations","width":200,"locked":false,"edit":false,"label":"User Locations","filter":""},
                            {"domain":"application","property":"useFrequency","width":200,"locked":false,"edit":false,"label":"Use Frequency","filter":""},
                            {"domain":"application","property":"drRpoDesc","width":200,"locked":false,"edit":false,"label":"DR RPO","filter":""},
                            {"domain":"application","property":"drRtoDesc","width":200,"locked":false,"edit":false,"label":"DR RTO","filter":""},
                            {"domain":"application","property":"latency","width":200,"locked":false,"edit":false,"label":"Latency OK","filter":""},
                            {"domain":"application","property":"testProc","width":200,"locked":false,"edit":false,"label":"Test Proc OK","filter":""},
                            {"domain":"application","property":"startupProc","width":200,"locked":false,"edit":false,"label":"Startup Proc OK","filter":""},
                            {"domain":"application","property":"url","width":200,"locked":false,"edit":false,"label":"URL","filter":""},
                            {"domain":"application","property":"shutdownBy","width":200,"locked":false,"edit":false,"label":"Shutdown By","filter":""},
                            {"domain":"application","property":"shutdownFixed","width":200,"locked":false,"edit":false,"label":"Shutdown Fixed","filter":""},
                            {"domain":"application","property":"shutdownDuration","width":200,"locked":false,"edit":false,"label":"Shutdown Duration","filter":""},
                            {"domain":"application","property":"startupBy","width":200,"locked":false,"edit":false,"label":"Startup By","filter":""},
                            {"domain":"application","property":"startupFixed","width":200,"locked":false,"edit":false,"label":"Startup Fixed","filter":""},
                            {"domain":"application","property":"startupDuration","width":200,"locked":false,"edit":false,"label":"Startup Duration","filter":""},
                            {"domain":"application","property":"testingBy","width":200,"locked":false,"edit":false,"label":"Testing By","filter":""},
                            {"domain":"application","property":"testingFixed","width":200,"locked":false,"edit":false,"label":"Testing Fixed","filter":""},
                            {"domain":"application","property":"testingDuration","width":200,"locked":false,"edit":false,"label":"Testing Duration","filter":""}
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
