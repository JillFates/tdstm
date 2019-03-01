package com.tds.asset


import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.TaskBatch
import net.transitionmanager.domain.WorkflowTransition
import org.apache.commons.lang3.StringUtils

import static com.tdsops.tm.enums.domain.AssetCommentCategory.GENERAL
import static com.tdsops.tm.enums.domain.AssetCommentStatus.*
import static com.tdsops.tm.enums.domain.TimeScale.M

class AssetComment {

	String comment                  // This is also the title of an issue or task
	String commentType
	Integer mustVerify = 0          // Flag used in MoveTech to have the user verify an instruction

	// These three properties all relating the project
	AssetEntity assetEntity
	Project project
	MoveEvent moveEvent

	Date dateCreated
	Date lastUpdated
	Date statusUpdated              // Updated when the status changes so we can compute the elapsed time that a task is in a status

	Date dateResolved
	String resolution
	Person resolvedBy
	Person createdBy
	Person assignedTo               // FKA owner
	Integer hardAssigned = 0        // Flags a task that can ONLY be done by an individual TODO : constraint 1/0 default 0 type tinyint
	String commentCode
	String category = GENERAL
	String displayOption = 'U'      // Used in dashboard to control display of user entered test (comment) or a generic message
	String attribute = 'default'    // TODO : What is attribute used for?  See if we're using and remove if not
	String commentKey               // TODO : What is commentKey used for?  See if we're using and remove if not

	String status
	Date dueDate

	Integer duration = 0            // # of minutes/hours/days/weeks to perform task
	TimeScale durationScale = M     // Scale that duration represents m)inute, h)our, d)ay, w)eek
	Integer priority = 3            // An additional option to score the order that like tasks should be processed where 1=highest and 5=lowest

	Date estStart
	Date estFinish
	Date actStart
	Date constraintTime               // For tasks that have a constraint on the time that it can start or finish (typically used for start event or start testing)
	TimeConstraintType constraintType // The type of constraint for time (e.g. MSO-Must Start On, )
	// Date actFinish		// Alias of dateResolved

	Integer slack                     // Indicated the original or recalculated slack time that this task has based on other predecessors of successors of this task
	WorkflowTransition workflowTransition   // The transition that this task was cloned from
	Integer workflowOverride = 0      // Flag that the Transition values (duration) has been overridden
	String role                       // The team that will perform the task
	Integer taskNumber                // TODO : constraint type short int min 1, max ?, nullable
	Integer score                     // Derived property that calculates the weighted score for sorting on priority

	TaskBatch taskBatch               // The batch from which the task was generated
	Boolean sendNotification = false  // Whether or not a task will send email notifications
	Boolean isPublished = true        // Whether or not the task is visible to the average user
	Boolean autoGenerated = false     // Whether or not the task is auto generated by the system -  TODO : this will go away with the new Catalog functionality
	Integer recipe                    // The recipe id # that caused this task to be created - TODO : this will be replaced by the taskBatch property with Catalog functionality
	Integer taskSpec                  // The taskSpec id # within the recipe that caused this task to be created
	String instructionsLink
	Boolean durationLocked = false

	// If present is an API Action that will be invoked when a task goes to Started or Completed or user presses the Invoke
	ApiAction apiAction

	// The time that the API Action was invoked. Invocation can only occur if this property is null.
	Date apiActionInvokedAt

	// The time that the API Action invocation completed
	Date apiActionCompletedAt

	// Any settings for the API Action that will override the settings in the apiAction (stored as JSON)
	String apiActionSettings

	// The percentage of the action duration that has been completed (set by API call)
	Integer apiActionPercentDone = 0

	// The percentage of the task that has been completed (manually set by users)
	Integer taskPercentDone = 0

	static hasMany = [notes: CommentNote, taskDependencies: TaskDependency]

	// The belongsTo would delete both Tasks and Comments when deleting Assets with the delete method. However
	// when deleting an asset, asset references in Tasks should be nulled and Asset Comments should deleted. This is
	// handled in the AssetEntityService.deleteAsset appropriately. See TM-6847
	// static belongsTo = [assetEntity:AssetEntity]

	// TODO : Add custom validator for role that checks that the role is legit for "Staff : *" of RoleType

