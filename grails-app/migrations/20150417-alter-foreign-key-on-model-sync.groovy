/**
 * Alter foreign key on table model_sync
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150417 TM-3813-3") {
		comment('Alter foreign key on table model_sync')
		sql("ALTER TABLE model_sync DROP foreign key FK7D7869F1AD54E83A;")
		sql("ALTER TABLE model_sync ADD CONSTRAINT FK_MANUFACTURER_SYNC_ID FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturer_sync` (`manufacturer_id`);")
	}
}
