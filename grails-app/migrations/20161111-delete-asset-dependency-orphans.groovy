/**
 * TM-5567 Correct orphaned asset dependency references to previously deleted assets
 * Created by octavio on 11/11/16.
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161111 TM-5567-1.1") {
		comment('Add FKs to ')
		preConditions(onFail: 'MARK_RAN', onFailMessage: 'Sorry, the foreign key fk_projectDailyMetric_project already exist in the database') {
			not {
				foreignKeyConstraintExists(foreignKeyName: 'fk_asset_dependency_asset_id_to_asset_entity_id ')
			}
		}
		sql("""DELETE FROM asset_dependency
				WHERE asset_id NOT IN (SELECT ae.asset_entity_id FROM asset_entity ae)""")
		sql("""ALTER TABLE `asset_dependency`
					 ADD CONSTRAINT `fk_asset_dependency_asset_id_to_asset_entity_id` FOREIGN KEY (`asset_id`)
						REFERENCES `asset_entity` (`asset_entity_id`)
						ON DELETE CASCADE
						ON UPDATE CASCADE""")
	}

	changeSet(author: "oluna", id: "20161111 TM-5567-1.2") {
		comment('Add FKs to asset_dependency.dependent_id amd ')
		preConditions(onFail: 'MARK_RAN', onFailMessage: 'Sorry, the foreign key fk_projectDailyMetric_project already exist in the database') {
			not {
				foreignKeyConstraintExists(foreignKeyName: 'fk_asset_dependency_dependent_id_to_asset_entity_id ')
			}
		}
		sql("""DELETE FROM asset_dependency
				WHERE dependent_id NOT IN (SELECT ae.asset_entity_id FROM asset_entity ae)""")
		sql("""ALTER TABLE `asset_dependency`
					 ADD CONSTRAINT `fk_asset_dependency_dependent_id_to_asset_entity_id` FOREIGN KEY (`dependent_id`)
						REFERENCES `asset_entity` (`asset_entity_id`)
						ON DELETE CASCADE
						ON UPDATE CASCADE""")
	}
}
