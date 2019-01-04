databaseChangeLog = {
    changeSet(author: "arecordon", id: "TM-8603-1") {
        comment("Make API Action's Callback Mode, Method, Timeout and Async Queue columns nullable.")
        sql ("""ALTER TABLE api_action 
                MODIFY callback_mode VARCHAR(64),
                MODIFY callback_method VARCHAR(64),
                MODIFY timeout INT(8),
                MODIFY async_queue VARCHAR(64)
            """)
    }
}
