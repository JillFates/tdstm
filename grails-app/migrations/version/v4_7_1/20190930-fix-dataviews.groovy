package version.v4_7_1

databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20190930 TM-15976-0") {
		comment('Delete all Favorite Dataview dependencies')

		preConditions(onFail: 'MARK_RAN') {
			sqlCheck(expectedResult: '0', 'select count(*) from DATABASECHANGELOG where ID = "20180809 TM-10208-1"')
		}

		sql("delete from favorite_dataview;")
	}

	changeSet(author: "tpelletier", id: "20171109 TM-15976-1") {
		comment('Increments current dataview ids above the highest id ')

		preConditions(onFail: 'MARK_RAN') {
			sqlCheck(expectedResult: '0', 'select count(*) from DATABASECHANGELOG where ID = "20180809 TM-10208-1"')
		}

		grailsChange {
			change {
				int highestId = sql.firstRow("SELECT id FROM dataview ORDER BY id DESC LIMIT 0, 1").id

				sql.executeUpdate("""
					UPDATE dataview SET id=id+$highestId
					WHERE id < 1001 AND is_system=0 and project_id <> 2 and person_id is not null;
				""")
			}
		}
	}

	changeSet(author: "tpelletier", id: "20171109 TM-15976-2") {
		comment('Updates AUTO_INCREMENT value for dataview table ')

		preConditions(onFail: 'MARK_RAN') {
			sqlCheck(expectedResult: '0', 'select count(*) from DATABASECHANGELOG where ID = "20180809 TM-10208-1"')
		}

		grailsChange {
			change {
				int highestId = sql.firstRow("SELECT id FROM dataview ORDER BY id DESC LIMIT 0, 1").id

				sql.executeUpdate("""
					ALTER TABLE dataview AUTO_INCREMENT=${highestId + 1};
				""")
			}
		}
	}

	changeSet(author: "tpelletier", id: "20171109 TM-15976-3") {
		comment('Update party relationship role type code from/to id to be varchar(255)')

		sql(""" 
			ALTER TABLE `party_relationship` MODIFY `role_type_code_from_id` VARCHAR(255);
			ALTER TABLE `party_relationship` MODIFY `role_type_code_to_id` VARCHAR(255);
		""")
	}

}
