package version.v4_7_1
/**
 * @author ecantu
 * Add four Model fields.
 * @See TM-11945
 */
databaseChangeLog = {

    changeSet(author: 'ecantu', id: '20190725 TM-11945-1') {
        comment('Add columns cpu_type, cpu_count, memory_size and storage_size to the model table')

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'api_action', columnName: 'dictionary_method_name')
            }
        }

        addColumn(tableName: 'api_action') {
            column(name: 'dictionary_method_name', type: 'VARCHAR(255)') {
                constraints(nullable: 'true')
            }
        }
    }
}
