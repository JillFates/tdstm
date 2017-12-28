databaseChangeLog = {
    changeSet(author: "arecordon", id: "TM-8603-1") {
        comment("Make API Action's Callback Mode column nullable.")
        sql ("ALTER TABLE api_action MODIFY callback_mode VARCHAR(64)")
    }

    changeSet(author: "arecordon", id: "TM-8603-2") {
        comment("Make API Action's Callback Method column nullable.")
        sql ("ALTER TABLE api_action MODIFY callback_method VARCHAR(64)")
    }
}