/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
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
		validateDataviewViewAccessOrException(id, dataview);

		return dataview
	}

	/**
	 * Updates a database dataview object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param dataviewJson JSONObject to take changes from.
	 * @return the Dataview object that was updated
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	Dataview update(Integer id, JSONObject dataviewJson) {
		Dataview dataview = Dataview.get(id)
		validateDataviewUpdateAccessOrException(id, dataviewJson, dataview)

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
	 * @throws DomainUpdateException, UnauthorizedException
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
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	void delete(Integer id) {
		Dataview dataview = Dataview.get(id)
		validateDataviewDeleteAccessOrException(id, dataview)

		dataview.delete()
	}

	/**
	 * Validates if person accessing dataview is authorized to access it
	 * - should belong to current project in session
	 * - should be either system or shared or current person in session owned
	 * @param dataview
	 * @throws InvalidRequestException
	 */
	void validateDataviewViewAccessOrException(Integer id, Dataview dataview) {
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
			if (! allowedToView) {
				securityService.reportViolation("attempted to access non-shared Dataview ($dataview.id)")
				throwNotFound = true
			}
		}

		// Throw an exception if any of the above falied
		if (throwNotFound) {
			throw new EmptyResultException("Dataview not found ($id})")
		}
	}

	/**
	 * Validates if the person updating a dataview has permission to it.
	 * @param dataviewJSON - the JSON object containing information about the Dataview to create
	 * @param dataview - original object from database
	 * @throws UnauthorizedException
	 */
	void validateDataviewUpdateAccessOrException(Integer id, JSONObject dataviewJson, Dataview dataview) {
		validateDataviewViewAccessOrException(id, dataview)
		validateDataviewJson(dataviewJson, UPDATE_PROPERTIES)

		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemEdit : Permission.AssetExplorerEdit
		if (! securityService.hasPermission(requiredPerm)) {
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
		if (! securityService.hasPermission(requiredPerm)) {
			securityService.reportViolation("attempted to create a Dataview without required permission $requiredPerm")
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Validates if person deleting a Dataview has permission to do so
	 * @param dataview - original object from database
	 * @throws UnauthorizedException
	 */
	void validateDataviewDeleteAccessOrException(Integer id, Dataview dataview) {
		validateDataviewViewAccessOrException(id, dataview)

		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemDelete : Permission.AssetExplorerDelete
		if (! securityService.hasPermission(requiredPerm)) {
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
				if (! dataviewJson.containsKey(prop)) {
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
     * @return a Map with data as a List of Map values and pagination
     */
    // TODO : Annotate READONLY
    Map query(Project project, Dataview dataview, DataviewUserParamsCommand userParams) {

        DataviewSpec dataviewSpec = new DataviewSpec(userParams, dataview)
        return previewQuery(project, dataviewSpec)
    }

    /**
     * Perform a query against one or domains specified in the DataviewSpec passed into the method
	 *
     * @param project - the project that the data should be isolated to
     * @param dataviewSpec - the specifications for the view/query as JSON
     * @return a Map with data as a List of Map values and pagination
     *
     * Example return values:
     * 		[
     * 			data: [
     * 		    	[
     *   				common.id: 12,
     * 	    			common.name: 'Exchange',
     * 		    		common.class: 'Application',
     * 				    common.bundle: 'M1',
     * 			    	application.sme: 'Joe',
     *   				application.owner: 'Tony'
     * 	    		],
     * 			    [
     * 				    common.id: 23,
     * 				    common.name: 'VM123',
     * 				    common.class: 'Device',
     * 				    common.bundle: 'M1',
     * 				    device.os: 'Windows'
     * 				    device.serial: '123123123',
     * 				    device.tag: 'TM-234'
     * 			    ]
     * 			],
     * 		   pagination: [
     * 		        offset: 350,
     * 		        max: 100,
     * 		        total: 472
     * 		   ]
     * 		]
     */
    // TODO : Annotate READONLY
    Map previewQuery(Project project, DataviewSpec dataviewSpec) {

        String hqlColumns = hqlColumns(dataviewSpec)
        String hqlWhere = hqlWhere(dataviewSpec)
        String hqlOrder = hqlOrder(dataviewSpec)
        String hqlJoins = hqlJoins(dataviewSpec)

        String hql = """
            select $hqlColumns
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project $hqlWhere  
          order by $hqlOrder  
        """

        String countHql = """
            select count(*)
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project $hqlWhere
        """

        log.debug "DataViewService previewQuery hql: ${hql}, count hql: $countHql"

        def assets = AssetEntity.executeQuery(hql, hqlParams(project, dataviewSpec), dataviewSpec.args)
        def totalAssets = AssetEntity.executeQuery(countHql, hqlParams(project, dataviewSpec))

        previewQueryResults(assets, totalAssets[0], dataviewSpec)
    }

    /**
     *
     * Prepares the previewQuery result with data and pagination details.
     *
     * In data it returns all rows with column domain correctly set. e.g. common.id or application.appOwner
     * In pagination it returns the following map:
     *
     *  [ max: max, offset: offset, total: total ]
     *
     * @param assets a list from HQL result
     * @param total total of asset for the HQL query that's paginated
     * @param dataviewSpec a DataviewSpec instace with order definition
     * @return a Map with data and pagination defined as [data: [.. ..], pagination: [ max: ..., offset: ..., total: ... ]]
     */
    private Map previewQueryResults(List assets, Long total, DataviewSpec dataviewSpec) {
        [
                pagination: [
                        offset: dataviewSpec.offset, max: dataviewSpec.max, total: total
                ],
                assets    : assets.collect { columns ->
                    Map row = [:]
                    columns = [columns].flatten()
                    columns.eachWithIndex { cell, index ->
                        if (dataviewSpec.columns[index]) {
                            row["${dataviewSpec.columns[index].domain}_${dataviewSpec.columns[index].property}"] = cell
                        }
                    }

                    row
                }
        ]
    }
    /**
     *
     *
     *
     * @param dataviewSpec
     * @return
     */
	private String hqlJoins(DataviewSpec dataviewSpec) {
        dataviewSpec.columns.collect { Map column ->
            "${joinFor(column)}"
        }.join(" ")
    }
    /**
     *
     * Calculates HQL params from DataviewSpec for HQL query
     *
     * @param project a Project instance to be added in Parameters
     * @param dataviewSpec
     * @return
     */
	private Map hqlParams(Project project, DataviewSpec dataviewSpec) {
        Map params = [project: project]
        if (dataviewSpec.justPlanning != null) {
            params << [
                    moveBundles: MoveBundle.where {
                        project == project && useForPlanning == dataviewSpec.justPlanning
                    }.list()
            ]
        }
        dataviewSpec.filterColumns.each { Map column ->
            params << [("${namedParameterFor(column)}".toString()): calculateParamsFor(column)]
        }

        if(!dataviewSpec.domains.isEmpty()) {
            params << ["assetClasses" : dataviewSpec.domains.findResults { AssetClass.safeValueOf(it.toUpperCase())}]
        }

        params
    }
    /**
     *
     * Creates a String with all the columns correctly set for select clause
     *
     * @param dataviewSpec
     * @return
     */
	private String hqlColumns(DataviewSpec dataviewSpec){
        dataviewSpec.columns.collect { Map column ->
            "${propertyFor(column)}"
        }.join(", ")
    }
    /**
     *
     * Creates a String with all the columns correctly set for where clause
     *
     * @param dataviewSpec
     * @return
     */
	private String hqlWhere(DataviewSpec dataviewSpec){

        String where = ""

        if(!dataviewSpec.domains.isEmpty()){
            where += " and AE.assetClass in (:assetClasses) "
        }

        if (dataviewSpec.justPlanning != null) {
            where += " and AE.moveBundle in (:moveBundles) "
        }

        where += dataviewSpec.filterColumns.collect { Map column ->
            if (hasMultipleFilter(column)){
                " and ${propertyFor(column)} in (:${namedParameterFor(column)}) \n"
            } else {
                " and ${propertyFor(column)} like :${namedParameterFor(column)} \n"
            }
        }.join(" ")

        where
    }
    /**
     *
     * Checks if column.filter has values to be used in filter.
     *
     *
     * @param column a Column with filter value
     * @return
     */
	private Boolean hasMultipleFilter(Map column) {
        splitColumnFilter(column).size() > 1
    }

    /**
     *
     * Calculate Map with params splitting column.filter content
     * if column.flter has only one value It's prepared with %${column.filter}%
     * in order to use like filter in HQL query.
     *
     * Also, if a parameter should be Strings, it is parsed and converted to the correct type
     *
     * @param column
     * @return
     */
	private def calculateParamsFor(Map column){
        String[] values = splitColumnFilter(column)

        if(values.size()==1){
            paramConversionFor(column, "%${values[0]}%")
        } else {
            values.collect { paramConversionFor(column, it) }
        }
    }
    /**
     *
     * Columnn filters value could be split by '|' separator
     *
     * For example:
     *
     * { "domain": "common", "property": "environment", "filter": "production|development" }
     *
     */
	private String[] splitColumnFilter(Map column) {
        column.filter.split("\\|")
    }

	private String hqlOrder(DataviewSpec dataviewSpec){
        "${propertyFor(dataviewSpec.order)} ${dataviewSpec.order.sort}"
    }

    private static String namedParameterFor(Map column) {
        transformations[column.property].namedParamter
    }

    private static String propertyFor(Map column) {
        transformations[column.property].property
    }

    private static String joinFor(Map column) {
        transformations[column.property].join
    }

    private static def paramConversionFor(Map column, String value) {
        transformations[column.property].transform(value)
    }


    private static final Map<String, Map> transformations = [
            "id"          : [property: "AE.id", namedParamter: "moveBundleName", join: "", transform: { String value -> Long.parseLong(value) }],
            "moveBundle"  : [property: "AE.moveBundle.name", namedParamter: "moveBundleName", join: "left outer join AE.moveBundle", transform: { String value -> value?.trim() }],
            "project"     : [property: "AE.project.description", namedParamter: "projectDescription", join: "left outer join AE.project", transform: { String value -> value?.trim() }],
            "manufacturer": [property: "AE.manufacturer.name", namedParamter: "manufacturerName", join: "left outer join AE.manufacturer", transform: { String value -> value?.trim() }],
            "sme"         : [property: "AE.sme.firstName", namedParamter: "smeFirstName", join: "left outer join AE.sme", transform: { String value -> value?.trim() }],
            "sme2"        : [property: "AE.sme2.firstName", namedParamter: "sme2FirstName", join: "left outer join AE.sme2", transform: { String value -> value?.trim() }],
            "model"       : [property: "AE.model.modelName", namedParamter: "modelModelName", join: "left outer join AE.model", transform: { String value -> value?.trim() }],
            "appOwner"    : [property: "AE.appOwner.firstName", namedParamter: "appOwnerFirstName", join: "left outer join AE.appOwner", transform: { String value -> value?.trim() }]
    ].withDefault { String key -> [property: "AE." + key, namedParamter: key, join: "", transform: { String value -> value?.trim() }] }
}