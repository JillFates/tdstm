package net.transitionmanager

import net.transitionmanager.domain.Project
import net.transitionmanager.service.LicenseAdminService
import org.apache.commons.codec.digest.DigestUtils

class ProjectDailyMetric {
	private static SEAL_SALT = "kx23lafKJqzfa123"

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
	Boolean licenseCompliant = true
	String  seal

	static mapping = {
		version false
		autowire true
	}

	static constraints = {
		seal nullable: false, blank:false
	}

	//Services
	LicenseAdminService licenseAdminService
	static transients=['licenseAdminService']

	String computeSeal(){
		String cat = "" +
				project?.id +
				metricDate +
				dateCreated +
				planningServers +
				planningApplications +
				planningDatabases +
				planningPhysicalStorages +
				planningLogicalStorages +
				planningNetworkDevices +
				planningOtherDevices +
				dependencyMappings +
				tasksAll +
				totalPersons +
				activeUserLogins +
				SEAL_SALT

		return DigestUtils.md5Hex(cat)
	}

	boolean validSeal(){
		return computeSeal() == seal
	}

	/**
	 * WARNING! READ this before changing it for something else.
	 * For a STUPID reason grails don't want to call beforeInsert in the ProjectService:activitySnapshot
	 * 	if (!metric.save(flush:true))
	 *
	 * it's calling beforeValidate and failing because the seal is null, so I'll add the seal computing logic here.
	 * this is called before any insert and update, so I'll only compute the seal if the "id" is null
	 *
	 * If some future programmer get's to know why this happened I'll be glad to know @tavo_luna
	 * @return
	 */
	def beforeValidate(){
		if(!id && !seal){
			seal = computeSeal()
			licenseCompliant = licenseAdminService.isLicenseCompliant(project)
		}
	}
}
