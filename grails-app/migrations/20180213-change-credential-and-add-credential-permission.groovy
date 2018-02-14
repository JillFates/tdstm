import net.transitionmanager.security.Permission

/**
 * TM-8601 Improve the CredentialService and add Endpoints to be used with Actions
 */
databaseChangeLog = {
	changeSet(author: "slopez", id: "TM-8601") {
		comment('Create new permission for credential access')
		grailsChange {
			change {
				Map perms = [
						(Permission.CredentialView): [
								group: 'NONE',
								description: 'Can view Credential information',
								roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
						],
						(Permission.CredentialCreate): [
								group: 'NONE',
								description: 'Can create Credential information',
								roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
						],
						(Permission.CredentialEdit): [
								group: 'NONE',
								description: 'Can edit Credential information',
								roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
						],
						(Permission.CredentialDelete): [
								group: 'NONE',
								description: 'Can delete Credential information',
								roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
						]
				]

				ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
			}
		}


		comment('Alter credential entity to have new column and renamed columns')
		sql ("""
				ALTER TABLE `credential` CHANGE `type` `environment` VARCHAR(32) NOT NULL;
				ALTER TABLE `credential` CHANGE `method` `authentication_method` VARCHAR(32) NOT NULL;
				ALTER TABLE `credential` CHANGE `access_key` `username` VARCHAR(255);
				ALTER TABLE `credential` ADD `http_method` VARCHAR(6);
				
				UPDATE `credential` SET `http_method` = 'POST';
				ALTER TABLE `credential` CHANGE `http_method` `http_method` VARCHAR(6) NOT NULL;
				ALTER TABLE `credential` MODIFY COLUMN `http_method` VARCHAR(6) NOT NULL;
				
				UPDATE `credential` SET `authentication_method` = 'BASIC_AUTH' WHERE `authentication_method` = 'HTTP_BASIC';
				UPDATE `credential` SET `authentication_method` = 'COOKIE' WHERE `authentication_method` = 'HTTP_COOKIE';
				UPDATE `credential` SET `authentication_method` = 'JWT' WHERE `authentication_method` = 'JWT_TOKEN';
		""")
	}
}
