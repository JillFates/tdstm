
/**
 * @author oluna
 * TM-2083 add new permissions to COVER CLONE ASSETS FEATURES
 */
databaseChangeLog = {
    changeSet(author: "oluna", id: "20170407 TM-2083") {
        comment('add new permissions to COVER CLONE ASSETS FEATURES')

        grailsChange {
            change {
                def perms = [
                        'AssetCloneDependencies'          : [
                                group      : 'NONE',
                                description: 'Enables Include Dependencies option in Clone Assets Dialog',
                                roles      : []
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}
