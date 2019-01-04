/**
 * Change the attribute in_progress in the database to reflect the news bar, rather than being in progress.
 */
databaseChangeLog = {
	
	changeSet(author: "jdanahy", id: "20150519 TM-3891-1") {
		comment('Change the name in the column from in_progress to news_bar_mode')
		sql("""
			alter table move_event
			change in_progress news_bar_mode varchar(5);
		""")
	}
	changeSet(author: "jdanahy", id: "20150519 TM-3891-2") {
		comment('Change all instances of false to off in the column')
		sql("""
			UPDATE move_event
			SET news_bar_mode='off'
			Where news_bar_mode='false';
		""")
	}
	changeSet(author: "jdanahy", id: "20150519 TM-3891-3") {
		comment('Change all instances of true to on in the column')
		sql("""
			UPDATE move_event
			SET news_bar_mode='on'
			Where news_bar_mode='true';
		""")
	}
}
