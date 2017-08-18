
/**
 * @author slopez
 * TM-6844 add API Action permissions for Downstream
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170816 TM-6844") {
        comment('Add API Action permissions for Downstream')

        grailsChange {
            change {
                def perms = [
                        'ActionCreate': [
                                group      : 'NONE',
                                description: 'Can create new Integration Actions',
                                roles      : ['ADMIN', 'CLIENT_ADMIN']
                        ],
                        'ActionEdit': [
                                group      : 'NONE',
                                description: 'Can edit existing Integration Actions',
                                roles      : ['ADMIN', 'CLIENT_ADMIN']
                        ],
                        'ActionDelete': [
                                group      : 'NONE',
                                description: 'Can delete existing Integration Actions',
                                roles      : ['ADMIN', 'CLIENT_ADMIN']
                        ],
                        'ActionInvoke': [
                                group      : 'NONE',
                                description: 'Can invoke an Integration Actions',
                                roles      : ['ADMIN', 'CLIENT_ADMIN', 'SUPERVISOR', 'EDITOR']
                        ],
                        'ActionAssignment': [
                                group      : 'NONE',
                                description: 'Can assign an Integration Action to a task',
                                roles      : ['ADMIN', 'CLIENT_ADMIN', 'SUPERVISOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}
