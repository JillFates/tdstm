import net.transitionmanager.security.Permission
/**
 * @author aalbuquerque
 * TM-6843 add new permissions to the Asset explorer epic feature
 */
databaseChangeLog = {
    changeSet(author: "aalbuquerque", id: "20170807 TM-6843") {
        comment('add new permissions to the Asset explorer epic feature')

        grailsChange {
            change {
                def perms = [
					(Permission.AssetExplorerSystemCreate): [
                        group      : 'NONE',
                        description: 'Can create shared system level reports',
                        roles      : ['ADMIN']
                    ],
					(Permission.AssetExplorerSystemDelete): [
                        group      : 'NONE',
                        description: 'Can delete shared system level reports',
                        roles      : ['ADMIN']
                    ],
                    (Permission.AssetExplorerSystemEdit): [
                        group      : 'NONE',
                        description: 'Can edit shared system level reports',
                        roles      : ['ADMIN']
                    ],
                    (Permission.AssetExplorerSystemSaveAs): [
                        group      : 'NONE',
                        description: 'Can modify and \'Save As\' system level reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
                    (Permission.AssetExplorerCreate): [
                        group      : 'NONE',
                        description: 'Can create logged in user owned reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
					(Permission.AssetExplorerDelete): [
                        group      : 'NONE',
                        description: 'Can delete logged in user owned reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
                    (Permission.AssetExplorerEdit): [
                        group      : 'NONE',
                        description: 'Can edit logged in user owned reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
                    (Permission.AssetExplorerSaveAs): [
                        group      : 'NONE',
                        description: 'Can modify and \'Save As\' user published reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
					(Permission.AssetExplorerPublish): [
                        group      : 'NONE',
                        description: 'Can publish logged in user owned reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
                    (Permission.AssetExplorerUnPublish): [
                        group      : 'NONE',
                        description: 'Can unpublish logged in user owned reports',
                        roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
                    ],
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}
