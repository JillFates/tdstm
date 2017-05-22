
/**
 * Null out Asset Comments orphaned by a missing workflow transition
 */
databaseChangeLog = {

    changeSet(author: "slopez", id: "20170417 TM-5297-1") {
        comment('Null out Asset Comments orphaned by a deleted/missing Move Events')

        List statements = [
                """DELETE FROM user_preference 
                    WHERE
                        preference_code = 'MOVE_EVENT'
                        AND value NOT IN (SELECT move_event_id FROM move_event);"""
        ]

        grailsChange {
            change {
                statements.each {s ->
                    int n = sql.executeUpdate(s)
                    println "${s.split(/\n/)[0]} asset comments updated $n row${n!=1 ? 's' : ''}"
                }
            }
        }
    }

    changeSet(author: "slopez", id: "20170417 TM-5297-2") {
        comment('Null out User Preference orphaned by a deleted/missing Move Events')

        List statements = [
                """UPDATE asset_comment 
                    SET 
                        move_event_id = NULL
                    WHERE
                        move_event_id NOT IN (SELECT move_event_id FROM move_event);"""
        ]

        grailsChange {
            change {
                statements.each {s ->
                    int n = sql.executeUpdate(s)
                    println "${s.split(/\n/)[0]} user preferences updated $n row${n!=1 ? 's' : ''}"
                }
            }
        }
    }
}
