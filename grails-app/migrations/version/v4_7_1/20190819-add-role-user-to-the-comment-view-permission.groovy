package version.v4_7_1
/**
 * @author ecantu
 * Add the ROLE_USER to the CommentView permission.
 * @See TM-15733
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20190628 TM-15733") {
        comment('Add the ROLE_USER to the CommentView permission.')
        sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_item='CommentView'), 'ROLE_USER')""")
    }
}