package version.v4_7_3

databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20190423 TM-17156") {
		comment('Add category codes to assetOptions.')

		preConditions(onFail: 'MARK_RAN') {
			sqlCheck(expectedResult: '0', "SELECT count(*) FROM asset_options WHERE type = 'TASK_CATEGORY'")
		}

		sql("""
			insert into asset_options (type, value) values
			('TASK_CATEGORY', 'general'),
			('TASK_CATEGORY', 'discovery'),
			('TASK_CATEGORY', 'analysis'),
			('TASK_CATEGORY', 'design'),
			('TASK_CATEGORY', 'planning'),
			('TASK_CATEGORY', 'buildout'),
			('TASK_CATEGORY', 'walkthru'),
			('TASK_CATEGORY', 'premove'),
			('TASK_CATEGORY', 'moveday'),
			('TASK_CATEGORY', 'shutdown'),
			('TASK_CATEGORY', 'physical'),
			('TASK_CATEGORY', 'transport'),
			('TASK_CATEGORY', 'startup'),
			('TASK_CATEGORY', 'verify'),
			('TASK_CATEGORY', 'postmove'),
			('TASK_CATEGORY', 'closeout'),
			('TASK_CATEGORY', 'learning')
			""")
	}
}

