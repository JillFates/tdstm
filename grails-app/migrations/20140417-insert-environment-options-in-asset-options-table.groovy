/**
 * Inserting ENVIRONMENT_OPTION in asset_options table
 * ENVIRONMENT_OPTION = ['Production','DR','Development','QA','Staging','UAT'] by default.
 */

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20140417 TM-2630-1") {
		comment(" Inserting environment_options in asset_options table")
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "select count(*) from asset_options where type ='ENVIRONMENT_OPTION' ")
		}

		grailsChange {
			change {
				def values= ['Production','DR','Development','QA','Staging','UAT']
				values.each{ value->
					sql.execute("INSERT INTO asset_options (type, value) VALUES ('ENVIRONMENT_OPTION', ${value});")
				}
			}
		}
	}
}
