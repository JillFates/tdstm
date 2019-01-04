/**
 * Create the tables for Credential table.
 */

databaseChangeLog = {

    changeSet(author: "slopez", id: "20171011 TM-7331") {
        comment("Create table for Credential")


        sql("""
			  CREATE TABLE IF NOT EXISTS `credential` (
				  `credential_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
				  `name` varchar(255) NOT NULL,
				  `type` varchar(32),
				  `status` varchar(32),
				  `method` varchar(32),
				  `salt` varchar(255),
				  `access_key` varchar(255),
				  `password` varchar(255),
				  `authentication_url` varchar(255),
				  `renew_token_url` varchar(255),
				  `project_id` BIGINT(20) NOT NULL,
				  `provider_id` BIGINT(20) NOT NULL,
				  `date_created` datetime NOT NULL,
				  `last_updated` datetime,
				  `expiration_date` datetime,
				  `version` int,
				  PRIMARY KEY (credential_id),
				  FOREIGN KEY FK_CREDENTIAL_PROJECT (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
				  FOREIGN KEY FK_CREDENTIAL_PROVIDER (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE,
				  UNIQUE IX_CREDENTIAL_PROJECT (`name`,`project_id`)
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")

    }
}
