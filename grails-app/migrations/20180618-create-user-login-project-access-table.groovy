/**
 * @author ecantu
 * Create user_login_project_access table.
 * See TM-10119 - Project Daily Metrics does not properly collect the active users
 */

databaseChangeLog = {

    changeSet(author: "ecantu", id: "20180618 TM-10119-1") {
        comment("Create table for UserLoginProjectAccess")

        sql("""
			  CREATE TABLE IF NOT EXISTS `user_login_project_access` (
				  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
				  `user_login_id` BIGINT(20) NOT NULL,
				  `project_id` BIGINT(20) NOT NULL,
				  `date` datetime NOT NULL,
				  PRIMARY KEY (id),
				  FOREIGN KEY FK_USERLOGINPROJECTACCESS_PROJECT (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
				  FOREIGN KEY FK_USERLOGINPROJECTACCESS_USER_LOGIN (user_login_id) REFERENCES user_login(user_login_id) ON DELETE CASCADE,
				  UNIQUE IX_USERLOGINPROJECTACCESS_PROJECT_DATE (`user_login_id`, `project_id`, `date`)
			  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
		""")
    }
}
