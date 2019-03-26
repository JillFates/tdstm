/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.DataviewApiFilterParam
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.command.DataviewNameValidationCommand
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.FavoriteDataview
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.Permission
import net.transitionmanager.service.dataview.DataviewSpec
import org.grails.web.json.JSONObject

import java.sql.Timestamp

/**
 * Service class with main database operations for Dataview.
 * @see net.transitionmanager.domain.Dataview
 */
class DataviewService implements ServiceMethods {

	static ProjectService projectService
	UserPreferenceService userPreferenceService
	@Lazy
	private static CustomDomainService customDomainService = { -> ApplicationContextHolder.getBean('customDomainService', CustomDomainService) }()

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
	@Transactional
	Dataview update(Person person, Project project, Long id, JSONObject dataviewJson) {
		Dataview dataview = Dataview.get(id)
		validateDataviewUpdateAccessOrException(id, dataviewJson, dataview)

		dataview.with {
			reportSchema = dataviewJson.schema
			isShared = dataviewJson.isShared
		}

		if (!dataview.save(failOnError:false)) {
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
	@Transactional
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

		if (!dataview.save(failOnError: false)) {
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
	@Transactional
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
	 * @param dataview - the specifications for the view/query
	 * @param apiParamsCommand - parameters from the user for filtering and sort order
	 * @return a Map with data as a List of Map values and pagination
	 */
	// TODO : Annotate READONLY
	Map query(Project project, Dataview dataview, DataviewApiParamsCommand apiParamsCommand) {
		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(apiParamsCommand, dataview, fieldSpecProject)
		previewQuery(project, dataviewSpec)
	}

	/**
	 * Gets the hql for filtering by asset ids, based of the filters from the all asset views.
	 *
	 * @param project The current project used to limit the query.
	 * @param dataViewId The id of the dataview used in generating the DataviewSpec.
	 * @param userParams the user parameters from the front end that are used in generating the DataviewSpec.
	 *
	 * @return a map containing the hql query string, and the parameters needed to run the query.
	 */
	Map getAssetIdsHql(Project project, Long dataViewId, DataviewUserParamsCommand userParams) {
		Dataview dataview = get(Dataview, dataViewId, project)

		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(userParams, dataview, fieldSpecProject)

		Map whereInfo = hqlWhere(dataviewSpec, project)
		String conditions = whereInfo.conditions
		String hqlJoins = hqlJoins(dataviewSpec)

		String query = """
			SELECT AE.id
			FROM AssetEntity AE
			$hqlJoins
			WHERE AE.project = :project AND $conditions
			group by AE.id
	    """

		return [query: query, params: whereInfo.params]
	}

	Map previewQuery(Project project, Dataview dataview, DataviewUserParamsCommand dataviewUserParamsCommand) {
		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(dataviewUserParamsCommand, dataview, fieldSpecProject)
		previewQuery(project, dataviewSpec)
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
		Map whereInfo = hqlWhere(dataviewSpec, project)
		String conditions = whereInfo.conditions
		Map whereParams = whereInfo.params
		String hqlOrder = hqlOrder(dataviewSpec)
		String hqlJoins = hqlJoins(dataviewSpec)

		String hql = """
            select $hqlColumns
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project and $conditions
		  group by AE.id
          order by $hqlOrder
        """

		String countHql = """
            select count(DISTINCT AE)
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project and $conditions
        """

		def assets = AssetEntity.executeQuery(hql, whereParams, dataviewSpec.args)
	    def totalAssets = AssetEntity.executeQuery(countHql, whereParams)

	    Map queryResults = previewQueryResults(assets, totalAssets[0], dataviewSpec)

	    postProcessAssetQuery(queryResults, whereInfo.mixedFields)

	    return queryResults

    }

	/**
	 * After the query for assets is invoked, this method needs to be called
	 * to perform some final operations on the query results, if needed.
	 *
	 * @param queryResults
	 * @param mixedFieldsInfo
	 */
	private void postProcessAssetQuery(Map queryResults, Map mixedFieldsInfo) {
		// Check if mixed fields were detected when building the query.
		if (mixedFieldsInfo) {
			mixedFieldsInfo.each {field, fieldInfo ->
				Closure transformer = mixedTransformerFor(field)
				if (transformer) {
					transformer(queryResults, field, fieldInfo)
				}
			}
		}

		// Add the flags signaling whether or not each asset has comments and/or tasks associated.
		appendTasksAndComments(queryResults)

	}

	/**
	 * After querying for the assets we need to determine if the returned assets
	 * have comments and/or tasks associated.
	 *
	 * @param queryResults - the result of the assets query.
	 */
	private void appendTasksAndComments(Map queryResults) {
		// Build a list with the asset ids and initialize tasks and comments flags.
		List<Long> assetIds = []
		// List with the assets found by the preview method.
		List<Map> assetResults = queryResults['assets']
		// This map will contain, for each asset, the flags for each comment type.
		Map<Long, Map<String, Boolean>> commentsAndTasksMap = [:]
		// Iterate over the assets keeping their id and initializing the map for its flags.
		assetResults.each { Map assetMap ->
			Long assetId = NumberUtil.toLong(assetMap['common_id'])
			assetIds << assetId
			commentsAndTasksMap[assetId] = [issue: false, comment: false]
		}

		if (assetIds) {
			// Query for assets and the comment types associated with them.
			String tasksAndCommentsQuery = """
				SELECT assetEntity.id, commentType FROM AssetComment 
				WHERE assetEntity.id IN (:assetIds) AND isPublished = true
				GROUP BY assetEntity.id, commentType
			"""

			// Execute the query.
			List tasksAndComments = AssetComment.executeQuery(tasksAndCommentsQuery, [assetIds: assetIds])

			// Iterate over the results from the database updating the flags map.
			for (taskOrComment in tasksAndComments) {
				Long assetId = taskOrComment[0]
				String commentType = taskOrComment[1]
				commentsAndTasksMap[assetId][commentType] = true
			}

			// Iterate over the preview assets setting the flags map as an attribute.
			assetResults.each { Map assetMap ->
				Long assetId = NumberUtil.toLong(assetMap['common_id'])
				assetMap['taskAndCommentFlags'] = commentsAndTasksMap[assetId]
			}
		}
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
	@Transactional
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
	@Transactional
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
		if (!favoriteDataview.save(failOnError: false)) {
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
					Map column = dataviewSpec.columns[index]

					if (column.property == 'tagAssets'){
						cell = handleTags(cell)
					}
					if (column.property == 'scale'){
						if (cell) {
							cell = cell.value
						}
					}

					if (column) {
						row["${column.domain}${DataviewApiFilterParam.FIELD_NAME_SEPARATOR_CHARACTER}${column.property}"] = cell
					}
				}

				row
			}
        ]
    }

	private handleTags(String cell) {
		def json = JsonUtil.parseJson("""{"tags":$cell}""").tags

		return json.collect { Map tag ->
			tag.css = Color.valueOfParam(tag.color).css
			return tag
		}
	}

    /**
	 * Used to prepare the left outer join sentence based on
	 * columns of a dataview Spec and transformations for HQL query
     * @param dataviewSpec
     * @return the left outer join HQL sentence from the a Dataview spec
     */
	private String hqlJoins(DataviewSpec dataviewSpec) {
		return dataviewSpec.columns.collect { Map column ->
            "${joinFor(column)}"
        }.join(" ")
    }


	/**
	 * Creates a String with all the columns correctly set for select clause
	 * @param dataviewSpec an instance of {@code DataviewSpec}
	 * @return
	 */
	private String hqlColumns(DataviewSpec dataviewSpec){
		return dataviewSpec.columns.collect { Map column ->
			"${projectionPropertyFor(column)}"
		}.join(", ")
	}

    /**
     * Creates a String with all the columns correctly set for where clause
     * @param dataviewSpec
	 * @param project
     * @return
     */
	private Map hqlWhere(DataviewSpec dataviewSpec, Project project) {

		// List of conditions for the WHERE clause.
		List whereConditions = []
		// Params for the WHERE
		Map whereParams = [project: project]

		if(!dataviewSpec.domains.isEmpty()){
			whereConditions << "AE.assetClass in (:assetClasses)"
			whereParams << ["assetClasses" : dataviewSpec.domains.findResults { AssetClass.safeValueOf(it.toUpperCase())}]
		}

		if (dataviewSpec.justPlanning != null) {
			whereConditions << "AE.moveBundle in (:moveBundles)"
			whereParams << [
					moveBundles: MoveBundle.where {
						project == project && useForPlanning == dataviewSpec.justPlanning
					}.list()
			]
		}

		// Populate this list with the mix fields that the user is using for filtering.
		Map<String, List> mixedFieldsInfo = [:]

		// The keys for all the declared mixed fields.
		Set mixedKeys = mixedFields.keySet()

		Map additionalInfo = [:]

		// Iterate over each column
		dataviewSpec.columns.each { Map column ->

			Class type = typeFor(column)
			// Check if the user provided a filter expression.
			// TODO: dcorrea: TM-13471 Turn off filter by date and datetime.
			if (StringUtil.isNotBlank(filterFor(column)) && !(type in [Date, Timestamp])) {

				// Create a basic FieldSearchData with the info for filtering an individual field.
				FieldSearchData fieldSearchData = new FieldSearchData([
						column: propertyFor(column),
						columnAlias: namedParameterFor(column),
						domain: domainFor(column),
						filter: filterFor(column),
						type: type,
						whereProperty: wherePropertyFor(column),
						manyToManyQueries: manyToManyQueriesFor(column),
						fieldSpec: column.fieldSpec
				])

				String property = propertyFor(column)
				// Check if the current column requires special treatment (e.g. startupBy, etc.)

				if (property in mixedKeys) {
					// Flag the fieldSearchData as mixed.
					fieldSearchData.setMixed(true)
					// Retrieve the additional results (e.g: persons matching the filter).
					Closure sourceForField = sourceFor(property)
					Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
					if (additionalResults) {
						// Keep a copy of this results for later use.
						mixedFieldsInfo[property] = additionalResults
						// Add additional information for the query (e.g: the staff ids for IN clause).
						Closure paramsInjector = injectWhereParamsFor(property)
						paramsInjector(fieldSearchData, property, additionalResults)
						// Add the sql where clause for including the additional fields in the query
						Closure whereInjector = injectWhereClauseFor(property)
						whereInjector(fieldSearchData, property)
					} else {
					// If no additional results, then unset the flag as no additional filtering should be required.
						fieldSearchData.setMixed(false)
					}

				}

				// Trigger the parsing of the parameter.
				SqlUtil.parseParameter(fieldSearchData)

				if (fieldSearchData.sqlSearchExpression) {
					// Append the where clause to the list of conditions.
					whereConditions << fieldSearchData.sqlSearchExpression
				}

				if (fieldSearchData.sqlSearchParameters) {
					// Add the parameters required for this field.
					whereParams += fieldSearchData.sqlSearchParameters
				}

			// If the filter for this column is empty, some logic/transformation might still be required for the mixed fields
			} else {
				String property = propertyFor(column)
				if (property in mixedKeys) {
					Closure sourceForField = sourceFor(property)
					Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
					// Keep a copy of this results for later use.
					mixedFieldsInfo[property] = additionalResults
				}
			}
		}

		return [conditions: whereConditions.join(" AND \n"), params: whereParams, mixedFields: mixedFieldsInfo]
	}

    /**
     * Columnn filters value could be split by '|' separator
	 * @param column - a set of column attributes
	 * @return one or more strings based on filter being spilt
     * For example:
     * 		{ "domain": "common", "property": "environment", "filter": "production|development" }
     */
	private String[] splitColumnFilter(Map column) {
        return column.filter.split("\\|")
    }

    /**
     * Calculates the order from DataviewSpec for HQL query
     * @param dataviewSpec
     * @return a String HQL order sentence
     */
	private String hqlOrder(DataviewSpec dataviewSpec){
		return "${orderFor(dataviewSpec.order)} ${dataviewSpec.order.sort}"
    }

    private static String namedParameterFor(Map column) {
		return transformations[column.property].namedParameter
    }

	/**
	 * https://docs.jboss.org/hibernate/orm/3.5/reference/en/html/queryhql.html#queryhql-expressions
	 * @param column
	 * @return
	 */
	private static String propertyFor(Map column) {

		String property = transformations[column.property].property
		FieldSpec fieldSpec = column.fieldSpec

		if(fieldSpec?.isCustom()){
			property = fieldSpec.getHibernateCastSentence(property)
		}

		return property
	}

    private static String joinFor(Map column) {
		return transformations[column.property].join
    }

	private static String orderFor(Map column) {
		return transformations[column.property].alias ?: propertyFor(column)
	}

	/**
	 * Return the type for this column
	 * @param column
	 * @return
	 */
	private static Class typeFor(Map column) {

		Class type = transformations[column.property].type
		FieldSpec fieldSpec = column.fieldSpec

		if (fieldSpec?.isCustom()) {
			type = fieldSpec.getClassType()
		}

		return type
	}

	/**
	 * Return the alias for the given column.
	 * @param column
	 * @return
	 */
	private static String aliasFor(Map column) {
		return transformations[column.property].alias
	}

	/**
	 * Build the projection for the given column.
	 *
	 * @param column a Map with column definitions
	 * @return
	 */
	private static String projectionPropertyFor(Map column) {
		String projection
		String property = propertyFor(column)
		String alias = aliasFor(column)
		if (alias) {
			projection = "$property AS $alias"
		} else {
			projection = property
		}

		return projection
	}


	/**
	 * Return the Domain class this column belongs to.
	 *
	 * @param column
	 * @return
	 */
	private static Class domainFor(Map column) {
		String domain = column.domain == "common" ? "DEVICE" : column.domain.toUpperCase()
		AssetClass assetClass = AssetClass.safeValueOf(domain)
		return AssetClass.domainClassFor(assetClass)
	}

	/**
	 * Retrieve the filter the user provided for this column.
	 * @param column
	 * @return
	 */
	private static String filterFor(Map column) {
		return column.filter
	}

	/**
	 * Return the expression that needs to be used when constructing the 'where'
	 * clause for the given field.
	 *
	 * This is added to catch scenarios, such as tags, where the expression for projecting the field
	 * and for filtering it are different.
	 * @param column
	 * @return
	 */
	private static String wherePropertyFor(Map column) {
		return transformations[column.property].whereProperty?: propertyFor(column)
	}

	/**
	 * Return the complementary query to be used in cases of many to many relationships.
	 * @param column
	 * @return
	 */
	private static Map manyToManyQueriesFor(Map column) {
		return transformations[column.property].manyToManyQueries
	}

	/**
	 * Return the parameter to be used when filtering many to many relationships.
	 * @param column
	 * @return
	 */
	private static String manyToManyParameterNameFor(Map column) {
		return transformations[column.property].manyToManyParameterName
	}


	/**
	 * Return the closure for fetching additional elements for this
	 * mixed field.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure sourceFor(String mixedField) {
		return mixedFields[mixedField]["source"]
	}

	/**
	 * Return the closure that needs to be executed in order to
	 * do the final replacement/transformation.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure mixedTransformerFor(String mixedField) {
		Closure transformer = null
		Map field = mixedFields[mixedField]
		if (field) {
			transformer = field["transform"]
		}
		return transformer
	}

	private static String mixedAliasFor(String mixedField) {
		return mixedFields[mixedField]["mixedAlias"]
	}

	/**
	 * Return the closure that is executed to add additional where clauses according to the field.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure injectWhereClauseFor(String mixedField) {
		return mixedFields[mixedField]["injectWhereClause"]
	}

	/**
	 * Return the closure to be executed to perform additional work on the
	 * given field before triggering the search, like providing the list
	 * of staff ids for the by properties.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure injectWhereParamsFor(String mixedField) {
		return mixedFields[mixedField]["injectWhereParams"]
	}

	/**
	 * Return the alias to be used while post processing the query results.
	 *
	 * @param field
	 * @return
	 */
	private static String postProcessAliasFor(String field) {
		return mixedFields[field]["postProcessAlias"]
	}

	/**
	 * Tansformer for the By fields that replaces IDs with the
	 * corresponding person's fullname.
	 */
	private static transformByField = { Map queryResults, field,  mixedResults->
		queryResults.assets.each { asset ->
			String fieldAlias = postProcessAliasFor(field)
			String originalValue = asset[fieldAlias]
			if (NumberUtil.isLong(originalValue)) {
				asset[fieldAlias] = mixedResults[originalValue]
			}
		}

	}

	/**
	 * Add the where for querying a list of persons ids.
	 */
	private static injectByPropertyWhereClause = { FieldSearchData fieldSearchData, String field ->
		fieldSearchData.setMixedSqlExpression("${field} in (:${mixedAliasFor(field)}")
	}

	/**
	 * Add the staff ids as a list of strings.
	 */
	private static injectByPropertyWhereParams = { FieldSearchData fieldSearchData, String property, Map additionalResults ->
		Set ids = additionalResults.keySet()
		fieldSearchData.addSqlSearchParameter(mixedAliasFor(property), ids)
	}

	/**
	 * Source for the By fields. It returns a map[id: fullName] for the persons
	 * that match the filter criteria.
	 *
	 * This closure is smart enough to avoid querying the database multiple times
	 * when all the staff is required.
	 */
	private static getMatchingStaff = { Project project, String filter, Map mixedFieldsInfo ->
		Map results = [:]
		boolean setAllStaff = false
		// Check if the filter is empty, in which case all the persons are required.
		if (!filter || !filter.trim()) {
			// Check if we already fetched all the persons
			if (mixedFieldsInfo["allStaff"]) {
				results = mixedFieldsInfo["allStaff"]
			// If they haven't been fetched, signal that they should be gathered and 'allStaff' set.
			} else {
				setAllStaff = true
			}
		}

		// Check if the query needs to be executed
		if (!results) {
			List<Person> individuals = projectService.getAssociatedStaffByName(project, filter)
			for (Person person : individuals) {
				results[person.id.toString()] = person.toString()
			}
		}

		// If the flag is set, update the mixedFieldsInfo to avoid executing the same query a second time.
		if (setAllStaff) {
			mixedFieldsInfo["allStaff"] = results
		}

		return results
	}


	/**
	 * Mixed fields are those, such as startupBy, shutdownBy and testingBy that may
	 * contain references to different things (teams, staff, other properties, etc).
	 *
	 * In this map you should specify the field and define a closure to be executed
	 * in order to fetch additional records (like persons) and the closure that
	 * is going to be executed to do the actual replacement/transformation.
	 */
	private static final Map mixedFields = [
			'AE.shutdownBy': [source: getMatchingStaff,
			                  injectWhereParams: injectByPropertyWhereParams,
			                  injectWhereClause: injectByPropertyWhereClause ,
			                  transform: transformByField,
			                  postProcessAlias: "application_shutdownBy",
			                  mixedAlias: "mixedShutdownBy"
			],
			'AE.startupBy': [source: getMatchingStaff,
			                 injectWhereParams: injectByPropertyWhereParams,
			                 injectWhereClause: injectByPropertyWhereClause ,
			                 transform: transformByField,
			                 postProcessAlias: "application_startupBy",
			                 mixedAlias: "mixedStartupBy"
			],
			'AE.testingBy': [source: getMatchingStaff,
			                 injectWhereParams: injectByPropertyWhereParams,
			                 injectWhereClause: injectByPropertyWhereClause ,
			                 transform: transformByField,
			                 postProcessAlias: "application_testingBy",
			                 mixedAlias: "mixedTestingBy"
			]
	]

    private static final Map<String, Map> transformations = [
		'id'             : [property: 'AE.id', type: Long, namedParameter: 'id', join: ''],
		'assetClass'     : [property: 'str(AE.assetClass)', type: String, namedParameter: 'assetClass', join: ''],
		'moveBundle'     : [property: 'AE.moveBundle.name', type: String, namedParameter: 'moveBundleName', join: 'left outer join AE.moveBundle'],
		'project'        : [property: 'AE.project.description', type: String, namedParameter: 'projectDescription', join: 'left outer join AE.project'],
		'manufacturer'   : [property: 'AE.manufacturer.name', type: String, namedParameter: 'manufacturerName', join: 'left outer join AE.manufacturer'],
		'appOwner'       : [property: SqlUtil.personFullName('appOwner', 'AE'),
							type: String, namedParameter: 'appOwnerName',
							join: 'left outer join AE.appOwner',
							alias:'appOwner'],
		'sme'            : [property: SqlUtil.personFullName('sme', 'AE'),
							type: String,
							namedParameter: 'smeName',
							join: 'left outer join AE.sme',
							alias:'sme'],
		'sme2'           : [property: SqlUtil.personFullName('sme2', 'AE'),
							type: String,
							namedParameter: 'sme2Name',
							join: 'left outer join AE.sme2',
							alias:'sme2'],
		'model'          : [property: 'AE.model.modelName', type: String, namedParameter: 'modelModelName', join: 'left outer join AE.model'],
		'locationSource' : [property: 'AE.roomSource.location', type: String, namedParameter: 'sourceLocation', join: 'left outer join AE.roomSource'],
		'rackSource'     : [property: 'AE.rackSource.tag', type: String, namedParameter: 'sourceRack', join: 'left outer join AE.rackSource'],
		'roomSource'     : [property: 'AE.roomSource.roomName', type: String, namedParameter: 'roomSourceName', join: 'left outer join AE.roomSource'],
		'locationTarget' : [property: 'AE.roomTarget.location', type: String, namedParameter: 'targetLocation', join: 'left outer join AE.roomTarget'],
		'rackTarget'     : [property: 'AE.rackTarget.tag', type: String, namedParameter: 'targetRack', join: 'left outer join AE.rackTarget'],
		'roomTarget'     : [property: 'AE.roomTarget.roomName', type: String, namedParameter: 'roomTargetName', join: 'left outer join AE.roomTarget'],
		'targetRackPosition': [property: "AE.targetRackPosition", type: Integer, namedParameter: 'targetRackPosition', join: ""],
		'sourceRackPosition': [property: "AE.sourceRackPosition", type: Integer, namedParameter: 'sourceRackPosition', join: ""],
		'sourceBladePosition': [property: "AE.sourceBladePosition", type: Integer, namedParameter: 'sourceBladePosition', join: ""],
		'targetBladePosition': [property: "AE.targetBladePosition", type: Integer, namedParameter: 'targetBladePosition', join: ""],
		'scale' : [property: 'AE.scale', type: SizeScale, namedParameter: 'scale', join: ''],
		'size': [property: "AE.size", type: Integer, namedParameter: 'size', join: ""],
		'rateOfChange': [property: "AE.rateOfChange", type: Integer, namedParameter: 'rateOfChange', join: ""],
	    'sourceChassis': [property: "AE.sourceChassis.assetName", type: String, namedParameter: 'sourceChassis', join: 'left outer join AE.sourceChassis'],
		'targetChassis': [property: "AE.targetChassis.assetName", type: String, namedParameter: 'targetChassis', join: 'left outer join AE.targetChassis'],
		'lastUpdated': [property: "AE.lastUpdated", type: Date, namedParameter: 'lastUpdated', join: ''],
		'maintExpDate': [property: "AE.maintExpDate", type: Date, namedParameter: 'maintExpDate', join: ''],
		'purchaseDate': [property: "AE.purchaseDate", type: Date, namedParameter: 'purchaseDate', join: ''],
		'retireDate': [property: "AE.retireDate", type: Date, namedParameter: 'retireDate', join: ''],
		'tagAssets': [
			property: """
				CONCAT(
					'[',
					if(
						TA.id,
						group_concat(
							json_object('id', TA.id, 'tagId', T.id, 'name', T.name, 'description', T.description, 'color', T.color)
						),
						''
					),
					']'
				)""",
			type: String,
			namedParameter: 'assetId',
			alias: 'tags',
			join: """
				left outer join AE.tagAssets TA
				left outer join TA.tag T
			""",
			whereProperty: 'AE.id',
			manyToManyQueries: [
			    AND : { String filter ->
				    List<String> numbers = filter.split('&')
				    List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
				    Long listSize = tagList.size() // Assign to long to avoid 'Integer cannot be casted to Long' error.
				    return [
				        query: "SELECT asset.id FROM TagAsset WHERE tag.id in (:tagList) GROUP BY asset.id HAVING count(*) = :tagListSize",
					    params: [
					        tagList: tagList,
						    tagListSize: listSize
					    ]
				    ]
			    },
				OR: { String filter ->
					List<String> numbers = filter.split('\\|')
					List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
					return [
					    query: "SELECT DISTINCT(ta.asset.id) FROM TagAsset ta WHERE tag.id in (:tagList)",
						params: [tagList: tagList]
					]

				}
			]
		],

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

	/**
	 * Validate if the name for the view is valid.
	 * @param command
	 * @return
	 */
	boolean validateUniqueName(DataviewNameValidationCommand command) {
		return validateUniqueName(command.name, command.dataViewId)
	}

	/**
	 * Check if a given DataView name is unique across project.
	 *
	 * This method will check that there's no other DataView for this project
	 * with the same name or, if there is, that they have the same id.
	 *
	 * @param dataViewName
	 * @param dataViewId
	 * @param project (optional)
	 * @return  true if the DataView name is unique for this project, false otherwise.
	 */
	boolean validateUniqueName(String dataViewName, Long dataViewId, Project project = null) {
		boolean isUnique = true
		if (!project) {
			project = securityService.userCurrentProject
		}

		// If the name is null don't validate, throw an exception.
		if (!dataViewName) {
			throw new InvalidParamException("The DataView name cannot be null.")
		}

		Dataview dataView = Dataview.where {
			name == dataViewName
			project == project
		}.find()

		if (dataView) {
			// If the ids don't match or params has no id, then it's a duplicate.
			if (dataViewId != dataView.id) {
				isUnique = false
			}
		}
		return isUnique
	}
}
