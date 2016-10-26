package net.transitionmanager

import net.transitionmanager.domain.Project

class ProjectDailyMetric {

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

	static mapping = {
		version false
	}
}
