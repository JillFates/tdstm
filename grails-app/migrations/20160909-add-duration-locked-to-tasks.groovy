/**
 * @author arecordon
 * TM-5286 Add Task attribute durationLocked to the Task schema
 */
databaseChangeLog = {
	changeSet(author: "arecordon", id: "20160909 TM-5286-1") {
		sql("ALTER TABLE asset_comment ADD COLUMN duration_locked TINYINT(1) NOT NULL DEFAULT 0");
	}
}
