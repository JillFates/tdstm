package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
import net.transitionmanager.service.InvalidParamException

@CompileStatic
enum UserPreferenceEnum {
	App_Columns,
	ARCH_GRAPH('archGraph'),
	Asset_Columns,
	ASSET_JUST_PLANNING('assetJustPlanning'),
	ASSET_LIST_SIZE('assetListSize'),
	ASSIGNED_GROUP('AssignedGroup'),
	AUDIT_VIEW(),
	BULK_WARNING('Bulk Warning'),
	CONSOLE_TEAM_TYPE('Console Team Type'),
	CART_TRACKING_REFRESH('Cart Tracking Refresh Time'),
	CURR_BUNDLE,
	CURR_DT_FORMAT,
	CURR_POWER_TYPE('Power Type'),
	CURR_PROJ,
	CURR_ROOM,
	CURR_TZ ('Time Zone'),
	DASHBOARD_REFRESH('Dashboard Refresh Time'),
	Database_Columns,
	DataScriptSize,
	Dep_Columns,
	DEP_CONSOLE_COMPACT('depConsoleCompact'),
	DEP_GRAPH('depGraph'),
	DRAGGABLE_RACK('DraggableRack'),
	HIGHLIGHT_TASKS('highlightTasks'),
	ImportApplication,
	ImportBatchListSize,
    ImportBatchRecordsFilter,
	ImportCabling,
	ImportComment,
	ImportDatabase,
	ImportDependency,
	ImportRack,
	ImportRoom,
	ImportServer,
	ImportStorage,
	JUST_REMAINING,
	LAST_MANUFACTURER('lastManufacturer'),
	LAST_TYPE('lastType'),
	LEGEND_TWISTIE_STATE('legendTwistieState'),
	MAX_ASSET_LIST,
	Model_Columns,
	MOVE_BUNDLE,
	MOVE_EVENT,
	MY_TASK('My Task Refresh Time'),
	MYTASKS_MOVE_EVENT_ID,
	MYTASKS_REFRESH('RefreshMyTasks'),
	PARTY_GROUP('PARTYGROUP'),
	Physical_Columns,
	PMO_COLUMN1('PMO Column 1 Filter'),
	PMO_COLUMN2('PMO Column 2 Filter'),
	PMO_COLUMN3('PMO Column 3 Filter'),
	PMO_COLUMN4('PMO Column 4 Filter'),
	PRINT_LABEL_QUANTITY('printLabelQuantity'),
	PRINTER_NAME,
	ROOM_TABLE_SHOW_ALL('roomTableShowAll'),
	SHOW_ADD_ICONS('Rack Add Icons'),
	SHOW_ALL_ASSET_TASKS('showAllAssetTasks'),
	SHOW_ASSIGNED_STAFF('ShowAssignedStaff'),
	SHOW_CLIENT_STAFF('ShowClientStaff'),
	STAFFING_LOCATION('Default Project Staffing Location'),
	STAFFING_PHASES('Default Project Staffing Phase'),
	STAFFING_ROLE('Default Project Staffing Role'),
	STAFFING_SCALE('Default Project Staffing Scale'),
	START_PAGE('Welcome Page'),
	SUPER_CONSOLE_REFRESH('Console Refresh Time'),
	Storage_Columns,
	Task_Columns,
	TASK_CREATE_CATEGORY,
	TASK_CREATE_EVENT,
	TASK_CREATE_STATUS,
	TASK_LIST_SIZE('TaskListSize'),
	TASKMGR_REFRESH('RefreshTaskMgr'),
	VIEW_UNPUBLISHED('viewUnpublished'),
	EVENTDB_REFRESH('RefreshEventDB'),
	TASKGRAPH_REFRESH('RefreshTaskGraph'),
	TIMELINE_REFRESH('RefreshTimeline'),
	VIEW_MANAGER_DEFAULT_SORT('viewManagerDefaultSort')

	static final Map<UserPreferenceEnum, ?> DEFAULT_VALUES = [
			  (PRINT_LABEL_QUANTITY) : 2
	].asImmutable()

	static final List<UserPreferenceEnum> importPreferenceKeys = [ImportApplication, ImportServer, ImportDatabase,
	                                                              ImportStorage, ImportDependency, ImportCabling,
	                                                              ImportComment, DataScriptSize].asImmutable()

	static final List<UserPreferenceEnum> exportPreferenceKeys = [ImportApplication, ImportServer, ImportDatabase,
	                                                              ImportStorage, ImportDependency, ImportRoom,
	                                                              ImportRack, ImportCabling, ImportComment, DataScriptSize].asImmutable()

	static final List<UserPreferenceEnum> sessionOnlyPreferences = [
		TASK_CREATE_EVENT,
		TASK_CREATE_CATEGORY,
		TASK_CREATE_STATUS
	].asImmutable()

	private final String value

	private UserPreferenceEnum() {
		this(null)
	}

	private UserPreferenceEnum(String value) {
		this.value = value
	}

	/**
	 * Get preference value or name if no values is present or null
	 * @return
	 */
	String value() {
		value ?: name()
	}

	String toString() {
		value()
	}

	/**
	 * Get preference instance from name or value
	 * @param str - preference name or value
	 * @return
	 */
	static UserPreferenceEnum valueOfNameOrValue(String str) {
		UserPreferenceEnum userPreferenceEnum = values().find {
			it.name() == str || it.value() == str
		}
		if (userPreferenceEnum == null) {
			throw new InvalidParamException('UserPreferenceEnum name or value invalid: ' + str)
		}
		return userPreferenceEnum
	}

	/**
	 * Determine if a preference is session attachable only
	 * @param preferenceString
	 * @return
	 */
	static boolean isSessionOnlyPreference(String preferenceString){
		sessionOnlyPreferences.find {
			preferenceString == it.toString()
		}
	}
}
