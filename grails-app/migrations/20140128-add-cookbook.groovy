
/**
 * This Changelog will add the recipe, recipe_version and task_batch
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140128 TM-2416-1") {
		comment('Create "recipe" table in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'recipe')
			}
		}
		//Creating the "recipe" table
		sql("""
				CREATE TABLE `tdstm`.`recipe` (
				  `recipe_id` BIGINT NOT NULL ,
				  `name` VARCHAR(40) NOT NULL ,
				  `description` VARCHAR(255) NULL ,
				  `context` VARCHAR(45) NOT NULL ,
				  `project_id` BIGINT NOT NULL ,
				  `released_version_id` BIGINT NULL ,
				  `created_by_id` BIGINT NOT NULL ,
				  `date_created` TIMESTAMP NULL ,
				  `last_updated` TIMESTAMP NULL ,
				  `archived` BIT NOT NULL DEFAULT 0,
				  PRIMARY KEY (`recipe_id`) ,
				  INDEX `FK_PROJECT_RECIPE_idx` (`project_id` ASC) ,
				  INDEX `FK_CREATED_BY_RECIPE_idx` (`created_by_id` ASC) ,
				  CONSTRAINT `FK_PROJECT_RECIPE`
				    FOREIGN KEY (`project_id` )
				    REFERENCES `tdstm`.`project` (`project_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_CREATED_BY_RECIPE`
				    FOREIGN KEY (`created_by_id` )
				    REFERENCES `tdstm`.`person` (`person_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION)
			""")
	}


	changeSet(author: "eluna", id: "20140128 TM-2416-2") {
		comment('Create "recipe_version" table in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				
				tableExists(schemaName:'tdstm', tableName:'recipe_version')
			}
		}
		//Creating the "recipe_version" table
		sql("""
				CREATE TABLE `tdstm`.`recipe_version` (
				  `recipe_version_id` BIGINT NOT NULL ,
				  `source_code` TEXT NULL ,
				  `changelog` TEXT NULL ,
				  `cloned_from_id` BIGINT NULL ,
				  `version_number` SMALLINT NOT NULL ,
				  `created_by_id` BIGINT NOT NULL ,
				  `date_created` TIMESTAMP NULL ,
				  `last_updated` TIMESTAMP NULL ,
				  `recipe_id` BIGINT NOT NULL ,
				  PRIMARY KEY (`recipe_version_id`) ,
				  INDEX `FK_CLONED_FROM_ID_idx` (`created_by_id` ASC) ,
				  INDEX `FK_RECIPE_RECIPE_VERSION_idx` (`recipe_id` ASC) ,
				  CONSTRAINT `FK_CREATED_BY_RECIPE_VERSION`
				    FOREIGN KEY (`created_by_id` )
				    REFERENCES `tdstm`.`person` (`person_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_RECIPE_RECIPE_VERSION`
				    FOREIGN KEY (`recipe_id` )
				    REFERENCES `tdstm`.`recipe` (`recipe_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION)
			""")
	}
	
	
	changeSet(author: "eluna", id: "20140128 TM-2416-3") {
		comment('Add missing FKs between recipe and recipe_version')
		
		preConditions(onFail:'MARK_RAN') { //TODO
			not {
				foreignKeyConstraintExists(schemaName:'tdstm', foreignKeyName:'FK_RELEASED_VERSION_RECIPE')
			}
		}
		//Adding missing FKs
		sql("""
				ALTER TABLE `tdstm`.`recipe` 
				  ADD CONSTRAINT `FK_RELEASED_VERSION_RECIPE`
				  FOREIGN KEY (`released_version_id` )
				  REFERENCES `tdstm`.`recipe_version` (`recipe_version_id` )
				  ON DELETE NO ACTION
				  ON UPDATE NO ACTION
				, ADD INDEX `FK_RELEASED_VERSION_RECIPE_idx` (`released_version_id` ASC)
			""")
	}
	
	changeSet(author: "eluna", id: "20140128 TM-2416-4") {
		comment('Add missing FKs between recipe_version and itself')
		
		preConditions(onFail:'MARK_RAN') { //TODO
			not {
				foreignKeyConstraintExists(schemaName:'tdstm', foreignKeyName:'FK_CLONED_FROM_RECIPE_VERSION')
			}
		}
		//Adding missing FKs
		sql("""
				ALTER TABLE `tdstm`.`recipe_version` 
				  ADD CONSTRAINT `FK_CLONED_FROM_RECIPE_VERSION`
				  FOREIGN KEY (`cloned_from_id` )
				  REFERENCES `tdstm`.`recipe_version` (`recipe_version_id` )
				  ON DELETE NO ACTION
				  ON UPDATE NO ACTION
				, ADD INDEX `FK_CLONED_FROM_RECIPE_VERSION_idx` (`cloned_from_id` ASC) ;
			""")
	}
		
	changeSet(author: "eluna", id: "20140128 TM-2416-5") {
		comment('Create "task_batch" table in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') { 
			not {
				tableExists(schemaName:'tdstm', tableName:'task_batch')
			}
		}
		//CREATING TABLE task_batch
		sql("""
				CREATE  TABLE `tdstm`.`task_batch` (
				  `task_batch_id` BIGINT NOT NULL ,
				  `move_event_id` BIGINT NULL ,
				  `move_bundle_id` BIGINT NULL ,
				  `asset_entity_id` BIGINT NULL ,
				  `recipe_version_used_id` BIGINT NULL ,
				  `task_count` SMALLINT NOT NULL ,
				  `exception_count` INT NULL ,
				  `is_published` BIT NULL ,
				  `exception_log` TEXT NULL ,
				  `info_log` TEXT NULL ,
				  `created_by_id` BIGINT NOT NULL ,
				  `date_created` TIMESTAMP NULL ,
				  `last_updated` TIMESTAMP NULL ,
				  PRIMARY KEY (`task_batch_id`) ,
				  INDEX `FK_MOVE_EVENT_TASK_BATCH_idx` (`move_event_id` ASC) ,
				  INDEX `FK_MOVE_BUNDLE_TASK_BATCH_idx` (`move_bundle_id` ASC) ,
				  INDEX `FK_MOVE_EVENT_TASK_BATCH_idx1` (`asset_entity_id` ASC) ,
				  INDEX `assetEntity_idx` (`recipe_version_used_id` ASC) ,
				  INDEX `FK_CREATED_BY_TASK_BATCH_idx` (`created_by_id` ASC) ,
				  CONSTRAINT `FK_MOVE_EVENT_TASK_BATCH`
				    FOREIGN KEY (`move_event_id` )
				    REFERENCES `tdstm`.`move_event` (`move_event_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_MOVE_BUNDLE_TASK_BATCH`
				    FOREIGN KEY (`move_bundle_id` )
				    REFERENCES `tdstm`.`move_bundle` (`move_bundle_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_ASSET_ENTITY_TASK_BATCH`
				    FOREIGN KEY (`asset_entity_id` )
				    REFERENCES `tdstm`.`asset_entity` (`asset_entity_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_RECIPE_VERSION_TASK_BATCH`
				    FOREIGN KEY (`recipe_version_used_id` )
				    REFERENCES `tdstm`.`recipe_version` (`recipe_version_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION,
				  CONSTRAINT `FK_CREATED_BY_TASK_BATCH`
				    FOREIGN KEY (`created_by_id` )
				    REFERENCES `tdstm`.`person` (`person_id` )
				    ON DELETE NO ACTION
				    ON UPDATE NO ACTION)
		""")
	}
	
	changeSet(author: "eluna", id: "20140128 TM-2416-6") {
		comment('Add is_published and task_batch to asset_comment')
		
		preConditions(onFail:'MARK_RAN') { //TODO
			not {
				foreignKeyConstraintExists(schemaName:'tdstm', foreignKeyName:'FK_TASK_BATCH_ASSET_COMMENT')
			}
		}
		//Adding missing FKs
		sql("""
				ALTER TABLE `tdstm`.`asset_comment` ADD COLUMN `task_batch_id` BIGINT NULL  AFTER `constraint_type` , ADD COLUMN `is_published` BIT NULL  AFTER `task_batch_id` , 
				  ADD CONSTRAINT `FK_TASK_BATCH_ASSET_COMMENT`
				  FOREIGN KEY (`task_batch_id` )
				  REFERENCES `tdstm`.`task_batch` (`task_batch_id` )
				  ON DELETE NO ACTION
				  ON UPDATE NO ACTION
				, ADD INDEX `FK_TASK_BATCH_ASSET_COMMENT_idx` (`task_batch_id` ASC) ;
			""")
	}
}
