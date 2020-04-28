package net.transitionmanager.search

import com.tdssrc.grails.DateTimeFilterUtil
import grails.util.Pair
import net.transitionmanager.task.AssetComment
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.person.PersonService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.person.UserPreferenceService
import org.apache.commons.lang3.BooleanUtils

class TaskQueryBuilder {

	/**
	 * Some fields will require additional joins. This list keep track of all
	 * additional tables that need to be joined.
	 * Using a Set Collection to avoid duplicates
	 */
	private Set<String> joinTables

	/**
	 * List of all the expressions that need to be used for building the WHERE clause.
	 */
	private List<String> whereClauses

	/**
	 * Map with the parameters for the query.
	 */
	private Map whereParams

	/**
	 * Map with the params map used when calling this method.
	 */
	private Map requestParams

	/**
	 * Current project.
	 */
	private Project project

	/**
	 * Query for retrieving all matching tasks.
	 */
	private String query

	/**
	 * Query for retrieving the total number of records matching the filtering criteria. This needs to be done in
	 * a second query since the master query will contain pagination info and, unlike Criteria, we don't have
	 * access to the totalCount of records.
	 */
	private String countQuery

	/**
	 * Sorting expression for the queries.
	 */
	private String sorting

	/**
	 * Name of the field used for sorting.
	 */
	private String sortIndex

	/**
	 * Sorting order (asc or desc)
	 */
	private String sortOrder

	/**
	 * Flag that keeps track of invalid filter such as non-existent enum strings.
	 */
	private boolean invalidCriterion = false

	/**
	 * Constructor that takes all the parameters required for building the query for tasks.
	 *
	 * @param project - the project the tasks must belong to.
	 * @param params - map with the params sent in the request.
	 * @param sortIndex - column used for sorting results.
	 * @param sortOrder - asc/desc order for sorting the results.
	 * @param viewUnpublished - when set to true, this flag will limit the results to only those that are unpublished.
	 */
	TaskQueryBuilder(Project project, Map params, String sortIndex, String sortOrder) {
		this.project = project
		this.sortIndex = sortIndex
		this.sortOrder = sortOrder
		requestParams = params
		whereClauses = ["ac.project = :project"]
		whereParams = ['project': project]
		joinTables = ['AssetComment ac', 'LEFT JOIN ac.assetEntity'] // TM-17521: Preloaded Left Join of AssetEntity to avoid Task with NO Asset Associated
		// Make sure we query for Tasks only.
		whereClauses << "ac.commentType = :commentType"
		whereParams['commentType'] = AssetCommentType.TASK
	}

	/**
	 * Create the queries required for populating the Task Manager with tasks.
	 *
	 * @return a map with:
	 *  - the query for filtering tasks.
	 *  - the query for counting the total number of tasks.
	 *  - the parameters for both queries.
	 */
	Map buildQueries() {
		// Process the fields the user used for filtering.
		processParameters()
		// Add the sorting required for the query.
		addSorting()
		// Build the query for tasks and the count query.
		buildQueryAndCount()
		return [
			query: query,
			countQuery: countQuery,
			queryParams: whereParams,
			invalidCriterion: invalidCriterion
		]
	}

	/**
	 * Construct the query and count query by putting the 'from', 'where' and 'order by' parts together.
	 */
	private void buildQueryAndCount() {
		if (invalidCriterion) {
			whereParams = null
		} else {
			String join = joinTables.join("\n")
			String whereClause = whereClauses.join(" AND ")
			query = "SELECT ac FROM ${join} WHERE ${whereClause} ${sorting}"
			countQuery = "SELECT count(ac.id) FROM ${join} WHERE ${whereClause}"
		}
	}


	/**
	 * Add an additional join for the field being processed based on the information provided in the map.
	 * @param fieldMap
	 */
	private void addJoinTable(Map fieldMap) {
		String join = fieldMap.join ?: 'LEFT JOIN'
		// Changing to String to avoid GString Objects and avoid duplicates
		joinTables << "${join} ${fieldMap.joinTable}".toString()
	}


