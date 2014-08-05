databaseChangeLog = {
	changeSet(author: "eluna", id: "20140804 TM-3039-1") {
		comment("Fix asset comment task number to NULL")
		sql("UPDATE tdstm.asset_comment SET task_number = NULL WHERE comment_type != 'issue'")
	}
}
