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

    changeSet(author: 'oluna', id: '20191009 TM-16432') {
        comment('Drop table step_snapshot, this only works with the newest version of TMI_8.0')

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'step_snapshot')
        }
        sql('DROP TABLE step_snapshot')
    }

}