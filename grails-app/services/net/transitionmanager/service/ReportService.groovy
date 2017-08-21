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

		return query.list()
	}

	/**
	 * Gets a Dataview by id.
	 * @param id
	 * @return
	 */
	Report fetch(Integer id) {

		Report dataview = Report.get(id)
		validateDataviewViewAccessOrException(dataview);

		return dataview
	}

	/**
	 * Updates a database dataview object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param dataviewJson JSONObject to take changes from.
	 * @return the Dataview object that was updated
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	Report update(Integer id, JSONObject dataviewJson) {

		Report dataview = Report.get(id)
		validateDataviewViewAccessOrException(dataview)
		validateDataviewUpdateAccessOrException(dataviewJson, dataview)

		dataview.with {
			reportSchema = dataviewJson.schema
			isShared = dataviewJson.isShared
		}

		if (!dataview.save()) {
			throw new DomainUpdateException('Error on Update', dataview)
		}

		return dataview
	}

	/**
	 * Creates a Dataview object
	 * Dataview person and project are taken from current session.
	 * @param json JSONObject to take changes from.
	 * @return the Dataview object that was created
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	Report create(JSONObject dataviewJson) {

		validateDataviewCreateAccessOrException(dataviewJson)

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

		return dataview
	}

	/**
	 * Deletes a Dataview object
	 * Dataview person and project are taken from current session.
	 * @param id Dataview id to delete
	 * @return
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	boolean delete(Integer id) {

		Report dataview = Report.get(id)
		validateDataviewViewAccessOrException(dataview)
		validateDataviewDeleteAccessOrException(dataview)

		dataview.delete()
		return true
	}

	/**
	 * Validates if person accessing dataview is authorized to access it.
	 * - should belong to current project in session
	 * - should be either system or shared or current person in session owned
	 * @param dataview
	 * @throws InvalidRequestException
	 */
	void validateDataviewViewAccessOrException(Report dataview) {
		if (!dataview) {
			throw new InvalidRequestException("Dataview JSON object not found $dataview.id")
		}
		boolean canView = dataview.project.id == securityService.userCurrentProject.id && (dataview.isSystem || dataview.isShared || dataview.person.id == securityService.currentPersonId);
		if (!canView) {
			securityService.reportViolation("Attempted action requiring unallowed access to Dataview $dataview.id", securityService.userLogin.toString())
			throw new InvalidRequestException("Unauthorized access to $dataview.id")
		}
	}

	/**
	 * Validates if the person updating a dataview has permission to it.
	 * @param dataview - original object from database
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @throws UnauthorizedException
	 */
	void validateDataviewUpdateAccessOrException(JSONObject dataviewJson, Report dataview) {
		validateDataviewJson(dataviewJson, ['name', 'schema', 'isShared'])

		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemEdit : Permission.AssetExplorerEdit
		if (! securityService.hasPermission(requiredPerm)) {
			throw new UnauthorizedException(requiredPerm)
		}
		// TODO: should we prevent editing other user reports ??
	}

	/**
	 * Validates if the person creating a dataview has permission to create a Dataview
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @throws UnauthorizedException
	 */
	void validateDataviewCreateAccessOrException(JSONObject dataviewJson) {
		validateDataviewJson(dataviewJson, ['isSystem', 'name', 'schema', 'isShared'])

		String requiredPerm = dataviewJson.isSystem ? Permission.AssetExplorerSystemCreate : Permission.AssetExplorerCreate
		if (! securityService.hasPermission(requiredPerm)) {
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Validates if person deleting a report has permission to do it.
	 * @param dataviewJSON - the JSON object containing information about the Dataview
	 * @throws UnauthorizedException
	 */
	void validateDataviewDeleteAccessOrException(Report dataview) {

		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemDelete : Permission.AssetExplorerDelete
		if (! securityService.hasPermission(requiredPerm)) {
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Used to validate if the Dataview JSON request has all of the required properties
	 * @param dataviewJson - the JSON object to inspect
	 * @throws InvalidRequestException with what property is missing or if no object present
	 */
	void validateDataviewJson(JSONObject dataviewJson, List<String> props) {
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
}