	// Grouping of the various categories
	static final List<String> preMoveCategories = AssetCommentCategory.preMoveCategories
	static final List<String> moveDayCategories = AssetCommentCategory.moveDayCategories
	static final List<String> postMoveCategories = AssetCommentCategory.postMoveCategories
	static final List<String> discoveryCategories = AssetCommentCategory.discoveryCategories
	static final List<String> planningCategories = AssetCommentCategory.planningCategories
	static final String AUTOMATIC_ROLE = 'AUTO'

	/* Transient properties for Task Generation. */
	Boolean tmpIsFunnellingTask
	Map tmpDefSucc
	Map tmpDefPred
	Boolean tmpHasSuccessorTaskFlag
	def tmpChainPeerTask
	List tmpAssociatedAssets
	Boolean isImported = false
	/* End transient properties for Task Generation.*/

	static constraints = {
		actStart nullable: true
		assetEntity nullable: true
		assignedTo nullable: true
		attribute nullable: true, size: 0..255
		autoGenerated nullable: true               // Note - can't have nullable:true and min:1, the category+taskNumber should be unique
		category blank: false, size: 0..64, inList: AssetCommentCategory.list
		comment size: 0..65535
		commentCode nullable: true, size: 0..255
		commentKey nullable: true, size: 0..255
		commentType blank: false, size: 0..11, inList: ['issue', 'instruction', 'comment']
		constraintTime nullable: true
		constraintType nullable: true
		createdBy nullable: true
		dateCreated nullable: true
		dateResolved nullable: true
		displayOption blank: false, size: 0..1, inList: ['G', 'U']  // Generic or User
		dueDate nullable: true
		durationScale inList: TimeScale.keys     // TODO: change duration to default to zero and min:1, need to coordinate with db update for existing data
		estFinish nullable: true
		estStart nullable: true
		hardAssigned nullable: true
		instructionsLink nullable: true, size: 0..255
		lastUpdated nullable: true
		moveEvent nullable: true
		mustVerify nullable: true
		priority nullable: true     /*,  range:1..5*/  // TODO : add constraint to priority
		recipe nullable: true
		resolution nullable: true
		resolvedBy nullable: true
		role nullable: true
		sendNotification nullable: true
		slack nullable: true
		status nullable: true, size: 0..9, inList: AssetCommentStatus.list // TODO: remove the blank/nullable constraint for status after testing
		statusUpdated nullable: true
		taskBatch nullable: true
		taskNumber nullable: true
		taskSpec nullable: true
		workflowOverride nullable: true            // TODO : add range to workflowOverride constraint
		workflowTransition nullable: true
		apiAction nullable: true
		apiActionInvokedAt nullable: true
		apiActionCompletedAt nullable: true
		apiActionSettings nullable: true
		score nullable: true
		apiActionPercentDone nullable: true, range: 0..100
		taskPercentDone nullable: false, range: 0..100
	}

	static mapping = {
		autoTimestamp false
		createdBy column: 'created_by'
		id column: 'asset_comment_id'
		resolvedBy column: 'resolved_by'
		// child lazy: false
		columns {
			comment sqltype: 'text'
			displayOption sqltype: 'char', length: 1
			duration sqltype: 'mediumint'
			durationScale sqltype: 'char', length: 1
			mustVerify sqltype: 'tinyint'
			priority sqltype: 'tinyint'
			resolution sqltype: 'text'
			taskNumber sqltype: 'shortint unsigned'
			workflowOverride sqltype: 'tinyint'
		}
		/*
			NOTE THAT THIS LOGIC IS DUPLICATED IN THE TaskService.getUserTasks method SO IT NEEDS TO BE MAINTAINED TOGETHER

			The objectives are sort the list descending in this order:
				- HOLD 900
					+ last updated factor ASC
				- DONE recently (60 seconds), to allow undo 800
					+ actual finish factor DESC
				- STARTED tasks     700
					- Hard assigned to user	+55
					- by the user	+50
					- + Est Start Factor to sort ASC
				- READY tasks		600
					- Hard assigned to user	+55
					- Assigned to user		+50
					- + Est Start factor to sort ASC
				- PENDING tasks		500
					- + Est Start factor to sort ASC
				- DONE tasks		200
					- Assigned to User	+50
					- + actual finish factor DESC
					- DONE by others	+0 + actual finish factor DESC
				- All other statuses ?
				- Task # DESC (handled outside the score)

			The inverse of Priority will be added to any score * 5 so that Priority tasks bubble up above hard assigned to user

			DON'T THINK THIS APPLIES ANY MORE - Category of Startup, Physical, Moveday, or Shutdown +10
			- If duedate exists and is older than today +5
			- Priority - Six (6) - <priority value> (so a priority of 5 will add 1 to the score and 1 adds 5)
		*/
		// TODO : JPM 11/2015 : TM-4249 Eliminate Timezone computation 'CONVERT_TZ(SUBTIME(NOW(),'00:01:00.0')....' below
		score formula: "CASE status \
			WHEN '$HOLD' THEN 900 \
			WHEN '$COMPLETED' THEN IF(status_updated >= SUBTIME(NOW(),'00:01:00.0'), 800, 200) + status_updated/NOW() \
			WHEN '$STARTED' THEN 700 + 1 - IFNULL(est_start,NOW())/NOW() \
			WHEN '$READY' THEN 600 + 1 - IFNULL(est_start,NOW())/NOW() \
			WHEN '$PENDING' THEN 500 + 1 - IFNULL(est_start,NOW())/NOW() \
			ELSE 0 END + \
			IF(role='$AUTOMATIC_ROLE',-100,0) + \
			(6 - priority) * 5"
	}

