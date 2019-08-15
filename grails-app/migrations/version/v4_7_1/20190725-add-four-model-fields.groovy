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
                columnExists(tableName: 'model', columnName: 'cpu_type')
                columnExists(tableName: 'model', columnName: 'cpu_count')
                columnExists(tableName: 'model', columnName: 'memory_size')
                columnExists(tableName: 'model', columnName: 'storage_size')
            }
        }

        addColumn(tableName: 'model') {
            column(name: 'cpu_type', type: 'VARCHAR(255)') {
                constraints(nullable: 'true')
            }
        }
        addColumn(tableName: 'model') {
            column(name: 'cpu_count', type: 'BIGINT(20)') {
                constraints(nullable: 'true')
            }
        }
        addColumn(tableName: 'model') {
            column(name: 'memory_size', type: 'FLOAT(10)') {
                constraints(nullable: 'true')
            }
        }
        addColumn(tableName: 'model') {
            column(name: 'storage_size', type: 'FLOAT(10)') {
                constraints(nullable: 'true')
            }
        }
    }
}