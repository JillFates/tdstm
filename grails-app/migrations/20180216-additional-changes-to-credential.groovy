import net.transitionmanager.security.Permission

/**
 * TM-9406 Additional changes to the Credential domain
 */
databaseChangeLog = {
	changeSet(author: "slopez", id: "TM-9406-01") {
		comment('Add several columns and change null/not null appropriately ')
		sql ("""
                -- Cleanup data to be able to be NOT NULL
                UPDATE `credential` SET `renew_token_url` = '' WHERE `renew_token_url` IS NULL;
                UPDATE `credential` SET `authentication_url` = '' WHERE `authentication_url` IS NULL;
                UPDATE `credential` SET `username` = '' WHERE `username` IS NULL;
                UPDATE `credential` SET `password` = '' WHERE `password` IS NULL;
                UPDATE `credential` SET `salt` = '' WHERE `salt` IS NULL;
                UPDATE `credential` SET `salt` = LEFT(`salt`, 16) WHERE `salt` IS NULL;

                -- Change for Enum value that was renamed
				UPDATE `credential` SET `environment` = 'DEVELOPMENT' WHERE `environment` = 'DEV';

                ALTER TABLE `credential` 
                    CHANGE `http_method` `http_method` VARCHAR(16) NOT NULL AFTER `authentication_method`,
                    ADD `request_mode` VARCHAR(16) NOT NULL AFTER `http_method`,
                    ADD `description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `name`,
                    ADD `terminate_url` VARCHAR(255) NOT NULL DEFAULT '' AFTER `authentication_url`,
                    CHANGE `username` `username` VARCHAR(255) NOT NULL,
                    CHANGE `password` `password` VARCHAR(255) NOT NULL,
                    CHANGE `status` `status` VARCHAR(255) NOT NULL,
                    CHANGE `salt` `salt` VARCHAR(16) NOT NULL AFTER `password`,
                    CHANGE `renew_token_url` `renew_token_url` VARCHAR(255) NOT NULL DEFAULT '',
                    CHANGE `authentication_url` `authentication_url` VARCHAR(255) NOT NULL DEFAULT '',
                    CHANGE `project_id` `project_id` BIGINT(20) NOT NULL AFTER `credential_id`,
                    CHANGE `provider_id` `provider_id` BIGINT(20) NOT NULL AFTER `project_id`,
                    DROP COLUMN `expiration_date`
                ;
				
		""")
	}
}
