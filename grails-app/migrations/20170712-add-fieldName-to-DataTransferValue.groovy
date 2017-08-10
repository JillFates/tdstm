/**
 * TM-6585 adding the field_name Column into the DataTransferValue table
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170712 TM-6585 adding the field_name column") {
        comment("Add column field_name to DataTransferValue table")
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'data_transfer_value', columnName: 'field_name')
            }
        }

        sql("""
			ALTER TABLE `data_transfer_value`
				ADD COLUMN `field_name` varchar(255) NULL AFTER `eav_attribute_id`
		""")

        sql("""
            ALTER TABLE `data_transfer_value` MODIFY COLUMN `eav_attribute_id` BIGINT(20) NULL
        """)
    }
}
