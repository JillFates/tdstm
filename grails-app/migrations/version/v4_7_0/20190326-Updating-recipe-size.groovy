package version.v4_7_0

databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20190326 TM-14505") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				sqlCheck(expectedResult: 'mediumtext', """SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
					  WHERE table_name = 'recipe_version' AND COLUMN_NAME = 'source_code';""")
			}
		}

		comment('Updating recipe size to be medium test')
		sql('ALTER TABLE recipe_version CHANGE source_code source_code mediumtext;')
	}
}


