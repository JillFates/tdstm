/**
* This changelog will update the date_created to last_modified where date_created is null.
*/

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20140320 TM-2546") {
		comment('change set is used to update date_created null value to last_updated')
		sql("update asset_comment set last_updated= UTC_TIMESTAMP() where last_updated is null")
		sql("update asset_comment set date_created= last_updated where date_created is null")
		sql("alter table asset_comment modify column date_created DATETIME not null")
	}
}