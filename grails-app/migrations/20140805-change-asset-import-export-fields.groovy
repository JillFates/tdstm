/**
 * This migration script changes the "Server" column on the Server sheet to "Name" and the sheet name from "Servers" to "Devices"
 */

databaseChangeLog = {
	changeSet(author: "rmacfarlane", id: "20140805 TM-3056-1") {
		comment('Change the "Server" column on the Server sheet to "Name" and the sheet name from "Servers" to "Devices"')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM data_transfer_attribute_map WHERE column_name = "Name" AND sheet_name = "Servers"')
			sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM data_transfer_attribute_map WHERE sheet_name = "Devices"')
		}
		sql('UPDATE data_transfer_attribute_map SET column_name = "Name" WHERE column_name = "Server"')
		sql('UPDATE data_transfer_attribute_map SET sheet_name = "Devices" WHERE sheet_name = "Servers"')
	}
}
