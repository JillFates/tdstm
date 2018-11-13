package net.transitionmanager.search

import com.tds.asset.AssetComment
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang3.BooleanUtils
import org.springframework.util.StringUtils

class AssetCommentQueryBuilder {

	/**
	 * Some fields will require additional joins. This list keep track of all
	 * additional tables that need to be joined.
	 */
	private List<String> joinTables

	/**
	 * List of all the expressions that need to be used for building the WHERE clause.
	 */
	private List<String> whereClauses

	/**
	 * Map with the parameters for the query.
	 */
	Map whereParams

	/**
	 * Map with the params map used when calling this method.
	 */
	Map requestParams

	/**
	 * Current project.
	 */
	Project project

	/**
	 * Query for retrieving all matching tasks.
	 */
	String query

	/**
	 * Query for retrieving the total number of records matching the filtering criteria. This needs to be done in
	 * a second query since the master query will contain pagination info and, unlike Criteria, we don't have
	 * access to the totalCount of records.
	 */
	String countQuery

	/**
	 * Sorting expression for the queries.
	 */
	String sorting

	/**
	 * Name of the field used for sorting.
	 */
	String sortIndex

	/**
	 * Sorting order (asc or desc)
	 */
	String sortOrder

	/**
	 * Flag to restrict only to published tasks.
	 */
	boolean viewUnpublished

