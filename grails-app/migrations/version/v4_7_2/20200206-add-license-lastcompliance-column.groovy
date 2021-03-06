package version.v4_7_2

import net.transitionmanager.license.License

databaseChangeLog = {
    changeSet(author: 'oluna', id: '20200206 TM-16920-1') {
        comment("Add lastCompliance column to License table")

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'license', columnName: 'last_compliance_hash')
            }
        }

        addColumn(tableName: "license") {
            column(name: "last_compliance_hash", type: "varchar(255)")
        }
    }

    changeSet(author: 'oluna', id: '20200206 TM-16920-2') {
        comment("Initialize Date compliance in License table")
        preConditions() {
            columnExists(tableName: 'license', columnName: 'last_compliance_hash')
        }
        grailsChange {
            change {
                def licenses = License.findAllByLastComplianceHashIsNull()
                licenses?.each { lic ->
                    lic.settleCompliance()
                    lic.save()
                }
            }
        }
    }

    changeSet(author: 'oluna', id: '20200206 TM-16920-3') {
        comment("Add NOT NULL constraint to last_compliance_hash column in License table")
        preConditions() {
            columnExists(tableName: 'license', columnName: 'last_compliance_hash')
        }
        addNotNullConstraint(tableName: 'license', columnName: 'last_compliance_hash', columnDataType: 'varchar(255)')
    }

}
