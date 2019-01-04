/**
 * Drop not null constraint on data_transfer_batch table user_login_id column.
 */
databaseChangeLog = {
   changeSet(author: "ecantu", id: "20180104 TM-8396-1") {
      comment('Drop not null constraint on data_transfer_batch table user_login_id column.')
      sql("""
			ALTER TABLE data_transfer_batch MODIFY COLUMN user_login_id BIGINT(20) NULL;
		""")
   }
}
