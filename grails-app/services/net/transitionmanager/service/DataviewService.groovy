/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import com.tds.asset.AssetEntity
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.dataview.DataviewSpec

import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Service class with main database operations for Dataview.
 * @see Dataview
 */
class DataviewService implements ServiceMethods {

    SecurityService securityService

    // Properties used in validating the JSON Create and Update functions
    static final List<String> UPDATE_PROPERTIES = ['name', 'schema', 'isShared']
    static final List<String> CREATE_PROPERTIES = UPDATE_PROPERTIES + 'isSystem'

    /**
     * Query for getting all projects where: belong to current project and either shared, system or are owned by
     * current person in session
     * @return
     */
    List<Dataview> list() {
        Person currentPerson = securityService.loadCurrentPerson()
        Project currentProject = securityService.userCurrentProject

        def query = Dataview.where {
            project == currentProject
            (isSystem == true || isShared == true || person == currentPerson)
        }

        return query.list()
    }

    /**
     * Gets a Dataview by id.
     * @param id
     * @return
     */
    Dataview fetch(Integer id) {
        Dataview dataview = Dataview.get(id)
        validateDataviewViewAccessOrException(dataview)

        return dataview
    }

    /**
     * Retrieve the Dataview specification from the database that is persisted as Json and converts to DataviewSpec
     * @param id - the id of the Dataview record
     * @return the DataviewSpec for the given Dataview
     */
    DataviewSpec fetchAsDataviewSpec(Integer id) {
        Dataview dataview = fetch(id)
        DataviewSpec dataviewSpec = new DataviewSpec(dataview)
        return dataviewSpec
    }

    /**
     * Updates a database dataview object.
     * At this point just schema and isShared properties are accessible to be updated.
     * @param dataviewJson JSONObject to take changes from.
     * @return the Dataview object that was updated
     * @throws DomainUpdateException , UnauthorizedException
     */
    Dataview update(Integer id, JSONObject dataviewJson) {
        Dataview dataview = Dataview.get(id)
        validateDataviewUpdateAccessOrException(dataviewJson, dataview)

        dataview.with {
            reportSchema = dataviewJson.schema
            isShared = dataviewJson.isShared
        }

        if (!dataview.save()) {
            throw new DomainUpdateException('Error on update', dataview)
        }

        return dataview
    }

    /**
     * Creates a Dataview object
     * Dataview person and project are taken from current session.
     * @param json JSONObject to take changes from.
     * @return the Dataview object that was created
     * @throws DomainUpdateException , UnauthorizedException
     */
    Dataview create(JSONObject dataviewJson) {
        validateDataviewCreateAccessOrException(dataviewJson)

        Dataview dataview = new Dataview()
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
     * Dataview person and project are taken from current session
     * @param id Dataview id to delete
     * @throws DomainUpdateException , UnauthorizedException
     */
    void delete(Integer id) {
        Dataview dataview = Dataview.get(id)
        validateDataviewDeleteAccessOrException(dataview)

        dataview.delete()
    }

    /**
     * Validates if person accessing dataview is authorized to access it
     * - should belong to current project in session
     * - should be either system or shared or current person in session owned
     * @param dataview
     * @throws InvalidRequestException
     */
    void validateDataviewViewAccessOrException(Dataview dataview) {
        boolean throwNotFound = false
        if (!dataview) {
            throwNotFound = true
        }

        // Validate Dataview belongs to the project
        if (!throwNotFound && dataview.project.id != securityService.userCurrentProject.id) {
            securityService.reportViolation("attempted to access Dataview $dataview.id unrelated to project")
            throwNotFound = true
        }

        if (!throwNotFound) {
            // Make sure the user has proper access to the Dataview
            boolean allowedToView = (dataview.isSystem || dataview.isShared || dataview.person.id == securityService.currentPersonId)
            if (!allowedToView) {
                securityService.reportViolation("attempted to access non-shared Dataview ($dataview.id)")
                throwNotFound = true
            }
        }

        // Throw an exception if any of the above falied
        if (throwNotFound) {
            throw new EmptyResultException("Dataview not found (${dataview.id})")
        }
    }

    /**
     * Validates if the person updating a dataview has permission to it.
     * @param dataviewJSON - the JSON object containing information about the Dataview to create
     * @param dataview - original object from database
     * @throws UnauthorizedException
     */
    void validateDataviewUpdateAccessOrException(JSONObject dataviewJson, Dataview dataview) {
        validateDataviewViewAccessOrException(dataview)
        validateDataviewJson(dataviewJson, UPDATE_PROPERTIES)

        String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemEdit : Permission.AssetExplorerEdit
        if (!securityService.hasPermission(requiredPerm)) {
            securityService.reportViolation("attempted to modify Dataview ($dataview.id) without required permission $requiredPerm")
            throw new UnauthorizedException(requiredPerm)
        }
    }

    /**
     * Validates if the person creating a dataview has permission to create a Dataview
     * @param dataviewJSON - the JSON object containing information about the Dataview to create
     * @throws UnauthorizedException
     */
    void validateDataviewCreateAccessOrException(JSONObject dataviewJson) {
        validateDataviewJson(dataviewJson, CREATE_PROPERTIES)

        // Check if user has necessary permission(s)
        String requiredPerm = dataviewJson.isSystem ? Permission.AssetExplorerSystemCreate : Permission.AssetExplorerCreate
        if (!securityService.hasPermission(requiredPerm)) {
            securityService.reportViolation("attempted to create a Dataview without required permission $requiredPerm")
            throw new UnauthorizedException(requiredPerm)
        }
    }

    /**
     * Validates if person deleting a Dataview has permission to do so
     * @param dataview - original object from database
     * @throws UnauthorizedException
     */
    void validateDataviewDeleteAccessOrException(Dataview dataview) {
        validateDataviewViewAccessOrException(dataview)

        String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemDelete : Permission.AssetExplorerDelete
        if (!securityService.hasPermission(requiredPerm)) {
            securityService.reportViolation("attempted to delete Dataview ($dataview.id) without required permission $requiredPerm")
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
                if (!dataviewJson.containsKey(prop)) {
                    log.warn "validateDataviewJson failed validation of JSON for property $prop for $dataviewJson"
                    throw new InvalidRequestException("JSON object missing property $prop")
                }
            }
        } else {
            throw new InvalidRequestException('Dataview JSON object was missing from request')
        }
    }

