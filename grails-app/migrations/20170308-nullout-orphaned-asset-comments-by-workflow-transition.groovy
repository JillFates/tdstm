
/**
 * Null out Asset Comments orphaned by a missing workflow transition
 */
databaseChangeLog = {

    changeSet(author: "slopez", id: "20170308 TM-6116") {
        comment('Null out Asset Comments orphaned by a missing workflow transition')

        List statements = [
                """UPDATE asset_comment 
                    SET 
                        workflow_transition_id = NULL
                    WHERE
                        workflow_transition_id NOT IN (SELECT 
                                workflow_transition_id
                            FROM
                                workflow_transition);"""
        ]

        grailsChange {
            change {
                statements.each {s ->
                    int n = sql.executeUpdate(s)
                    println "${s.split(/\n/)[0]} updated $n row${n!=1 ? 's' : ''}"
                }
            }
        }
    }
}