	static transients = ['actFinish', 'assetName', 'assignedToString', 'done', 'isImported', 'runbookTask',
	                     'tmpAssociatedAssets', 'tmpDefPred', 'tmpDefSucc', 'tmpHasSuccessorTaskFlag',
	                     'tmpIsFunnellingTask', 'isActionable']

	// TODO : need method to handle inserting new assetComment or updating so that the category+taskNumber is unique

	String getAssignedToString() {
		assignedTo?.toString()
	}

	String getAssetName() {
		assetEntity?.assetName
	}

	// The actFinish value is stored in the dateResolved column	so need setter/getter
	Date getActFinish() {
		dateResolved
	}

	void setActFinish(Date date) {
		setDateResolved(date)
	}

	boolean isDone() {
		status == COMPLETED
	}

	boolean isStarted() {
		status == STARTED
	}

	boolean isOnHold() {
		status == HOLD
	}

	boolean isRunbookTask() {
		moveDayCategories.contains(category)
	}

	void setDateResolved(Date date) {
		dateResolved = date
	}

	/**
	 * Returns true is the task/comment is resolved, that is to say,
	 * if the dateResolved is not null.
	 * @return  true if the task/comment is resolved, false otherwise.
	 */
	boolean isResolved() {
		dateResolved != null
	}

	/**
	 * Determines if the task is Automatic processed
	 * @return
	 */
	boolean isAutomatic(){
		AUTOMATIC_ROLE == role
	}

	/**
	 * Used to determine if the task has an action associated with it
	 * @return true if the task has an associated action
	 */
	boolean hasAction() {
		apiAction != null
	}

	/*
	 * Used to determine if the task action can be invoked locally
	 *
	 * Note that a user can Mark a task STARTED OR DONE an the action should be run.
	 * Automated tasks that turn to READY should invoke the action. If the Action is Async then the status
	 * will turn to STARTED otherwise marked DONE if successful.
	 * With both manual/user or automatic tasks, if the invocation fails the status should change to HOLD
	 *
	 * @return true if action can be invoked
	 */
	Boolean isActionInvocableLocally() {
		return hasAction() && !apiActionInvokedAt && !apiAction.isRemote && status in [READY, STARTED]
	}

	/*
	 * Used to determine if the task action can be invoked remotely
	 *
	 * Note that a user can Mark a task STARTED OR DONE an the action should be run.
	 * Automated tasks that turn to READY should invoke the action. If the Action is Async then the status
	 * will turn to STARTED otherwise marked DONE if successful.
	 * With both manual/user or automatic tasks, if the invocation fails the status should change to HOLD
	 *
	 * @return true if action can be invoked
	 */
	Boolean isActionInvocableRemotely() {
		return hasAction() && !apiActionInvokedAt && apiAction.isRemote && status in [READY, STARTED]
	}

	/*
	 * Returns the remaining duration until the tasks will be completed. This tasks into account the start time
	 * if the task is in progress otherwise returns the total duration
	 * @return duration in minutes
	 */
	Integer durationRemaining() {
		// TODO : implement durationRemaining
		duration
	}

