package version.v4_7_2

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20200103 TM-16715') {
		comment('Making from_address nullable')

		sql ('ALTER TABLE email_dispatch MODIFY COLUMN from_address varchar(100)  NULL;')
	}

}
