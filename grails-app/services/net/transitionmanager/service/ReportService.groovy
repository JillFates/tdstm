/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Report
import net.transitionmanager.security.Permission
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Service class with main database operations for Report.
 * @see Report
 */
class ReportService implements ServiceMethods {

	SecurityService securityService

	/**
	 * Query for getting all projects where: belong to current project and either shared, system or are owned by
	 * current person in session.
	 * @return
	 */
	List<Report> list() {

		Person currentPerson = securityService.loadCurrentPerson()
		Project currentProject = securityService.userCurrentProject

		def query = Report.where {
			project == currentProject && (isSystem == true || isShared == true || person == currentPerson)
		}

		List<Report> dataviewList = query.list()

		return dataviewList*.toMap( currentPerson.id )
	}

	/**
	 * Gets a Dataview by id.
	 * @param id
	 * @return
	 */
	def fetch(Integer id) {

		Report dataview = Report.get(id)
		validateDataviewViewAccess(dataview);

		return dataview.toMap(securityService.loadCurrentPerson().id)
	}

	/**
	 * Updates a database dataview object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param dataviewJson JSONObject to take changes from.
	 * @return the Dataview object that was updated
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	def update(Integer id, JSONObject dataviewJson) {

		Report dataview = Report.get(id)
		validateDataviewUpdateAccess(dataview, dataviewJson)

		dataview.with {
			reportSchema = dataviewJson.schema
			isShared = dataviewJson.isShared
		}

		if (!dataview.save()) {
			throw new DomainUpdateException('Error on Update', dataview)
		}

		return dataview.toMap(securityService.loadCurrentPerson().id);
	}

	/**
	 * Creates a Dataview object
	 * Dataview person and project are taken from current session.
	 * @param json JSONObject to take changes from.
	 * @return the Dataview object that was created
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	def create(JSONObject dataviewJson) {

		validateDataviewCreateAccess(dataviewJson)

		Report dataview = new Report()
		dataview.with {
			person = securityService.loadCurrentPerson()
			project = securityService.userCurrentProject
			name = dataviewJson.name
			isSystem = dataviewJson.isSystem
			isShared = dataviewJson.isShared
			reportSchema = dataviewJson.schema
		}

		if (!dataview.save()) {
			throw new DomainUpdateException('Error on create', dataview)
		}

		return dataview.toMap(securityService.loadCurrentPerson().id)
	}

	/**
	 * Validates if person accessing dataview is authorized to access it.
	 * - should belong to current project in session
	 * - should be either system or shared or current person in session owned
	 * @param dataview
	 * @return
	 */
	boolean validateDataviewViewAccess(Report dataview) {
		if (!dataview) {
			throw new InvalidRequestException("Dataview JSON object not found $dataview.id")
		}
		boolean canView = dataview.project.id == securityService.userCurrentProject.id && (dataview.isSystem || dataview.isShared || dataview.person.id == securityService.currentPersonId);
		if (!canView) {
			throw new UnauthorizedException("Unauthorized access to $dataview.id")
		}
	}

	/**
	 * Validates if the person updating a dataview has permission to it.
	 * @param dataview - original object from database
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @throws UnauthorizedException, InvalidRequestException
	 */
	void validateDataviewUpdateAccess(Report dataview, JSONObject dataviewJson) {
		if (!dataview) {
			throw new InvalidRequestException("Dataview JSON object not found $dataviewJson.id")
		}
		boolean valid = dataview.project.id == securityService.userCurrentProject.id
		// system dataview validation
		if (valid && dataview.isSystem && !securityService.hasPermission(Permission.AssetExplorerSystemEdit)) {
			throw new UnauthorizedException(Permission.AssetExplorerSystemEdit)
		} else if (valid && dataview.person.id == securityService.currentPersonId && !securityService.hasPermission(Permission.AssetExplorerEdit)) {
			throw new UnauthorizedException(Permission.AssetExplorerEdit)
		}
		// TODO: should we prevent editing other user reports ??
	}

	/**
	 * Used to validate if the Dataview JSON request has all of the required properties
	 * @param dataviewJson - the JSON object to inspect
	 * @throws InvalidRequestException with what property is missing or if no object present
	 */
	void validateDataviewJson(JSONObject dataviewJson) {
		// TODO - flush out all of the required properties
		List<String> props = ['isSystem', 'name']
		if (dataviewJson) {
			for (String prop in props) {
				if (! dataviewJson.containsKey(prop)) {
					throw new InvalidRequestException("JSON object missing property $prop")
				}
			}
		} else {
			throw new InvalidRequestException('Dataview JSON object was missing from request')
		}
	}

	/**
	 * Validates if the person creating a dataview has permission to create a Dataview
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @throws UnauthorizedException, InvalidRequestException
	 */
	void validateDataviewCreateAccess(JSONObject dataviewJson) {
		validateDataviewJson(dataviewJson)

		String requiredPerm = dataviewJson.isSystem ? Permission.AssetExplorerSystemCreate : Permission.AssetExplorerCreate
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