	Integer durationInMinutes() {
		durationScale?.toMinutes(duration)
	}

	def beforeInsert = {
		if (!isImported) {
			dateCreated = TimeUtil.nowGMT()
			lastUpdated = dateCreated
		}
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
		// If the API Action changed, clear out related properties
		if (this.dirtyPropertyNames.contains('apiAction')) {
			apiActionCompletedAt = null
			apiActionInvokedAt = null
		}
		return true
	}

	String toString() {
		(taskNumber ? taskNumber.toString() + ':' : '') + StringUtils.left(comment, 25)
	}

   /**
    * isActionable - return indicator that the status of the task is Actionable
    */
	boolean isActionable() {
		!(status in [ COMPLETED, TERMINATED ])
	}

	/**
	 * This method generates a Map representation of the AssetComment including the most
	 * useful fields for a Task. commentType is not included.
	 * @return Map of the task attributes of the domain object
	 */
	Map taskToMap() {
		Map actionMap = null
		if (apiAction) {
			actionMap = [
			    id: apiAction.id,
				name: apiAction.name,
				isRemote: false,
				type: 'Api',
				invokedAt: apiActionInvokedAt,
				completedAt: apiActionCompletedAt
			]
		}

		Map assetMap = null
		if (assetEntity) {
			assetMap = [
			    id: assetEntity.id,
				name: assetEntity.assetName,
				class: assetEntity.assetClass.toString() ?: '',
				type: assetEntity.assetType ?: ''
			]
		}

		Map assignedMap = null
		if (assignedTo) {
			assignedMap = [
			    id: assignedTo.id,
				name: assignedTo.toString()
			]
		}
		return [
				id: id,
				taskNumber: taskNumber,
				title: comment,
				status: status,
				statusUpdated: statusUpdated,
			statusUpdatedElapsed: TimeUtil.ago(statusUpdated),
			lastUpdated: lastUpdated,
			lastUpdatedElapsed: TimeUtil.ago(lastUpdated),
			action: actionMap,
			asset: assetMap,
			assignedTo: assignedMap,
			category: category ?: '',
			dateCreated: dateCreated,
			hardAssigned: hardAssigned == 1,
			estDurationMinutes: durationInMinutes(),
			estStart: estStart,
			estFinish: estFinish,
			actStart: actStart,
			actFinish: actFinish,
			team: role ?: '',
			isPublished: isPublished,
			isActionInvocableLocally: isActionInvocableLocally(),
			isActionInvocableRemotey: isActionInvocableRemotely(),
			isAutomatic: isAutomatic(),
		]

	}

	// task Manager column header names and its labels
	static final Map<String, String> taskCustomizeFieldAndLabel = [
		actStart: 'Actual Start', assignedTo: 'Assigned To', category: 'Category', commentType: 'Comment Type',
		createdBy: 'Created By', dateCreated: 'Date Created', dateResolved: 'Date Resolved', displayOption: 'Display Option',
		duration: 'Duration', durationScale: 'Duration Scale', estStart: 'Estimated Start', estFinish: 'Estimated Finish', actFinish: 'Actual Finish',
		hardAssigned: 'Hard Assignment', isPublished: 'Is Published', lastUpdated: 'Last Updated', sendNotification: 'Send Notification',
		priority: 'Priority', resolution: 'Resolution', resolvedBy: 'Resolved By', role: 'Team',
		statusUpdated: 'Status Updated', assetName: 'Asset Name', assetType: 'Asset Type', event: 'Move Event',
		instructionsLink: 'Instructions Link', taskSpec: 'TaskSpec ID', bundle: 'Move Bundle'
	].asImmutable()

	Map toMap() {
		Map dataMap = [
				id: id,
				assetEntityId: assetEntity.id,
				assetName: assetEntity.assetName,
				assetType: assetEntity.assetType,
				createdBy: [id:createdBy.id, name: createdBy.toString()],
				comment: comment,
				category: category,
				lastUpdated: lastUpdated,
				dateCreated: dateCreated,
				dateResolved: dateResolved,
				assignedTo: assignedTo?.toString() ?: '',
				isResolved: dateResolved ? 'Yes' : 'No'
		]
		return dataMap
	}
}
