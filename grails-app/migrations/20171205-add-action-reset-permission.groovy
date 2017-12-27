/**
 * @author ecantu
 * TM-7786 add API Action permission for Action Reset
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20171205 TM-7786") {
        comment('Add API Action permission for Action Reset')

        grailsChange {
            change {
                def perms = [
                        'ActionReset': [
                                group      : 'NONE',
                                description: 'Can reset Integration Actions',
                                roles      : ['ADMIN', 'CLIENT_ADMIN']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}
