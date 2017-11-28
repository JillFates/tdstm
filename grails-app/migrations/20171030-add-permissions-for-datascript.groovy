/**
 * Create the different Permission for working with DataScript.
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171030 TM-7228-1") {
        comment('Create the different Permission for working with DataScript.')
        grailsChange {
            change {
                Map perms = [
                        'DataScriptCreate' : [
                                description: 'Can create new DataScript for import and exports',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'DataScriptDelete' : [
                                description: 'Can delete existing DataScript.',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'DataScriptUpdate' : [
                                description: 'Can modify existing DataScript.',
                                roles: ['ADMIN', 'CLIENT_ADMIN']
                        ],

                        'DataScriptView' : [
                                description: 'Can view DataScript dialogs and lists.',
                                roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                        ]
                ]

                ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
            }
        }
    }

}
