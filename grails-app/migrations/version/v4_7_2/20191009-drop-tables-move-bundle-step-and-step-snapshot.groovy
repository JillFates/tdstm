package version.v4_7_2
/**
 * @author ecantu
 * Drop tables move_bundle_step and step_snapshot.
 * @See TM-16017
 */
databaseChangeLog = {
    changeSet(author: 'ecantu', id: '20191009 TM-16017-1') {
        comment('Drop table move_bundle_step')

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'move_bundle_step')
        }
        sql('DROP TABLE move_bundle_step')
    }

	/*
    changeSet(author: 'ecantu', id: '20191009 TM-16017-2') {
        comment('Drop table step_snapshot')

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'step_snapshot')
        }
        sql('DROP TABLE step_snapshot')
    }
	*/

	changeSet(author: 'oluna', id: '20191119 TM-16431') {
		comment("Recreate table step_snapshot in case that it doesn't exist")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'step_snapshot')
			}
		}
		sql('CREATE TABLE step_snapshot ( id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY)')
	}

}