	/**
	 * Process the parameters (fields) received in the request by
	 * constructing the corresponding clause for every parameter.
	 */
	private void processParameters() {

		// Iterate over the filters provided by the user creating the corresponding where clause.
		for (param in requestParams.keySet()) {
			def paramValue = requestParams[param]
			Map fieldInfo = fieldsInfoMap[param]
			if (paramValue != null && !StringUtil.isBlank(paramValue.toString()) && fieldInfo) {
				// Check if the field requires an additional join.
				if (fieldInfo.joinTable) {
					addJoinTable(fieldInfo)
				}
				// Add the where clause for this field.
				if (fieldInfo.containsKey('builder')) {
					fieldInfo.builder.call(param, fieldInfo)
				}

				// If the filter was invalid, stop processing.
				if (invalidCriterion) {
					break
				}
			}
		}
	}

	/**
	 * Construct the expression that needs to be used for sorting the results.
	 */
	private void addSorting() {

		if (sortIndex && sortOrder) {
			Map<String, String> references = [
				"assetName": "assetEntity.assetName",
				"assetType": "assetEntity.assetType",
				"bundle": "assetEntity.moveBundle.name",
				"event": "moveEvent.name"
			]
			if (references[sortIndex]) {
				sortIndex = references[sortIndex]
			}
			sorting = "ORDER BY ac.${sortIndex} ${sortOrder}"
			// If no sorting information provided, use predefined sorting.
		} else {
			sorting = "ORDER BY ac.score desc, ac.taskNumber asc, ac.dueDate asc, ac.dateCreated desc"
		}
	}

	/**
	 * Process a field by constructing the corresponding 'where' clause and adding the parameter to the param map.
	 * @param field - the name of the field being processed.
	 * @param fieldMap - the map with the info for constructing the clause for the field being handled.
	 * @param operator - which operator is needed when building the clause ( =, !=, etc.).
	 * @param argument - named parameter for the clause.
	 * @param value - actual value for the argument.
	 */
	private void processField(String field, Map fieldMap, String operator, String argument, value) {
		String property = fieldMap['property']
		whereClauses << "${property} ${operator} ${argument}"
		whereParams[field] = value
	}

	/**
	 * Process the special case is the role field is of type 'NO_ROLE'.
	 * @param fieldMap - the map with the info for constructing the clause for the field being handled.
	 */
	private void processNoRoleField(Map fieldMap) {
		String property = fieldMap['property']
		whereClauses << "(${property} LIKE '' OR ${property} is null)"
	}

	/**
	 * Construct an expression 'field like %someValue%. This closure handles all sorts of data types
	 * that cannot be handled with criteria (dates, numbers, etc).
	 */
	Closure likeBuilder = { String field, Map fieldMap ->
		boolean needsCasting = fieldMap['type'] != String
		String property = fieldMap['property']
		String value = "%${requestParams[field]}%"
		if (needsCasting) {
			whereClauses << "str(${property}) LIKE :${field}"
			whereParams[field] = value
		} else {
			processField(field, fieldMap, 'LIKE', ":${field}",  value)
		}
	}

	/**
	 * Construct an expression for date / datetime fields using the date filtering expressions. If the filter is invalid
	 * then the builder will do nothing vs causing a user input error.
	 */
	Closure dateBuilder = { String field, Map fieldMap ->
		try {
			Pair<Date, Date> dateRange = DateTimeFilterUtil.parseUserEntry(requestParams[field])
			String property = fieldMap['property']
			String from = "${field}_FROM"
			String to = "${field}_TO"
			whereClauses << "$property between :$from and :$to"
			whereParams[from] = dateRange.getaValue()
			whereParams[to] = dateRange.getbValue()
		} catch (e) {
			// We will just exit because the filter was not yet parsible -- That can be implemented in the parseUserEntry instead of here...
			// TODO : Throw new InvalidGridFilterException when this is implemented
			invalidCriterion = true
		}
	}

