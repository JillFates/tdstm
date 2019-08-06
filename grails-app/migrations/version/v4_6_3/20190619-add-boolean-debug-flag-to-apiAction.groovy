package version.v4_6_3

/**
 * TM-15211 Add debugEnabled column to the ApiAction domain
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "TM-15211") {
		comment('Add debugEnabled column to the ApiAction domain')
		sql ("""
                ALTER TABLE `api_action`
					ADD COLUMN `debug_enabled` TINYINT(1) DEFAULT '0' after `is_remote`;
 		""")
	}

}
