import net.transitionmanager.security.Permission

/**
 * TM-9406 Additional changes to the Credential domain
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "TM-9523-01") {
		comment('Add importResults column to the ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
                    ADD `import_results` MEDIUM_TEXT NOT NULL DEFAULT '' AFTER `http_method`;
 		""")
	}
}