databaseChangeLog = {
	changeSet(author: "eluna", id: "20140618 TM-2854-1") {
		comment("Fix asset comment durationScale to enum values")
		sql("UPDATE asset_comment SET duration_scale = UPPER(duration_scale)")
		sql("ALTER TABLE asset_comment MODIFY duration_scale CHAR(1) NOT NULL DEFAULT 'D' AFTER duration")
	}
}
