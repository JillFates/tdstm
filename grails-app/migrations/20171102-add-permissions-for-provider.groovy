/**
 * Create the different Permission for working with Provider.
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171102 TM-7226-1") {
        comment('Create the different Permission for working with Provider.')
        grailsChange {
            change {

                Map perms = [
                        'ProviderCreate': [
                                description: 'Can create new Providers for import and actions.',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'ProviderDelete' : [
                                description: 'Can delete existing Providers.',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'ProviderUpdate' : [
                                description: 'Can modify existing Providers.',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'ProviderView' : [
                                description: 'Can view Provider dialogs and lists.',
                                roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                        ]
                ]

                ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
            }
        }
    }

}
