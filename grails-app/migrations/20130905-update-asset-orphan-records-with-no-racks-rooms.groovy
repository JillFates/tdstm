/**
 * This change set is used to update assets sourceRacks,targetRacks,sourceRooms,targetRooms not having any rackId or roomId.
 * Add FK to room and rack references 
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130905 TM-2250-1") {
        comment('change set is used to update assets sourceRacks,targetRacks,sourceRooms,targetRooms not having any rackId or roomId')
		sql("update asset_entity set rack_source_id = null, source_rack = null where rack_source_id not in (select rack_id from rack)")
		sql("update asset_entity set rack_target_id = null, target_rack = null where rack_target_id not in (select rack_id from rack)")
		sql("update asset_entity set room_source_id = null, source_room = null where room_source_id not in (select room_id from room)")
		sql("update asset_entity set room_target_id = null, target_room = null where room_target_id not in (select room_id from room)")
		
		sql("update asset_entity set source_rack = null where rack_source_id is null")
		sql("update asset_entity set target_rack = null where rack_target_id is null")
		sql("update asset_entity set source_room = null where room_source_id is null")
		sql("update asset_entity set target_room = null where room_target_id is null")
	}
    changeSet(author: "lokanada", id: "20130905 TM-2250-2") {
        comment('Add FK to rack_source_id')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:"0", """SELECT count(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
                                               CONSTRAINT_SCHEMA = 'tdstm' AND
                                               CONSTRAINT_NAME   = 'FK_asset_entity_rack_source_id' AND
                                               CONSTRAINT_TYPE   = 'FOREIGN KEY';""")
        }
        sql("""ALTER TABLE `asset_entity`
                 ADD CONSTRAINT `FK_asset_entity_rack_source_id` FOREIGN KEY `FK_asset_entity_rack_source_id` (`rack_source_id`)
                    REFERENCES `rack` (`rack_id`)
                    ON DELETE NO ACTION
                    ON UPDATE NO ACTION""")
        
    }
    
    changeSet(author: "lokanada", id: "20130905 TM-2250-3") {
        comment('Add FK to rack_target_id')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:"0", """SELECT count(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
                                               CONSTRAINT_SCHEMA = 'tdstm' AND
                                               CONSTRAINT_NAME   = 'FK_asset_entity_rack_target_id' AND
                                               CONSTRAINT_TYPE   = 'FOREIGN KEY';""")
        }
        sql("""ALTER TABLE `asset_entity`
                 ADD CONSTRAINT `FK_asset_entity_rack_target_id` FOREIGN KEY `FK_asset_entity_rack_target_id` (`rack_target_id`)
                    REFERENCES `rack` (`rack_id`)
                    ON DELETE NO ACTION
                    ON UPDATE NO ACTION""")
        
    }
    changeSet(author: "lokanada", id: "20130905 TM-2250-4") {
        comment('Add FK to room_source_id')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:"0", """SELECT count(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
                                               CONSTRAINT_SCHEMA = 'tdstm' AND
                                               CONSTRAINT_NAME   = 'FK_asset_entity_room_source_id' AND
                                               CONSTRAINT_TYPE   = 'FOREIGN KEY';""")
        }
        sql("""ALTER TABLE `asset_entity`
                 ADD CONSTRAINT `FK_asset_entity_room_source_id` FOREIGN KEY `FK_asset_entity_room_source_id` (`room_source_id`)
                    REFERENCES `room` (`room_id`)
                    ON DELETE NO ACTION
                    ON UPDATE NO ACTION""")
        
    }
    
    changeSet(author: "lokanada", id: "20130905 TM-2250-5") {
        comment('Add FK to room_target_id')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:"0", """SELECT count(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
                                               CONSTRAINT_SCHEMA = 'tdstm' AND
                                               CONSTRAINT_NAME   = 'FK_asset_entity_room_target_id' AND
                                               CONSTRAINT_TYPE   = 'FOREIGN KEY';""")
        }
        sql("""ALTER TABLE `asset_entity`
                 ADD CONSTRAINT `FK_asset_entity_room_target_id` FOREIGN KEY `FK_asset_entity_room_target_id` (`room_target_id`)
                    REFERENCES `room` (`room_id`)
                    ON DELETE NO ACTION
                    ON UPDATE NO ACTION""")
        
    }
}
