import net.transitionmanager.common.DatabaseMigrationService

databaseChangeLog = {
    changeSet(author: "slopez", id: "20180809 TM-10209-1") {
        comment("Add an All Storage physical system view ")
        grailsChange {
            change {

                String viewSpec = '''
                    {
                        "domains":["common","device"],
                        "sort":{"domain":"common","order":"a","property":"assetName"},
                        "columns":[
                            {"domain":"common","property":"id","width":200,"locked":false,"edit":false,"label":"Id","filter":""},
                            {"domain":"common","property":"assetName","width":200,"locked":false,"edit":false,"label":"Name","filter":""},
                            {"domain":"common","property":"description","width":200,"locked":false,"edit":false,"label":"Description","filter":""},
                            {"domain":"common","property":"moveBundle","width":200,"locked":false,"edit":false,"label":"Bundle","filter":""},
                            {"domain":"common","property":"tagAssets","width":200,"locked":false,"edit":false,"label":"Tags","filter":""},
                            {"domain":"common","property":"validation","width":200,"locked":false,"edit":false,"label":"Validation","filter":""},
                            {"domain":"common","property":"planStatus","width":200,"locked":false,"edit":false,"label":"Plan Status","filter":""},
                            {"domain":"common","property":"externalRefId","width":200,"locked":false,"edit":false,"label":"External Ref Id","filter":""},
                            {"domain":"common","property":"lastUpdated","width":200,"locked":false,"edit":false,"label":"Modified Date","filter":""},
                            {"domain":"device","property":"shortName","width":200,"locked":false,"edit":false,"label":"Alternate Name","filter":""},
                            {"domain":"device","property":"serialNumber","width":200,"locked":false,"edit":false,"label":"Serial #","filter":""},
                            {"domain":"device","property":"assetTag","width":200,"locked":false,"edit":false,"label":"Asset Tag","filter":""},
                            {"domain":"device","property":"manufacturer","width":200,"locked":false,"edit":false,"label":"Manufacturer","filter":""},
                            {"domain":"device","property":"model","width":200,"locked":false,"edit":false,"label":"Model","filter":""},
                            {"domain":"device","property":"assetType","width":200,"locked":false,"edit":false,"label":"Device Type","filter":""},
                            {"domain":"device","property":"ipAddress","width":200,"locked":false,"edit":false,"label":"IP Address","filter":""},
                            {"domain":"device","property":"os","width":200,"locked":false,"edit":false,"label":"OS","filter":""},
                            {"domain":"device","property":"locationSource","width":200,"locked":false,"edit":false,"label":"Source Location","filter":""},
                            {"domain":"device","property":"roomSource","width":200,"locked":false,"edit":false,"label":"Source Room","filter":""},
                            {"domain":"device","property":"rackSource","width":200,"locked":false,"edit":false,"label":"Source Rack","filter":""},
                            {"domain":"device","property":"sourceRackPosition","width":200,"locked":false,"edit":false,"label":"Source Position","filter":""},
                            {"domain":"device","property":"sourceChassis","width":200,"locked":false,"edit":false,"label":"Source Chassis","filter":""},
                            {"domain":"device","property":"sourceBladePosition","width":200,"locked":false,"edit":false,"label":"Source Blade Position","filter":""},
                            {"domain":"device","property":"locationTarget","width":200,"locked":false,"edit":false,"label":"Target Location","filter":""},
                            {"domain":"device","property":"roomTarget","width":200,"locked":false,"edit":false,"label":"Target Room","filter":""},
                            {"domain":"device","property":"rackTarget","width":200,"locked":false,"edit":false,"label":"Target Rack","filter":""},
                            {"domain":"device","property":"targetRackPosition","width":200,"locked":false,"edit":false,"label":"Target Position","filter":""},
                            {"domain":"device","property":"targetChassis","width":200,"locked":false,"edit":false,"label":"Target Chassis","filter":""},
                            {"domain":"device","property":"targetBladePosition","width":200,"locked":false,"edit":false,"label":"Target Blade Position","filter":""},
                            {"domain":"common","property":"supportType","width":200,"locked":false,"edit":false,"label":"Support","filter":""},
                            {"domain":"device","property":"retireDate","width":200,"locked":false,"edit":false,"label":"Retire Date","filter":""},
                            {"domain":"device","property":"maintExpDate","width":200,"locked":false,"edit":false,"label":"Maint Expiration","filter":""},
                            {"domain":"device","property":"priority","width":200,"locked":false,"edit":false,"label":"Priority","filter":""},
                            {"domain":"device","property":"truck","width":200,"locked":false,"edit":false,"label":"Truck","filter":""},
                            {"domain":"device","property":"cart","width":200,"locked":false,"edit":false,"label":"Cart","filter":""},
                            {"domain":"device","property":"shelf","width":200,"locked":false,"edit":false,"label":"Shelf","filter":""},
                            {"domain":"device","property":"railType","width":200,"locked":false,"edit":false,"label":"Rail Type","filter":""},
                            {"domain":"device","property":"size","width":200,"locked":false,"edit":false,"label":"Size","filter":""},
                            {"domain":"device","property":"scale","width":200,"locked":false,"edit":false,"label":"Scale","filter":""},
                            {"domain":"device","property":"rateOfChange","width":200,"locked":false,"edit":false,"label":"Rate Of Change","filter":""},
                            {"domain": "device","edit": false,"filter": "Array|Disk|NAS|SAN|SAN Switch|Storage|Tape|Tape Library|Virtual Tape Library","label": "Device Type","locked": false,"property": "assetType","width": 200}        
                        ]
                    }
                    '''.stripIndent()

                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                // Update the different settings with the Asset Class fields.
                databaseMigrationService.addSystemView(sql, 5l, 'All Storage - Physical', viewSpec)
            }
        }
    }

    changeSet(author: "slopez", id: "20180809 TM-10209-2") {
        comment("Add an All Storage Virtual system view ")
        grailsChange {
            change {

                String viewSpec = '''
                    {
                        "domains":["common","storage"],
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
                            {"domain":"storage","property":"LUN","width":200,"locked":false,"edit":false,"label":"LUN","filter":""},
                            {"domain":"storage","property":"fileFormat","width":200,"locked":false,"edit":false,"label":"Format","filter":""},
                            {"domain":"storage","property":"size","width":200,"locked":false,"edit":false,"label":"Size","filter":""},
                            {"domain":"storage","property":"scale","width":200,"locked":false,"edit":false,"label":"Scale","filter":""},
                            {"domain":"storage","property":"rateOfChange","width":200,"locked":false,"edit":false,"label":"Rate Of Change","filter":""},
                            {"domain":"common","property":"supportType","width":200,"locked":false,"edit":false,"label":"Support","filter":""}
                        ]
                    }
                    '''.stripIndent()

                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                // Update the different settings with the Asset Class fields.
                databaseMigrationService.addSystemView(sql, 6l, 'All Storage - Virtual', viewSpec)
            }
        }
    }
}