	/**
	 * Construct an expression 'role like %someValue%.
	 */
	Closure roleLikeBuilder = { String field, Map fieldMap ->
		String value
		if (requestParams[field] == "NO_ROLE") {
		 // special case where we want to show all tasks without a role (ac.role is null or blank)
			processNoRoleField(fieldMap)
		} else {
			value = "%${requestParams[field]}%"
			processField(field, fieldMap, 'LIKE', ":${field}",  value)
		}
	}

	/**
	 * Create the where clause for handling time scale values.
	 */
	Closure timeScaleBuilder = { String field, Map fieldMap ->
		TimeScale timeScaleValue = TimeScale.fromLabel(requestParams[field].toUpperCase())
		if (timeScaleValue == null) {
			invalidCriterion = true
		} else {
			processField(field, fieldMap, '=', ":${field}", timeScaleValue)
		}
	}

	/**
	 * Handle cases where the resulting expression is 'field != someValue'
	 */
	Closure notEqIfSetBuilder = { String field, Map fieldMap ->
		if (StringUtil.toBoolean(requestParams[field])) {
			comparisonBuilder(field, fieldMap, '<>')
		}
	}

	/**
	 * Handle cases where the resulting expression is 'field = someValue'
	 */
	Closure eqBuilder = { String field, Map fieldMap ->
		comparisonBuilder(field, fieldMap, '=')
	}

	/**
	 * Implement common functionality for constructing '=' and '<>' expressions.
	 */
	Closure comparisonBuilder = { String field, Map fieldMap, String operator ->
		def value = fieldMap.containsKey('value') ? fieldMap['value'] : requestParams[field]
		// Parse to Integer in case the the field is number and the parameter is a string.
		if (fieldMap['type'] == Integer) {
			value = NumberUtil.toInteger(value)
		}
		if (value == null) {
			invalidCriterion = true
		} else {
			processField(field, fieldMap, operator, ":${field}", value)
		}
	}

	/**
	 * Construct a clause 'field = true' or 'field = false'
	 * If the filter value entered by the user can not be resolved as a boolean type then the criteria will just be
	 * ignored. This will allow the user to type T, tr, tru, or true, in order to get to a valid boolean string value
	 * without getting an error message in the UI.
	 */
	Closure boolEqBuilder = { String field, Map fieldMap ->
		Boolean value = StringUtil.toBoolean(requestParams[field])	// 1, 0, t, f, y, n, true, false, yes, no

		if (value == null) {
			invalidCriterion = true
		} else {
			processField(field, fieldMap, '=', ":${field}", value)
		}
	}

	/**
	 * Build a 'field is NULL' or 'field IS NOT NULL' clause depending upon the value for the field:
	 *  - 0: IS NULL
	 *  - 1: IS NOT NULL
	 */
	Closure zeroIsNullBuilder = { String field, Map fieldMap ->
		String property = fieldMap['property']
		boolean paramValue = BooleanUtils.toBoolean(requestParams[field], "1", "0")
		String operator = paramValue? "IS NOT NULL" : "IS NULL"
		whereClauses << "$property $operator"
	}

	/**
	 * Add a filter by MoveEvent if:
	 * - The user selected an event (the param is not null and something other than zero). If the selected
	 *      event is valid and different from the user preference, the preference is updated.
	 * - The parameter is null, in which case the user preference is used.
	 */
	Closure moveEventBuilder = { String field, Map fieldMap ->
		Long moveEventId
		String fieldValue = requestParams[field]
		println "moveEventBuilder -- field $field -- $fieldValue"
		if (fieldValue != null && fieldValue != '0') {
			moveEventId = NumberUtil.toPositiveLong(fieldValue)
			// If the parameter is null, used the user preference.
		} else {
			moveEventId = NumberUtil.toPositiveLong(userPreferenceService.getMoveEventId())
		}

		// Add the where clause and parameter if under one of the scenarios described in the JavaDoc for this method.
		if (moveEventId) {
			whereClauses << "ac.moveEvent.id = :moveEventId"
			whereParams['moveEventId'] = moveEventId
		}
	}

