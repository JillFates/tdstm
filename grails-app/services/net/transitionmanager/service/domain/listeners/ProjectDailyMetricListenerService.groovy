package net.transitionmanager.service.domain.listeners

import grails.events.annotation.gorm.Listener
import grails.events.bus.EventBusAware
import net.transitionmanager.ProjectDailyMetric
import net.transitionmanager.service.LicenseAdminService
import org.apache.commons.codec.digest.DigestUtils
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

class ProjectDailyMetricListenerService implements EventBusAware {
	LicenseAdminService licenseAdminService

	@Listener(ProjectDailyMetric)
	void onLicensedClientPreUpdate(PreUpdateEvent event) {
		beforeValidate(event.entity)
	}

	@Listener(ProjectDailyMetric)
	void onLicensedClientPreInsert(PreInsertEvent event) {
		beforeValidate(event.entity)

	}

	void beforeValidate(ProjectDailyMetric projectDailyMetric) {
		if (!projectDailyMetric.id && !projectDailyMetric.seal) {
			projectDailyMetric.seal = computeSeal(projectDailyMetric)
			projectDailyMetric.licenseCompliant = licenseAdminService.isLicenseCompliant(projectDailyMetric.project)
		}
	}

	String computeSeal(ProjectDailyMetric projectDailyMetric) {
		String cat = "" +
					 projectDailyMetric.project?.id +
					 projectDailyMetric.metricDate +
					 projectDailyMetric.dateCreated +
					 projectDailyMetric.planningServers +
					 projectDailyMetric.planningApplications +
					 projectDailyMetric.planningDatabases +
					 projectDailyMetric.planningPhysicalStorages +
					 projectDailyMetric.planningLogicalStorages +
					 projectDailyMetric.planningNetworkDevices +
					 projectDailyMetric.planningOtherDevices +
					 projectDailyMetric.dependencyMappings +
					 projectDailyMetric.tasksAll +
					 projectDailyMetric.totalPersons +
					 projectDailyMetric.activeUserLogins +
					 projectDailyMetric.SEAL_SALT

		return DigestUtils.md5Hex(cat)
	}

}
