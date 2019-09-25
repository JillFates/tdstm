databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20190920 TM-14929-1') {
		comment("removes old password history")

		sql('TRUNCATE table password_history;')
	}
}