	/**
	 * Constructor that takes all the parameters required for building the query for tasks.
	 *
	 * @param project - the project the tasks must belong to.
	 * @param params - map with the params sent in the request.
	 * @param sortIndex - column used for sorting results.
	 * @param sortOrder - asc/desc order for sorting the results.
	 * @param viewUnpublished - when set to true, this flag will limit the results to only those that are unpublished.
	 */
	AssetCommentQueryBuilder(Project project, Map params, String sortIndex, String sortOrder, boolean viewUnpublished) {
		this.project = project
		this.viewUnpublished = viewUnpublished
		this.sortIndex = sortIndex
		this.sortOrder = sortOrder
		requestParams = params
		whereClauses = ["ac.project = :project"]
		whereParams = ['project': project]
		joinTables = ["AssetComment ac"]

		if (!params.containsKey("commentType")) {
			whereClauses << "ac.commentType = :commentType"
			whereParams['commentType'] = AssetCommentType.TASK
		}
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
			queryParams: whereParams
		]
	}

	/**
	 * Construct the query and count query by putting the 'from', 'where' and 'order by' parts together.
	 */
	private void buildQueryAndCount() {
		String join = joinTables.join("\n")
		String whereClause = whereClauses.join(" AND ")
		query = "SELECT ac FROM ${join} WHERE ${whereClause} ${sorting}"
		countQuery = "SELECT count(ac.id) FROM ${join} WHERE ${whereClause}"
	}


	/**
	 * Add an additional join for the field being processed based on the information provided in the map.
	 * @param fieldMap
	 */
	private void addJoinTable(Map fieldMap) {
		String join = fieldMap.join ?: 'LEFT JOIN'
		joinTables << "${join} ${fieldMap.joinTable}"
	}


	/**
	 * Process the parameters (fields) received in the request by
	 * constructing the corresponding clause for every parameter.
	 */
	private void processParameters() {
		requestParams.each{ String param, paramValue ->
			Map fieldInfo = fieldsInfoMap[param]
			if (paramValue != null && !StringUtil.isBlank(paramValue.toString()) && fieldInfo) {
				// Check if the field requires an additional join.
				if (fieldInfo.joinTable) {
					addJoinTable(fieldInfo)
				}
				// Add the where clause for this field.
				if (fieldInfo.containsKey('builder')) {
					fieldInfo['builder'](param, fieldInfo)
				}
			}
		}

		// If the viewUnpublished flags wasn't set, limit the query only to published tasks.
		if (!viewUnpublished) {
			whereClauses << "ac.isPublished = true"
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
	 * Create the where clause for handling time scale values.
	 */
	Closure timeScaleBuilder = { String field, Map fieldMap ->
		TimeScale timeScaleValue = TimeScale.asEnum(requestParams[field])
		if (timeScaleValue != null) {
			processField(field, fieldMap, '=', ":${field}", timeScaleValue)
		}
	}

	/**
	 * Handle cases where the resulting expression is 'field = someValue'
	 */
	Closure eqBuilder = { String field, Map fieldMap ->
		def value = requestParams[field]
		// Parse to Integer in case the the field is number and the parameter is a string.
		if (fieldMap['type'] == Integer) {
			value = NumberUtil.toInteger(value)
		}
		processField(field, fieldMap, '=', ":${field}", value)
	}

	/**
	 * Construct a clause 'field = true' or 'field = false'
	 */
	Closure boolEqBuilder = { String field, Map fieldMap ->
		Boolean value = BooleanUtils.toBoolean(requestParams[field])
		processField(field, fieldMap, '=', ":${field}", value)
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
	 * if justMyTasks was set to '1', use the current person to narrow down search results.
	 */
	Closure justMyTasksBuilder = { String field, Map fieldMap ->
		if (requestParams[field] == '1') {
			processField(field, fieldMap, '=', ":${field}", securityService.loadCurrentPerson())
		}
	}

	/**
	 * If the justRemaining is set to '1', then filter all tasks with a status other than completed.
	 */
	Closure justRemainingBuilder = { String field, Map fieldMap ->
		if (requestParams[field] == '1') {
			processField(field, fieldMap, '!=', ":${field}", AssetCommentStatus.COMPLETED)
		}
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
				whereClauses << "ac.dueDate IN < :filterToday"
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
				whereClauses << "ac.dueDate IN < :filterToday"
				whereParams['filterToday'] = today
				whereClauses << "ac.category in (:planningCategories)"
				whereParams['planningCategories'] = AssetComment.planningCategories
				break
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
	 * Map with the information/logic required for dealing with each field.
	 *
	 * NOTE: Please, don't use .withDefault. This object will receive
	 * the request's param map, which contains keys other than valid fields.
	 */
	Map fieldsInfoMap = [
		'actStart':             [property: 'ac.actStart', builder: likeBuilder, type: Date],
		'assetName':            [property: 'ac.assetEntity.assetName', builder: likeBuilder],
		'assetType':            [property: 'ac.assetEntity.assetType', builder: likeBuilder],
		'assignedTo':           [property: SqlUtil.personFullName('assignedTo','ac'), builder: likeBuilder],
		'attribute':            [property: 'ac.attribute', builder: likeBuilder],
		'autogenerated':        [property: 'ac.autogenerated', builder: eqBuilder],
		'bundle':               [property: 'bundle.name', builder: likeBuilder, joinTable: 'ac.assetEntity.moveBundle bundle'],
		'category':             [property: 'ac.category', builder: likeBuilder],
		'comment':              [property: 'ac.comment', builder: likeBuilder],
		'commentType':          [property: 'ac.commentType', builder: likeBuilder],
		'createdBy':            [property: SqlUtil.personFullName('createdBy', 'ac'), builder: likeBuilder],
		'dateCreated':          [property: 'ac.dateCreated', builder: likeBuilder, type: Date],
		'dateResolved':         [property: 'ac.dateResolved', builder: likeBuilder, type: Date],
		'displayOption':        [property: 'ac.displayOption', builder: likeBuilder],
		'dueDate':              [property: 'ac.dueDate', builder: likeBuilder, type: Date],
		'durationScale':        [property: 'ac.durationScale', builder: timeScaleBuilder],
		'duration':             [property: 'ac.duration', builder: likeBuilder],
		'estStart':             [property: 'ac.estStart', builder: likeBuilder, type: Date],
		'estFinish':            [property: 'ac.estFinish', builder: likeBuilder, type: Date],
		'event':                [property: 'ac.moveEvent.name', builder: likeBuilder, joinTable: 'ac.moveEvent'],
		'filter':               [builder: filterBuilder],
		'hardAssigned':         [property: 'ac.hardAssigned', builder: eqBuilder],
		'instructionsLink':     [property: 'ac.instructionsLink', builder: likeBuilder],
		'isPublished':          [property: 'ac.isPublished', builder: boolEqBuilder],
		'isResolved':           [property: 'ac.dateResolved', builder: zeroIsNullBuilder ],
		'hardAssigned':         [property: 'ac.hardAssigned', builder: eqBuilder, type: Integer],
		'justMyTasks':          [property: 'ac.assignedTo', builder: justMyTasksBuilder],
		'justRemaining':        [property: 'ac.status', builder: justRemainingBuilder],
		'moveEvent':            [property: 'ac.moveEvent.id', builder: moveEventBuilder, joinTable: 'ac.moveEvent'],
		'priority':             [property: 'ac.priority', builder: likeBuilder, type: Integer],
		'resolution':           [property: 'ac.resolution', builder: likeBuilder],
		'resolvedBy':           [property: SqlUtil.personFullName('resolvedBy', 'ac'), builder: likeBuilder],
		'role':                 [property: 'ac.role', builder: likeBuilder],
		'sendNotification':     [property: 'ac.sendNotification', builder: boolEqBuilder],
		'status':               [property: 'ac.status', builder: likeBuilder],
		'statusUpdated':        [property: 'ac.statusUpdated', builder: likeBuilder, type: Date],
		'taskSpec':             [property: 'ac.taskSpec', builder: eqBuilder, type: Integer],
		'taskNumber':           [property: 'ac.taskNumber', builder: likeBuilder, type: Integer],
		'workflowTransition':   [property: 'ac.workflowTransition.id', builder: eqBuilder, joinTable: 'ac.workflowTransition'],
	]

}