/**
 * Deletes old unused refresh timer preferences from the user_preferences table
 */
databaseChangeLog = {
	changeSet(author: "rmacfarlane", id: "20150721 TM-3904") {
		comment('Delete old preferences for refresh timers')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM user_preference WHERE preference_code in ("MYTASKS_REFRESH", "TASKMGR_REFRESH", "DASHBOARD_REFRESH");')
			}
		}
		
		sql("DELETE FROM user_preference WHERE preference_code in ('MYTASKS_REFRESH', 'TASKMGR_REFRESH', 'DASHBOARD_REFRESH');")
	}
}

