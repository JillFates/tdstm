/**
 * Nulls out orphaned references to MoveEvent in Tasks and Comments.
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20170731 TM-6792-1") {
        comment('Nulls out orphaned references to MoveEvent in Tasks and Comments.')
        sql("""
            UPDATE asset_comment ac LEFT JOIN move_event me
                ON (ac.move_event_id = me.move_event_id) 
            SET ac.move_event_id = NULL
            WHERE ac.move_event_id IS NOT NULL AND me.move_event_id IS NULL
            """)
    }
}
