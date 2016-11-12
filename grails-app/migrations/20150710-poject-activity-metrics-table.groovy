databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150719 TM-3965-1") {
		comment('Create project activity metrics table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'project_daily_metric')
			}
		}
		createTable(tableName: "project_daily_metric") {

			column(name: "id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "project_id", type: "BIGINT(20)"){
				constraints(nullable:"false")
			}
			column(name: "metric_date", type: "DATE"){
				constraints(nullable:"false")
			}
			column(name: "planning_servers", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_applications", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_databases", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_physical_storages", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_logical_storages", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_network_devices", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "planning_other_devices", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_servers", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_applications", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_databases", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_physical_storages", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_logical_storages", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_network_devices", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "non_planning_other_devices", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "dependency_mappings", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "tasks_all", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "tasks_done", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "total_persons", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "total_user_logins", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "active_user_logins", type: "MEDIUMINT", defaultValue: 0){
				constraints(nullable:"true")
			}
			column(name: "date_created", type: "DATETIME", defaultValueComputed: "now()"){
				constraints(nullable:"false")
			}
		}	
	}

	changeSet(author: "dscarpa", id: "20150719 TM-3965-2") {
		comment('Add permission ShowProjectDailyMetrics to roles: ADMIN, CLIENT_ADMIN, CLIENT_MGR, SUPERVISOR')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="REPORTS" and permission_item = "ShowProjectDailyMetrics"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('REPORTS', 'ShowProjectDailyMetrics','Ability to view project daily metrics report')")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'REPORTS' and permission_item= 'ShowProjectDailyMetrics'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'REPORTS' and permission_item= 'ShowProjectDailyMetrics'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'REPORTS' and permission_item= 'ShowProjectDailyMetrics'), 'CLIENT_MGR')""")
	}

	changeSet(author: "dscarpa", id: "20150719 TM-3965-3") {
		comment('Add FKs to project activity metrics table')
		preConditions( onFail:'MARK_RAN', onFailMessage: 'Sorry, the foreign key fk_projectDailyMetric_project already exist in the database' ){
			not {
				foreignKeyConstraintExists(schemaName:'tdstm', foreignKeyName: 'fk_projectDailyMetric_project')
			}
		}
        sql("""ALTER TABLE `project_daily_metric`
                 ADD CONSTRAINT `fk_projectDailyMetric_project` FOREIGN KEY (`project_id`)
                    REFERENCES `project` (`project_id`)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE""")
	}

}

