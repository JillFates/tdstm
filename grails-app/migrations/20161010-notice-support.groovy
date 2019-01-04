/**
 * This migration will add "notice" & "notice_acknowledgment" table.
 * @author @tavo_luna
 */

databaseChangeLog = {
	changeSet(author: "oluna", id: "20161010 TM-5397_NOTICE") {
		comment('Create "notice" table in "tdstm" schema')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'notice')
			}
		}

		sql("""
			  CREATE TABLE `notice` (
				  `notice_id` bigint(20) NOT NULL AUTO_INCREMENT,
				  `acknowledgeable` bit(1) NOT NULL,
				  `activation_date` datetime DEFAULT NULL,
				  `active` bit(1) NOT NULL,
				  `created_by_id` bigint(20) NOT NULL,
				  `date_created` datetime NOT NULL,
				  `expiration_date` datetime DEFAULT NULL,
				  `html_text` text NOT NULL,
				  `last_modified` datetime NOT NULL,
				  `project_id` bigint(20) DEFAULT NULL,
				  `raw_text` text NOT NULL,
				  `title` varchar(255) NOT NULL,
				  `type` varchar(255) NOT NULL,
				  PRIMARY KEY (`notice_id`),
				  KEY `FK_NOTICE_CREATED_BY_ID` (`created_by_id`),
				  KEY `FK_NOTICE_PROJECT_ID` (`project_id`),
				  CONSTRAINT `FK_CREATED_BY_ID` FOREIGN KEY (`created_by_id`) REFERENCES `person` (`person_id`),
				  CONSTRAINT `FK_NOTICE_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`)
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")
	}

	changeSet(author: "oluna", id: "20161010 TM-5397_NOTICE_ACKNOWLEDGMENT") {
		comment('Create "notice_acknowledgment" table in "tdstm" schema')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'notice_acknowledgment')
			}
		}

		sql("""
			  CREATE TABLE `notice_acknowledgment` (
				  `id` bigint(20) NOT NULL AUTO_INCREMENT,
				  `version` bigint(20) NOT NULL,
				  `date_created` datetime NOT NULL,
				  `notice_id` bigint(20) NOT NULL,
				  `person_id` bigint(20) NOT NULL,
				  PRIMARY KEY (`id`),
				  KEY `FK_NOTICE_ACK_NOTICE_ID` (`notice_id`),
				  KEY `FK_NOTICE_ACK_PERSON_ID` (`person_id`),
				  CONSTRAINT `FK_NOTICE_ACK_NOTICE_ID` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`),
				  CONSTRAINT `FK_NOTICE_ACK_PERSON_ID` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`)
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")
	}
}
