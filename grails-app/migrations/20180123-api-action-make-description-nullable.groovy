databaseChangeLog = {
    changeSet(author: "arecordon", id: "TM-8569-1") {
        comment("Make API Action's description nullable")
        sql ("ALTER TABLE api_action MODIFY description VARCHAR(255)")
    }
}
