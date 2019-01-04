import groovy.sql.Sql
import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170224 TM-6063.2.a") {
		comment('Fix "license.environment" column enumerations for client')

		preConditions(onFail:'MARK_RAN') {
			grailsPrecondition {
				check {
					def schemaName = databaseConnection.catalog

					def sql = new Sql(connection) //this is fixed in 1.4.1
					def row = sql.firstRow("""
						SELECT COLUMN_TYPE
						FROM information_schema.COLUMNS
						WHERE 
							TABLE_SCHEMA = ${schemaName} and 
							TABLE_NAME = 'license' and 
							COLUMN_NAME = 'environment'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `license`
					ADD COLUMN `t_environment` VARCHAR(255) NOT NULL AFTER `environment`;
				""")

				def enumList = License.Environment.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE license set t_environment=${enu.name()} WHERE environment=${id}")
				}

				sql.execute("""
					ALTER TABLE `license`
						DROP COLUMN `environment`,
						CHANGE COLUMN `t_environment` `environment` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}

	changeSet(author: "oluna", id: "20170224 TM-6063.2.b") {
		comment('Fix "license.status" columns enumerations for client')

		preConditions(onFail:'MARK_RAN') {
			grailsPrecondition {
				check {
					def schemaName = databaseConnection.catalog

					def sql = new Sql(connection) //this is fixed in 1.4.1
					def row = sql.firstRow("""
						SELECT COLUMN_TYPE
						FROM information_schema.COLUMNS
						WHERE 
							TABLE_SCHEMA = ${schemaName} and 
							TABLE_NAME = 'license' and 
							COLUMN_NAME = 'status'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `license`
					ADD COLUMN `t_status` VARCHAR(255) NOT NULL AFTER `status`;
				""")

				def enumList = License.Status.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE license set t_status=${enu.name()} WHERE status=${id}")
				}

				sql.execute("""
					ALTER TABLE `license`
						DROP COLUMN `status`,
						CHANGE COLUMN `t_status` `status` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}


	changeSet(author: "oluna", id: "20170224 TM-6063.2.c") {
		comment('Fix "license.type" columns enumerations for client')

		preConditions(onFail:'MARK_RAN') {
			grailsPrecondition {
				check {
					def schemaName = databaseConnection.catalog

					def sql = new Sql(connection) //this is fixed in 1.4.1
					def row = sql.firstRow("""
						SELECT COLUMN_TYPE
						FROM information_schema.COLUMNS
						WHERE 
							TABLE_SCHEMA = ${schemaName} and 
							TABLE_NAME = 'license' and 
							COLUMN_NAME = 'type'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `license`
					ADD COLUMN `t_type` VARCHAR(255) NOT NULL AFTER `type`;
				""")

				def enumList = License.Type.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE license set t_type=${enu.name()} WHERE type=${id}")
				}

				sql.execute("""
					ALTER TABLE `license`
						DROP COLUMN `type`,
						CHANGE COLUMN `t_type` `type` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}

	changeSet(author: "oluna", id: "20170224 TM-6063.2.d") {
		comment('Fix "license.method" columns enumerations for client')
		//This column was already a Varchar

		grailsChange {
			change {
				def enumList = License.Method.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE license set `method`=${enu.name()} WHERE `method`=${id}")
				}
			}
		}

	}

}
