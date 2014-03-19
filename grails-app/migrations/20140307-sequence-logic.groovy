/**
 * Add Sequence logic
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140307 TM-2511-1") {
		comment('Add sequence table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'sequence_number')
			}
		}
		sql("""
				CREATE TABLE `sequence_number` (
				  `context_id` bigint(20) NOT NULL,
				  `name` varchar(16) NOT NULL,
				  `last` bigint(20) NOT NULL DEFAULT '1',
				  PRIMARY KEY (`context_id`,`name`)
				) ENGINE=InnoDB;
			""")
	}
	/*

	// This caused an error when deploying to production (see TM-2548)
	// We need to move this migration to a new migration script once we update production and resolve this issue
	// We also need to do a DROP function first for systems that did get the function
	
	changeSet(author: "eluna", id: "20140307 TM-2511-2") {
		comment('Add function')
		
		preConditions(onFail:'MARK_RAN') {
			tableExists(schemaName:'tdstm', tableName:'sequence_number')
		}
		createProcedure """
				CREATE FUNCTION `tdstm_sequencer`(context_id BIGINT, name VARCHAR(16)) RETURNS bigint(20)
				BEGIN
				
				SET @prevs := NULL;
				
				INSERT INTO sequence_number(context_id, name, last) VALUES (context_id, name, 1)
				ON DUPLICATE KEY UPDATE last = IF((@prevs := last) <> NULL IS NULL, last + 1, NULL);
				
				return IF(ISNULL(@prevs), 1, @prevs + 1);
				END;
			"""
	}
	*/
}
