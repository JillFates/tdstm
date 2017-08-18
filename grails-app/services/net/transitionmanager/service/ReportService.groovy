/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Report
import org.codehaus.groovy.grails.web.json.JSONObject
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.DomainUpdateException


/**
 * Service class with main database operations for Report.
 * @see Report
 */
class ReportService implements ServiceMethods {

	SecurityService securityService

	/**
	 * Query for getting all projects where: belong to current project and either shared, system or are owned by
	 * current person in session.
	 * @param project Filter reports by this project.
	 * @param person Filter by owned reports.
	 * @return
	 */
	List<Report> list(Project project, Person person) {
		def query = Report.where {
			project == project && (isSystem == true || isShared == true || person == person)
		}
		return query.list()
	}

	/**
	 * Get a report by specific id.
	 * @param id
	 * @return Report object.
	 */
	Report fetch(Integer id) {
		return Report.get(id)
	}

	/**
	 * Updates a database report object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param report original report object.
	 * @param json JSONObject to take changes from.
	 */
	def update(Report report, JSONObject json) {
		report.reportSchema = json.schema
		report.isShared = json.isShared
		if(!report.save()) {
			report.errors.each {
				log.error(it)
			}
		}
	}

	/**
	 * Creates a Dataview object
	 * Report person and project are taken from current session.
	 * @param json JSONObject to take changes from.
	 * @return the Dataview object that was created
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	def create(JSONObject json) {
		validateReportCreateAccess(json)

		Report dataview = new Report()
		dataview.with {
			person = securityService.loadCurrentPerson()
			project = securityService.userCurrentProject
			name = json.name
			isSystem = json.isSystem
			isShared = json.isShared
			reportSchema = json.schema
		}

		if (!dataview.save()) {
			throw new DomainUpdateException(dataview)
		}

		return dataview
	}

	/**
	 * Validates if person accessing report is authorized to access it.
	 * - should belong to current project in session
	 * - should be either system or shared or current person in session owned
	 * @param report
	 * @return
	 */
	boolean validateReportViewAccess(Report report) {
		return report.project.id == securityService.userCurrentProject.id && (report.isSystem || report.isShared || report.person.id == securityService.currentPersonId)
	}

	/**
	 * Validates if person updating a report has permission to do it.
	 * @param report
	 * @return
	 */
	boolean validateReportUpdateAccess(Report report, JSONObject reportJSON) {
		boolean valid = report.project.id == securityService.userCurrentProject.id
		// system report validation
		if (valid && report.isSystem) {
			valid = securityService.hasPermission(Permission.AssetExplorerSystemEdit)
		} else if (valid && report.person.id == securityService.currentPersonId) { // owned report validation
			valid = securityService.hasPermission(Permission.AssetExplorerEdit)
		}
		// TODO: should we prevent editing other user reports ??

		return valid
	}

	/**
	 * Used to validate if the Dataview JSON request has all of the required properties
	 * @param json - the JSON object to inspect
	 * @throws InvalidRequestException with what property is missing or if no object present
	 */
	void validateDataviewJson(JSONObject json) {
		// TODO - flush out all of the required properties
		List<String> props = ['isSystem', 'name']
		if (json) {
			for (String prop in props) {
				if (! json.containsKey(prop)) {
					throw new InvalidRequestException("JSON object missing property $prop")
				}
			}
		} else {
			throw new InvalidRequestException('Dataview JSON object was missing from request')
		}
	}

	/**
	 * Validates if the person creating a report has permission to create a Dataview
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @throws UnauthorizedException, InvalidRequestException
	 */
	void validateReportCreateAccess(JSONObject dataviewJson) {
		validateDataviewJson(dataviewJson)

		Permission requiredPerm = dataviewJson.isSystem ? Permission.AssetExplorerSystemCreate : Permission.AssetExplorerCreate
		if (! securityService.hasPermission(requiredPerm)) {
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Validates if person deleting a report has permission to do it.
	 * @param report
	 * @return
	 */
	boolean validateReportDeleteAccess(Report report) {
		boolean valid = report.project.id == securityService.userCurrentProject.id
		// system report validation
		if (valid && report.isSystem) {
			valid = securityService.hasPermission(Permission.AssetExplorerSystemDelete)
		} else if (valid) { // owned report validation
			valid = securityService.hasPermission(Permission.AssetExplorerDelete)
		}

		return valid
	}
}