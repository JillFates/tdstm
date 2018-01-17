databaseChangeLog = {
    changeSet(author: "arecordon", id: "TM-8697-1") {
        comment("Add columns for supporting reactions in API Actions.")
        sql ("""ALTER TABLE api_action
                ADD COLUMN endpoint_path VARCHAR(255),
                ADD COLUMN endpoint_url VARCHAR(255),
                ADD COLUMN is_polling TINYINT NOT NULL DEFAULT 0,
                ADD COLUMN reaction_scripts JSON NOT NULL,
                ADD COLUMN reaction_scripts_valid TINYINT NOT NULL DEFAULT 0,
                ADD COLUMN use_with_asset TINYINT NOT NULL DEFAULT 0,
                ADD COLUMN use_with_task TINYINT NOT NULL DEFAULT 0,
                ADD COLUMN polling_lapsed_after INT(10) NOT NULL DEFAULT 0,
                ADD COLUMN polling_stalled_after INT(10) NOT NULL DEFAULT 0
            """)
    }
}
