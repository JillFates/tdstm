/**
 * Add Sequence logic
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140922 TM-2899-1") {
		comment('DROP existing stored procedure')
		
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'1', """SELECT count(*) 
				FROM INFORMATION_SCHEMA.ROUTINES 
				WHERE 
				   ROUTINE_TYPE='FUNCTION' and 
				   ROUTINE_SCHEMA='tdstm' and 
				   ROUTINE_NAME='tdstm_sequencer'
				"""
			)
		}
		
		sql("""
				DROP FUNCTION `tdstm`.`tdstm_sequencer`;
			""")
	}

	// This caused an error when deploying to production (see TM-2548)
	// We need to move this migration to a new migration script once we update production and resolve this issue
	// We also need to do a DROP function first for systems that did get the function
	
	changeSet(author: "eluna", id: "20140922 TM-2899-2") {
		comment('Add the SP')
		
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', """SELECT count(*) 
				FROM INFORMATION_SCHEMA.ROUTINES 
				WHERE 
				   ROUTINE_TYPE='PROCEDURE' and 
				   ROUTINE_SCHEMA='tdstm' and 
				   ROUTINE_NAME='tdstm_sequencer'
				"""
			)
		}
		createProcedure """
				CREATE PROCEDURE `tdstm_sequencer`(IN context_id BIGINT, IN name VARCHAR(16), OUT sequence_number bigint(20))
				BEGIN
				
				SET @prevs := NULL;
				
				INSERT INTO sequence_number(context_id, name, last) VALUES (context_id, name, 1)
				ON DUPLICATE KEY UPDATE last = IF((@prevs := last) <> NULL IS NULL, last + 1, NULL);
				
				SET sequence_number = IF(ISNULL(@prevs), 1, @prevs + 1);
				END;
			"""
	}
}
