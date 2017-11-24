/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.domain.*
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

	static final InvalidParamException MISSING_ID_EXCEPTION = new InvalidParamException('Missing required id')
	static final EmptyResultException VIEW_NOT_FOUND_EXCEPTION = new EmptyResultException('View was not found')

	// Limit the number of favorites that should be returned in queries.
	static final int FAVORITES_MAX_SIZE = 10

	/**
	 *
	 * Query for getting all projects where: belong to current project and either shared, system or are owned by
	 * current person in session
	 *
	 * @param person
	 * @param project
	 * @return
	 */
    List<Dataview> list(Person userPerson, Project userProject) {
        def query
		List<Long> favoriteSystemViewIds

        boolean canSeeSystemViews = securityService.hasPermission(userPerson, Permission.AssetExplorerSystemList)
        if (canSeeSystemViews) {
            query = Dataview.where {
                (project == userProject && (isShared == true || person == userPerson)) \
				|| \
				(project.id == Project.DEFAULT_PROJECT_ID && isSystem == true)
            }
        } else {
			// Get list of user's list favorite View Ids
            favoriteSystemViewIds = FavoriteDataview.where { person == userPerson }
        		.projections { property('dataview.id') }
				.list()

            if (favoriteSystemViewIds) {
                query = Dataview.where {
                    (project == userProject  && (isShared == true || person == userPerson)) \
					|| \
                    (project.id == Project.DEFAULT_PROJECT_ID && isSystem == true && id in favoriteSystemViewIds)
                }
            } else {
                query = Dataview.where {
                    (project == userProject  && (isShared == true || person == userPerson))
                }
            }
        }

        return query.list()
    }

    /**
	 * Returns a Dataview by id after validating that the user has proper access
	 * @param id
	 * @return
	 */
	Dataview fetch(Long id) throws InvalidParamException, EmptyResultException {
		if (! id || id < 1) {
			throw MISSING_ID_EXCEPTION
		}

		Dataview dataview = Dataview.get(id)
		if (!dataview) {
			throw VIEW_NOT_FOUND_EXCEPTION
		} else {
			validateDataviewViewAccessOrException(id, dataview);
		}

		return dataview
	}

	/**
	 * Updates a database dataview object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param dataviewJson JSONObject to take changes from.
	 * @return the Dataview object that was updated
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	Dataview update(Person person, Project project, Long id, JSONObject dataviewJson) {
		Dataview dataview = Dataview.get(id)
		validateDataviewUpdateAccessOrException(id, dataviewJson, dataview)

		dataview.with {
			reportSchema = dataviewJson.schema
			isShared = dataviewJson.isShared
		}

		if (!dataview.save()) {
			throw new DomainUpdateException('Error on update', dataview)
		}

		Long currentPersonId = person.id

		// Check if the view is favorite and must be unfavorited.
		if (dataview.isFavorite(currentPersonId) && !dataviewJson.isFavorite) {
			deleteFavoriteDataview(person, project, dataview.id)
		} else {
			// Check if the view must be favorited
			if (!dataview.isFavorite(currentPersonId) && dataviewJson.isFavorite) {
				addFavoriteDataview(person, project, dataview.id)
			}
		}

		return dataview
	}

	/**
	 *
	 * Create a Dataview object and add it to the person's favorite if needed.
	 * Dataview person and project should be passed as a parameter
	 *
	 * @param currentPerson
	 * @param currentProject
	 * @param dataviewJson - JSONObject to take changes from.
	 * @return the Dataview object that was created
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	Dataview create(Person currentPerson, Project currentProject, JSONObject dataviewJson) {
		validateDataviewCreateAccessOrException(dataviewJson)

		Dataview dataview = new Dataview()
		dataview.with {
			person = currentPerson
            project = currentProject
			name = dataviewJson.name
			isSystem = dataviewJson.isSystem
			isShared = dataviewJson.isShared
			reportSchema = dataviewJson.schema
		}

		if (!dataview.save()) {
			throw new DomainUpdateException('Error on create', dataview)
		}

		if (dataviewJson.isFavorite) {
			addFavoriteDataview(currentPerson, currentProject, dataview.id)
		}

		return dataview
	}

	/**
	 * Deletes a Dataview object
	 * Dataview person and project are taken from current session
	 * @param id Dataview id to delete
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	void delete(Long id) {
		Dataview dataview = fetch(id)
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
	void validateDataviewViewAccessOrException(Long id, Dataview dataview) {
		boolean throwNotFound = false
		if (!dataview) {
			throwNotFound = true
		}

        boolean canAccess = 
			(dataview.project.id == Project.DEFAULT_PROJECT_ID && dataview.isSystem) \
			|| (dataview.project.id == securityService.userCurrentProject.id)

		// Validate Dataview belongs to the project
		if (!throwNotFound && !canAccess) {
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
	void validateDataviewUpdateAccessOrException(Long id, JSONObject dataviewJson, Dataview dataview) {
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
	void validateDataviewDeleteAccessOrException(Long id, Dataview dataview) {
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

		dataviewSpec = addRequieredColumns(dataviewSpec)

        String hqlColumns = hqlColumns(dataviewSpec)
        String hqlWhere = hqlWhere(dataviewSpec)
        String hqlOrder = hqlOrder(dataviewSpec)
        String hqlJoins = hqlJoins(dataviewSpec)
		Map hqlParams = hqlParams(project, dataviewSpec)

        String hql = """
			select $hqlColumns
				from AssetEntity AE
				$hqlJoins
				where AE.project = :project and $hqlWhere  
			order by $hqlOrder
        """

		String countHql = """
            select count(*)
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project and $hqlWhere
        """

        // log.debug "DataViewService previewQuery hql: ${hql}, count hql: $countHql"

        List assets = AssetEntity.executeQuery(hql, hqlParams, dataviewSpec.args)
        Long totalAssets = AssetEntity.executeQuery(countHql, hqlParams)[0]

        previewQueryResults(assets, totalAssets, dataviewSpec)
    }

	/**
	 * Retrieve the list of favorite data views for the current person,
	 * @return List of dataviews (DataView instances) the user has favorited.
	 */
	List<Dataview> getFavorites(Person person) {
		// Retrieve all the favorited views for the person.
		/* Using createCriteria instead of where because there's no way of limiting the number
		  of results using DetachedCriteria */
		List<FavoriteDataview> favorites = FavoriteDataview.createCriteria().list {
			and {
				person == person
			}
			projections {
				property('dataview')
			}
			maxResults(FAVORITES_MAX_SIZE)

		}
		return favorites
	}

	/**
	 * Delete the given dataview from the current person's favorites.
	 * @param dataviewId
	 * @return
	 */
	void deleteFavoriteDataview(Person person, Project currentProject, Long dataviewId) throws EmptyResultException, DomainUpdateException{
		Dataview dataview = fetch(dataviewId)

		// Delete the corresponding favorite
		int deletedFavs = FavoriteDataview.where{
			person == person && dataview == dataview
		}.deleteAll()

		// If no favs were deleted, throw an exception.
		if (deletedFavs == 0) {
			throw new DomainUpdateException('Favorite was not found')
		}
	}

	/**
	 * Add the given dataview to the current person's favorites.
	 * @param dataviewId
	 */
	void addFavoriteDataview(Person person, Project currentProject, Long dataviewId) {
		Dataview dataview = fetch(dataviewId)

		// Check if the favorite already exists.
		FavoriteDataview favoriteDataview = FavoriteDataview.where {
			person == person && dataview == dataview
		}.find()

		// If a favorite was found, throw an exception
		if (favoriteDataview) {
			throw new DomainUpdateException('View is already a favorite')
		}

		favoriteDataview = new FavoriteDataview(person: person, dataview: dataview)
		if (!favoriteDataview.save()) {
			throw new DomainUpdateException('Unable to create favorite', favoriteDataview)
		}
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
				offset: dataviewSpec.offset, max: dataviewSpec.max?:total, total: total
			],
			assets: assets.collect { columns ->
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
	 * Used to prepare the left outer join sentence based on
	 * columns of a dataview Spec and transformations for HQL query
     * @param dataviewSpec
     * @return the left outer join HQL sentence from the a Dataview spec
     */
	private String hqlJoins(DataviewSpec dataviewSpec) {
        dataviewSpec.columns.collect { Map column ->
            "${joinFor(column)}"
        }.join(" ")
    }

    /**
     * Calculates HQL params from DataviewSpec for HQL query
     * @param project a Project instance to be added in Parameters
     * @param dataviewSpec
     * @return a Map with params to be used in a executeQuery with an HQL query.
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
	 * Creates a String with all the columns correctly set for select clause
	 * @param dataviewSpec
	 * @return
	 */
	private String hqlColumns(DataviewSpec dataviewSpec){
		dataviewSpec.columns.collect { Map column ->
			"${propertyFor(column)}"
		}.join(", ")
	}

    /**
     * Creates a String with all the columns correctly set for where clause
     * @param dataviewSpec
     * @return
     */
	private String hqlWhere(DataviewSpec dataviewSpec) {

		List where = []

        if(!dataviewSpec.domains.isEmpty()){
            where << "AE.assetClass in (:assetClasses)"
        }

        if (dataviewSpec.justPlanning != null) {
            where << "AE.moveBundle in (:moveBundles)"
        }

		dataviewSpec.filterColumns.each { Map column ->
			if (hasCustomFilterFor(column)){
				if (hasMultipleFilter(column)){
					where << "${getCustomFilterIn(column)}) \n"
				} else {
					where <<  "${getCustomFilterLike(column)} \n"
				}
			} else {
				if (hasMultipleFilter(column)){
					where << "${propertyFor(column)} in (:${namedParameterFor(column)}) \n"
				} else {
					where <<  "${propertyFor(column)} like :${namedParameterFor(column)} \n"
				}
			}
		}
		where.join(' and ')
	}

	/**
     * Checks if column.filter has values to be used in filter
     * @param column a Column with filter value
     * @return
     */
	private Boolean hasMultipleFilter(Map column) {
        splitColumnFilter(column).size() > 1
    }

	/**
	 * Checks if the column has a custom filter defined
	 * @param column a Column to be checked
	 * @return
	 */
	private Boolean hasCustomFilterFor(Map column) {
		transformations[column.property].customFilterIn != null \
		|| \
		transformations[column.property].customFilterLike != null
	}

    /**
     * Calculate Map with params splitting column.filter content
     * if column.filter has only one value It's prepared with %${column.filter}%
     * in order to use like filter in HQL query.
     * @param column
     * @return
     */
	private def calculateParamsFor(Map column){
        String[] values = splitColumnFilter(column)

        if (values.size() == 1) {
			"%${values[0].trim()}%".toString()
        } else {
			values.collect { "${it.trim()}".toString()}
        }
    }

    /**
     * Columnn filters value could be split by '|' separator
	 * @param column - a set of column attributes
	 * @return one or more strings based on filter being spilt
     * For example:
     * 		{ "domain": "common", "property": "environment", "filter": "production|development" }
     */
	private String[] splitColumnFilter(Map column) {
        column.filter.split("\\|")
    }

    /**
     * Calculates the order from DataviewSpec for HQL query
     * @param dataviewSpec
     * @return a String HQL order sentence
     */
	private String hqlOrder(DataviewSpec dataviewSpec){
        "${orderFor(dataviewSpec.order)} ${dataviewSpec.order.sort}"
    }

    private static String namedParameterFor(Map column) {
        transformations[column.property].namedParameter
    }

    private static String propertyFor(Map column) {
		transformations[column.property].property
    }

    private static String joinFor(Map column) {
        transformations[column.property].join
    }

	private static String orderFor(Map column) {
		transformations[column.property].alias ?: propertyFor(column)
	}

	private static String getCustomFilterIn(Map column) {
		transformations[column.property].customFilterIn
	}

	private static String getCustomFilterLike(Map column) {
		transformations[column.property].customFilterLike
	}

	/** 
	 * Used to generate HQL that will properly concatenate a person's full name
	 * @param propertyName - the particular property to construct the HQL for
	 * @return the HQL for the computated fullName 
	 */
	private static String personFullName(String propertyName) {
		"""
			CONCAT( 
				COALESCE(AE.${propertyName}.firstName, ''),
				CASE WHEN COALESCE(AE.${propertyName}.middleName, '') = '' THEN '' ELSE ' ' END,
				COALESCE(AE.${propertyName}.middleName,''),
				CASE WHEN COALESCE(AE.${propertyName}.lastName, '') = '' THEN '' ELSE ' ' END,
				COALESCE(AE.${propertyName}.lastName,'')
			)
		"""
	}

	/**
	 * Used to render the WHERE logic to filter on a person with LIKE statement
	 * @param propertyName - the name of the property to query (e.g. sme, sme2 or appOwner)
	 * @param namedParameter - the name to use as the parameter fed into the query
	 * @return the SQL to append to the WHERE clause
	 */
	private static String customFilterLike(String propertyName, String namedParameter) {
		"( ${personFullName(propertyName)} like :${namedParameter} )"
	}

	/**
	 * Used to render the WHERE logic to filter on a person with IN statement
	 * @param propertyName - the name of the property to query (e.g. sme, sme2 or appOwner)
	 * @param namedParameter - the name to use as the parameter fed into the query
	 * @return the SQL to append to the WHERE clause
	 */
	private static String customFilterIn(String propertyName, String namedParameter) {
		"( ${personFullName(propertyName)} in (:${namedParameter}) )"
	}

    private static final Map<String, Map> transformations = [
		'id'             : [property: 'str(AE.id)', type: String, namedParameter: 'id', join: ''],
		'assetClass'     : [property: 'str(AE.assetClass)', type: String, namedParameter: 'assetClass', join: ''],
		'moveBundle'     : [property: 'AE.moveBundle.name', type: String, namedParameter: 'moveBundleName', join: 'left outer join AE.moveBundle'],
		'project'        : [property: 'AE.project.description', type: String, namedParameter: 'projectDescription', join: 'left outer join AE.project'],
		'manufacturer'   : [property: 'AE.manufacturer.name', type: String, namedParameter: 'manufacturerName', join: 'left outer join AE.manufacturer'],
		'appOwner'       : [property: "${personFullName('appOwner')} as appOwner", 
							type: String, namedParameter: 'appOwnerName', 
							join: 'left outer join AE.appOwner', 
							customFilterLike: customFilterLike('appOwner','appOwnerName'), 
							customFilterIn: customFilterIn('appOwner','appOwnerName'), alias:'appOwner'],
		'sme'            : [property: "${personFullName('sme')} as sme", 
							type: String, 
							namedParameter: 'smeName', 
							join: 'left outer join AE.sme', 
							customFilterLike: customFilterLike('sme','smeName'), 
							customFilterIn: customFilterIn('sme','smeName'), alias:'sme'],
		'sme2'           : [property: "${personFullName('sme2')} as sme2", 
							type: String, 
							namedParameter: 'sme2Name', 
							join: 'left outer join AE.sme2', 
							customFilterLike: customFilterLike('sme2','sme2Name'), 
							customFilterIn: customFilterIn('sme2','sme2Name'), alias:'sme2'],
		'model'          : [property: 'AE.model.modelName', type: String, namedParameter: 'modelModelName', join: 'left outer join AE.model'],
		'sourceLocation' : [property: 'AE.roomSource.location', type: String, namedParameter: 'sourceLocation', join: 'left outer join AE.roomSource'],
		'sourceRack'     : [property: 'AE.rackSource.tag', type: String, namedParameter: 'sourceRack', join: 'left outer join AE.rackSource'],
		'sourceRoom'     : [property: 'AE.roomSource.roomName', type: String, namedParameter: 'sourceRack', join: 'left outer join AE.roomSource'],
		'targetLocation' : [property: 'AE.roomTarget.location', type: String, namedParameter: 'targetLocation', join: 'left outer join AE.roomTarget'],
		'targetRack'     : [property: 'AE.rackTarget.tag', type: String, namedParameter: 'targetRack', join: 'left outer join AE.rackTarget'],
		'targetRoom'     : [property: 'AE.roomTarget.roomName', type: String, namedParameter: 'targetRack', join: 'left outer join AE.roomTarget']
    ].withDefault {
        String key -> [property: "AE." + key, type: String, namedParameter: key, join: "", mode:"where"]
    }

	/**
	 * Mutate DataViewSpec to add the requiered Columns when querying the DB
	 * This mutates the original Object
	 * @param dataviewSpec Original DataViewSpec
	 * @return the mutated dataViewSpec
	 */
	private DataviewSpec addRequieredColumns(DataviewSpec dataviewSpec){
		HashSet<String> requiredColumns = ['id', 'assetClass']

		requiredColumns = requiredColumns - dataviewSpec.columns*.property

		for(String property: requiredColumns) {
			dataviewSpec.addColumn(DataviewSpec.COMMON, property)
		}

		return dataviewSpec
	}
}
