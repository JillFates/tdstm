import groovy.sql.Sql
import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170224 TM-6063.2.lc.a") {
		comment('Fix "licensed_client.environment" column enumerations for client')

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
							TABLE_NAME = 'licensed_client' and 
							COLUMN_NAME = 'environment'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `licensed_client`
					ADD COLUMN `t_environment` VARCHAR(255) NOT NULL AFTER `environment`;
				""")

				def enumList = License.Environment.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE licensed_client set t_environment=${enu.name()} WHERE environment=${id}")
				}

				sql.execute("""
					ALTER TABLE `licensed_client`
						DROP COLUMN `environment`,
						CHANGE COLUMN `t_environment` `environment` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}

	changeSet(author: "oluna", id: "20170224 TM-6063.2.lc.b") {
		comment('Fix "licensed_client.status" columns enumerations for client')

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
							TABLE_NAME = 'licensed_client' and 
							COLUMN_NAME = 'status'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `licensed_client`
					ADD COLUMN `t_status` VARCHAR(255) NOT NULL AFTER `status`;
				""")

				def enumList = License.Status.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE licensed_client set t_status=${enu.name()} WHERE status=${id}")
				}

				sql.execute("""
					ALTER TABLE `licensed_client`
						DROP COLUMN `status`,
						CHANGE COLUMN `t_status` `status` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}


	changeSet(author: "oluna", id: "20170224 TM-6063.2.lc.c") {
		comment('Fix "licensed_client.type" columns enumerations for client')

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
							TABLE_NAME = 'licensed_client' and 
							COLUMN_NAME = 'type'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `licensed_client`
					ADD COLUMN `t_type` VARCHAR(255) NOT NULL AFTER `type`;
				""")

				def enumList = License.Type.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE licensed_client set t_type=${enu.name()} WHERE type=${id}")
				}

				sql.execute("""
					ALTER TABLE `licensed_client`
						DROP COLUMN `type`,
						CHANGE COLUMN `t_type` `type` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}

	changeSet(author: "oluna", id: "20170224 TM-6063.2.lc.d") {
		comment('Fix "licensed_client.method" columns enumerations for client')

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
							TABLE_NAME = 'licensed_client' and 
							COLUMN_NAME = 'method'
					""")

					assert row['COLUMN_TYPE'] == 'int(11)'
				}
			}
		}

		grailsChange {
			change {
				sql.execute("""
					ALTER TABLE `licensed_client`
					ADD COLUMN `t_method` VARCHAR(255) NOT NULL AFTER `method`;
				""")

				def enumList = License.Method.values()

				enumList.eachWithIndex{ enu, i ->
					def id = i+1 //the old id is offset+1
					sql.executeUpdate("UPDATE licensed_client set `t_method`=${enu.name()} WHERE `method`=${id}")
				}

				sql.execute("""
					ALTER TABLE `licensed_client`
						DROP COLUMN `method`,
						CHANGE COLUMN `t_method` `method` VARCHAR(255) NOT NULL;
				""")
			}
		}

	}

}
