package net.transitionmanager

import Project

class ProjectDailyMetric {

	Long id
	Project project
	Date metricDate
	Long planningServers = 0
	Long planningApplications = 0
	Long planningDatabases = 0
	Long planningPhysicalStorages = 0
	Long planningLogicalStorages = 0
	Long planningNetworkDevices = 0
	Long planningOtherDevices = 0
	Long nonPlanningServers = 0
	Long nonPlanningApplications = 0
	Long nonPlanningDatabases = 0
	Long nonPlanningPhysicalStorages = 0
	Long nonPlanningLogicalStorages = 0
	Long nonPlanningNetworkDevices = 0
	Long nonPlanningOtherDevices = 0
	Long dependencyMappings = 0
	Long tasksAll = 0
	Long tasksDone = 0
	Long totalPersons = 0
	Long totalUserLogins = 0
	Long activeUserLogins = 0
	Date dateCreated
	
    static constraints = {
		project (nullable:false)
		metricDate (nullable:false)
    }

	static mapping  = {
		table "project_daily_metric"
		version false
		id column:'id'
		columns {
			planningServers column:'planning_servers'
			planningApplications column:'planning_applications'
			planningDatabases column:'planning_databases'
			planningPhysicalStorages column:'planning_physical_storages'
			planningLogicalStorages column:'planning_logical_storages'
			planningNetworkDevices column:'planning_network_devices'
			planningOtherDevices column:'planning_other_devices'
			nonPlanningServers column:'non_planning_servers'
			nonPlanningApplications column:'non_planning_applications'
			nonPlanningDatabases column:'non_planning_databases'
			nonPlanningPhysicalStorages column:'non_planning_physical_storages'
			nonPlanningLogicalStorages column:'non_planning_logical_storages'
			nonPlanningNetworkDevices column:'non_planning_network_devices'
			nonPlanningOtherDevices column:'non_planning_other_devices'
			dependencyMappings column:'dependency_mappings'
			tasksAll column:'tasks_all'
			tasksDone column:'tasks_done'
			totalPersons column:'total_persons'
			totalUserLogins column:'total_user_logins'
			activeUserLogins column:'active_user_logins'
		}
	}

}