    /**
     * Perform a query against one or domains specified in the DataviewSpec passed into the method
     *
     * @param project - the project that the data should be isolated to
     * @param dataviewId - the specifications for the view/query
     * @param userParams - parameters from the user for filtering and sort order
     * @return a List of Map values
     */
    // TODO : Annotate READONLY
    List<Map> query(
            Project project,
            Long dataviewId,
            DataviewUserParamsCommand userParams) {

        Dataview dataview = Dataview.get(dataviewId)
        DataviewSpec dataviewSpec = new DataviewSpec(userParams, dataview)

        return previewQuery(project, dataviewSpec /*, userPrefs*/)
    }

    /**
     * Perform a query against one or domains specified in the DataviewSpec passed into the method
     * @param project - the project that the data should be isolated to
     * @param domainClass - the principle domain Class that should be queried
     * @param dataviewSpecJson - the specifications for the view/query as JSON
     * @param userParams - parameters from the user for filtering and sort order
     * @param userPrefs - any user perferences that the user may have for a give dataview (change of columns)
     * @return a List of Map values
     *
     * Example return values:
     * 		[
     * 			[
     * 				common.id: 12,
     * 				common.name: 'Exchange',
     * 				common.class: 'Application',
     * 				common.bundle: 'M1',
     * 				application.sme: 'Joe',
     * 				application.owner: 'Tony'
     * 			],
     * 			[
     * 				common.id: 23,
     * 				common.name: 'VM123',
     * 				common.class: 'Device',
     * 				common.bundle: 'M1',
     * 				device.os: 'Windows'
     * 				device.serial: '123123123',
     * 				device.tag: 'TM-234'
     * 			]
     * 		]
     */
    // TODO : Annotate READONLY
    List<Map> previewQuery(
            Project project,
            DataviewSpec dataviewSpec) {

        Map params = [:]
        String queryFields = ""

        String queryFrom = "from AssetEntity \n"
        queryFrom += " where project = :project \n"
        params['project'] = project

        if (dataviewSpec.justPlanning != null) {
            queryFrom += " and moveBundle in (:moveBundles) \n"
            params['moveBundles'] = MoveBundle.where {
                project == project && useForPlanning == dataviewSpec.justPlanning
            }.list()
        }

        dataviewSpec.columns.findAll { column ->

            String property = column.property
            def values = column.filter.split("\\|")?.findAll { it?.trim() != "" }

            queryFields += queryFields.isEmpty() ? "select " : ", "
            queryFields += " ${property}"

            if (values.size() == 1) {
                queryFrom += " and ${property} like :${property} \n"
                params[property] = "%$values%"
            } else if (values.size() > 1) {
                // Implement first criteria as like "%${column.filter}%" (note that this is SQL Injection waiting to happen)
                // TODO : In ticket TM-6532 we'll expand on the different types of queries
                queryFrom += " and ${property} in (:${property}) \n"
                params[property] = values
            }
        }

        String order = "order by ${dataviewSpec.order.property} ${dataviewSpec.order.sort}"

        def hql = queryFields + "\n" + queryFrom + order
        log.debug "DataViewService previewQuery hql: ${hql}"
        def assets = AssetEntity.executeQuery(hql, params, dataviewSpec.args)

        // Strip out non assetClass data that may have leaked into the result set. This only needs to be addressed if
        // there is more than domain in the query.
        // [
        // 	common.id: 123,
        // 	common.assetName: 'foo',
        // 	common.assetClass: 'Application',
        // 	application.businessUnit: 'HR',
        // 	device.os: ''
        // ]
        // Because it is assetClass Application, the device properties should be removed but the common to remain.

        assets.collect { columns ->
            Map row = [:]
            columns.eachWithIndex { cell, index ->
                row["${dataviewSpec.columns[index].domain}.${dataviewSpec.columns[index].property}"] = cell
            }
            row
        }
    }

}