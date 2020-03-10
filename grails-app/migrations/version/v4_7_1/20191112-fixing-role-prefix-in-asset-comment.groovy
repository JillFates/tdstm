/**
 * @author ecantu
 * Removes the 'ROLE_' prefix from the role property in the asset_comment table.
 * This was a residual issue from '20181219-Updating-role-prefix.groovy' and
 * '20190701-Fixing-role-prefix-rollbacks.groovy' changes.
 *
 * Also, it was found that there could be asset_comment records where the 'role' field is not
 * even present in the role_type table, which is to say that it doesn't exist. In that case
 * the role is removed from the asset_comment record, which will be considered a task not assigned.
 *
 * See TM-16364
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20191112 TM-16364-1") {
        comment('Fixing asset_comment role prefix ROLE_.')
        sql('''UPDATE asset_comment 
                       SET role = SUBSTRING(role,6)
                       WHERE role like "ROLE_%";''')
    }

    changeSet(author: "ecantu", id: "20191112 TM-16364-2") {
        comment('Take care of all the possible non-existent roles present in the asset_comment table')
        sql('''UPDATE asset_comment 
                       SET role = null 
                       WHERE role not in (SELECT role_type_code FROM role_type) 
                       AND role is not null 
                       AND role !='';''')
    }

}
