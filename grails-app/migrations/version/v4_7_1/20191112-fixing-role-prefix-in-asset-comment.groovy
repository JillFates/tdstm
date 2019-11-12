/**
 * @author ecantu
 * Removes the 'ROLE_' prefix from the role property in the asset_comment table.
 * This was a residual issue from '20181219-Updating-role-prefix.groovy' and
 * '20190701-Fixing-role-prefix-rollbacks.groovy' changes.
 * See TM-16364
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20191112 TM-16364-1") {
        comment('Fixing asset_comment role prefix ROLE_.')
        sql('''UPDATE asset_comment 
                       SET role = SUBSTRING(role,6)
                       WHERE role like "ROLE_%";''')
    }

}
