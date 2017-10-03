/**
 * Create the tables for Datasource and Provider.
 */

databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171002 TM-7221-1") {
        comment("Create the table for Provider")


        sql("""
			  CREATE TABLE IF NOT EXISTS `provider` (
				  `provider_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
				  `name` varchar(255) NOT NULL,
				  `description` varchar(255),
				  `comment` TEXT,
				  `project_id` BIGINT(20) NOT NULL,
				  `date_created` datetime NOT NULL,
				  `last_updated` datetime,
				  PRIMARY KEY (provider_id),
				  FOREIGN KEY FK_PROVIDER_PROJECT (project_id) REFERENCES project(project_id) ON DELETE CASCADE
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")
    }

    changeSet(author: "arecordon", id: "20171002 TM-7221-2") {
        comment("Create the table for Datasource")


        sql("""
			  CREATE TABLE IF NOT EXISTS `datasource` (
				  `datasource_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
				  `name` varchar(255) NOT NULL,
				  `description` varchar(255),
				  `target` varchar(255),
				  `provider_id` BIGINT(20) NOT NULL,
				  `project_id` BIGINT(20) NOT NULL,
				  `created_by` BIGINT(20) NOT NULL,
				  `last_modified_by` BIGINT(20),
				  `etl_source_code` MEDIUMTEXT,
				  `mode` varchar(255) NOT NULL,
				  `date_created` datetime NOT NULL,
				  `last_updated` datetime,
				  PRIMARY KEY (datasource_id),
				  FOREIGN KEY FK_DATASOURCE_PROJECT (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
				  FOREIGN KEY FK_DATASOURCE_PROVIDER (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE,
				  FOREIGN KEY FK_DATASOURCE_CREATEDBY (created_by) REFERENCES person(person_id),
				  FOREIGN KEY FK_DATASOURCE_LASTMODIFIEDBY (last_modified_by) REFERENCES person(person_id)
				  
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")
    }
}