	/**
	 * Deal with the 'filter' parameter which includes different filters in the query
	 * dependencing on the value.
	 */
	Closure filterBuilder = { String field, Map fieldMap ->
		Date today = new Date().clearTime()
		switch (requestParams[field]) {
			case "dueOpenIssue":
				whereClauses << "ac.dueDate < :filterToday"
				whereParams['filterToday'] = today
				// 'break' intentionally omitted.

			case "openIssue" :
				whereClauses << "ac.category IN (:discoveryCategories)"
				whereParams['discoveryCategories'] = AssetComment.discoveryCategories
				break

			case "analysisIssue" :
				whereClauses << "ac.status = :filterStatusReady"
				whereParams['filterStatusReady'] = AssetCommentStatus.READY
				whereClauses << "ac.category in (:planningCategories)"
				whereParams['planningCategories'] = AssetComment.planningCategories
				break

			case "generalOverDue" :
				whereClauses << "ac.dueDate < :filterToday"
				whereParams['filterToday'] = today
				whereClauses << "ac.category in (:planningCategories)"
				whereParams['planningCategories'] = AssetComment.planningCategories
				break
		}
	}

	/**
	 * Closure that handles the viewUnpublished parameter. If it's not present or it's set to false,
	 * the results will be limited to published tasks.
	 */
	Closure viewUnpublishedBuilder = { String field, Map fieldMap ->
		Boolean viewUnpublished = StringUtil.toBoolean(requestParams[field])
		// If the viewUnpublished param is false or wasn't set, limit the query only to published tasks.
		if (!viewUnpublished) {
			whereClauses << "ac.isPublished = true"
		}
	}

	/**
	 * if justMyTasks was set to '1', use the current person to narrow down search results.
	 */
	Closure justMyTasksBuilder = { String field, Map fieldMap ->
		if (StringUtil.toBoolean(requestParams[field])) {
			Person person =  securityService.loadCurrentPerson()
			List<String> assignedTeams = personService.getPersonTeamCodes(person)
			List<String> clauses = []
			clauses.add('ac.assignedTo = :currentPerson')
			if (assignedTeams) {
				clauses.add('(ac.assignedTo IS NULL AND ac.role IN (:assignedTeams))')
				whereParams['assignedTeams'] = assignedTeams
			}
			whereClauses << "(${clauses.join(' OR ')})"
			whereParams['currentPerson'] = person
		}
	}

	/**
	 * If the given parameter is set, this closure will build an IN expression.
	 */
	Closure inListIfSet = { String field, Map fieldMap ->
		if (StringUtil.toBoolean(requestParams[field])) {
			String namedParameter = "${field}List"
			whereClauses << "${fieldMap['property']} in (:${namedParameter})"
			whereParams[namedParameter] = fieldMap['values']
		}
	}

	/**
	 * Return the SecurityService instance.
	 * @return securityService from the container
	 */
	private static SecurityService getSecurityService() {
		ApplicationContextHolder.getBean('securityService', SecurityService)
	}

	/**
	 * Return the userPreferenceService from the container.
	 * @return userPreferenceService
	 */
	private static UserPreferenceService getUserPreferenceService() {
		ApplicationContextHolder.getBean('userPreferenceService', UserPreferenceService)
	}

	/**
	 * Return the PersonService instance from the container.
	 * @return the personService bean from the container.
	 */
	private static PersonService getPersonService() {
		ApplicationContextHolder.getBean('personService', PersonService)
	}


