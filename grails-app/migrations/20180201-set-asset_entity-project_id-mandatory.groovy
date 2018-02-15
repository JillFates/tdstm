databaseChangeLog = {
    changeSet(author: "oluna", id: "TM-8939-1") {
        comment("maske project_id from asset_entity mandatory")
        sql ("ALTER TABLE asset_entity MODIFY project_id bigint(20) NOT NULL")
    }
}
