/**
 * This migration will change the column "type" to "type_id" in the "notice" table
 * @author @tavo_luna
 */

databaseChangeLog = {
	changeSet(author: "oluna", id: "20161012 TM-5397_NOTICE_TYPE_ID_COLUMN") {
		comment('Create column "type_id" so support "noticeType" id for notices')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'notice', columnName:'type_id')
			}
		}

		//Lets add the new typeId representation
		sql(" alter table `notice` add column `type_id` int not null; ")

		//Migrate old Values
		Notice.NoticeType.values().each { nType ->
			sql(" update notice set type_id = ${nType.id} where type='${nType.name()}'")
		}

		//Drop old Column
		sql(" alter table `notice` drop column `type`; ")

		//Yay! we are Done
	}
}