	/**
	 * Map with the information/logic required for dealing with each field.
	 *
	 * NOTE: Please, don't use .withDefault. This object will receive
	 * the request's param map, which contains keys other than valid fields.
	 */
	final
	private Map fieldsInfoMap = [
		'actStart'          : [property: 'ac.actStart', builder: dateBuilder, type: Date],
		'actFinish'         : [property: 'ac.dateResolved', builder: dateBuilder, type: Date],
		'apiAction'         : [property: 'ac.apiAction.name', builder: likeBuilder],
		'assetName'         : [property: 'ac.assetEntity.assetName', builder: likeBuilder],
		'assetType'         : [property: 'ac.assetEntity.assetType', builder: likeBuilder],
		'assignedTo'        : [property: SqlUtil.personFullName('assignedTo', 'ac'), builder: likeBuilder],
		'attribute'         : [property: 'ac.attribute', builder: likeBuilder],
		'autogenerated'     : [property: 'ac.autogenerated', builder: eqBuilder],
		'bundle'            : [property: 'bundle.name', builder: likeBuilder, joinTable: 'ac.assetEntity.moveBundle bundle'],
		'category'          : [property: 'ac.category', builder: likeBuilder],
		'comment'           : [property: 'ac.comment', builder: likeBuilder],
		'createdBy'         : [property: SqlUtil.personFullName('createdBy', 'ac'), builder: likeBuilder],
		'dateCreated'       : [property: 'ac.dateCreated', builder: dateBuilder, type: Date],
		'dateResolved'      : [property: 'ac.dateResolved', builder: dateBuilder, type: Date],
		'dueDate'           : [property: 'ac.dueDate', builder: dateBuilder, type: Date],
		'durationScale'     : [property: 'ac.durationScale', builder: timeScaleBuilder],
		'duration'          : [property: 'ac.duration', builder: likeBuilder],
		'estStart'          : [property: 'ac.estStart', builder: dateBuilder, type: Date],
		'estFinish'         : [property: 'ac.estFinish', builder: dateBuilder, type: Date],
		'event'             : [property: 'ac.moveEvent.name', builder: likeBuilder, joinTable: 'ac.moveEvent'],
		'filter'            : [builder: filterBuilder],
		'hardAssigned'      : [property: 'ac.hardAssigned', builder: eqBuilder],
		'instructionsLink'  : [property: 'ac.instructionsLink', builder: likeBuilder],
		'isCriticalPath'    : [property: 'ac.isCriticalPath', builder: boolEqBuilder],
		'isPublished'       : [property: 'ac.isPublished', builder: boolEqBuilder],
		'isResolved'        : [property: 'ac.dateResolved', builder: zeroIsNullBuilder],
		'justActionable'    : [property: 'ac.status', builder: inListIfSet, values: AssetCommentStatus.ActionableStatusCodes],
		'justMyTasks'       : [builder: justMyTasksBuilder],
		'justRemaining'     : [property: 'ac.status', builder: notEqIfSetBuilder, value: AssetCommentStatus.COMPLETED],
		'lastUpdated'       : [property: 'ac.lastUpdated', builder: dateBuilder, type: Date],
		'latestFinish'      : [property: 'ac.lastUpdated', builder: dateBuilder, type: Date],
		'latestStart'       : [property: 'ac.lastUpdated', builder: dateBuilder, type: Date],
		'moveEvent'         : [property: 'ac.moveEvent.id', builder: moveEventBuilder, joinTable: 'ac.moveEvent'],
		'priority'          : [property: 'ac.priority', builder: likeBuilder, type: Integer],
		'role'              : [property: 'ac.role', builder: roleLikeBuilder],
		'sendNotification'  : [property: 'ac.sendNotification', builder: boolEqBuilder],
		'status'            : [property: 'ac.status', builder: likeBuilder],
		'statusUpdated'     : [property: 'ac.statusUpdated', builder: dateBuilder, type: Date],
		'percentageComplete': [property: 'ac.percentageComplete', builder: eqBuilder, type: Integer],
		'taskSpec'          : [property: 'ac.taskSpec', builder: eqBuilder, type: Integer],
		'taskNumber'        : [property: 'ac.taskNumber', builder: likeBuilder, type: Integer],
		'viewUnpublished'   : [builder: viewUnpublishedBuilder].asImmutable()
	]

}
