package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
import net.transitionmanager.service.InvalidParamException

@CompileStatic
enum UserPreferenceEnum {
	App_Columns('Application List Custom Columns'),
	ARCH_GRAPH('Architecture Graph Settings'),
	Asset_Columns('Device List Custom Columns'),
	ASSET_JUST_PLANNING('Assets Just Planning'),
	ASSET_LIST_SIZE('Asset List Size'),
	ASSIGNED_GROUP('Assigned Group'),
	AUDIT_VIEW(),
	BULK_WARNING('Bulk Warning'),
	CONSOLE_TEAM_TYPE('Console Team Type'),
	CART_TRACKING_REFRESH('Cart Tracking Refresh Timer'),
	CURR_BUNDLE('Selected Bundle'),
	CURR_DT_FORMAT('Date Format'),
	CURR_POWER_TYPE('Room Power Mode'),
	CURR_PROJ('Current Project'),
	CURR_ROOM('Current Room'),
	CURR_TZ ('Time Zone'),
	DASHBOARD_REFRESH('Dashboard Refresh Timer'),
	Database_Columns('Database List Size'),
	DataScriptSize('ETL Designer Window Size'),
	Dep_Columns('Dependency List Custom Columns'),
	DEP_CONSOLE_COMPACT('depConsoleCompact'),
	DEP_GRAPH('Dependency Analyzer Settings'),
	DRAGGABLE_RACK('Draggable Rack'),
	HIGHLIGHT_TASKS('Task Hightlight'),
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
	JUST_REMAINING('Dependency Analyzer Just Remaining'),
	LAST_MANUFACTURER('Latest Manufacturer'),
	LAST_TYPE('Latest Device Type'),
	LEGEND_TWISTIE_STATE('Legend Twistie State'),
	MAX_ASSET_LIST('Asset List Size'),
	Model_Columns('Model List Custom Columns'),
	MOVE_BUNDLE('Selected Bundle'),
	MOVE_EVENT('Selected Event'),
	// Believed to unused
	MY_TASK('My Task Refresh Timer'),
	MYTASKS_MOVE_EVENT_ID,
	MYTASKS_REFRESH('My Tasks Refresh Timer'),
	PARTY_GROUP('Company ID'),
	Physical_Columns('Physical Servers Custom Columns'),
	PMO_COLUMN1('PMO Column 1 Filter'),
	PMO_COLUMN2('PMO Column 2 Filter'),
	PMO_COLUMN3('PMO Column 3 Filter'),
	PMO_COLUMN4('PMO Column 4 Filter'),
	PRINT_LABEL_QUANTITY('Print Label Quantity'),
	PRINTER_NAME('Selected Printer'),
	ROOM_TABLE_SHOW_ALL('roomTableShowAll'),
	SHOW_ADD_ICONS('Rack Add Icons'),
	SHOW_ALL_ASSET_TASKS('Asset Views Show All Tasks'),
	SHOW_ASSIGNED_STAFF('ShowAssignedStaff'),
	SHOW_CLIENT_STAFF('ShowClientStaff'),
	// Believe to not being used ---
	STAFFING_LOCATION('Default Project Staffing Location'),
	STAFFING_PHASES('Default Project Staffing Phase'),
	STAFFING_ROLE('Default Project Staffing Role'),
	STAFFING_SCALE('Default Project Staffing Scale'),
	// ---
	START_PAGE('Welcome Page'),
	SUPER_CONSOLE_REFRESH('Console Refresh Time'),
	Storage_Columns('Storage List Custom Columns'),
	Task_Columns('Task Manager Custom Columns'),
	TASK_CREATE_CATEGORY('Task Create Default Category'),
	TASK_CREATE_EVENT('Task Create Default Event'),
	TASK_CREATE_STATUS('Task Create Default Status'),
	TASK_LIST_SIZE('Task List Size'),
	TASKMGR_REFRESH('Task Manager Refresh Timer'),
	VIEW_UNPUBLISHED('Task View Unpublished'),
	EVENTDB_REFRESH('RefreshEventDB'),
	TASKGRAPH_REFRESH('Task Graph Refresh Timer'),
	TIMELINE_REFRESH('Timeline Graph Refresh Timer'),
	VIEW_MANAGER_DEFAULT_SORT('View Manager Default Sort')

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
			preferenceString == it.name()
		}
	}
}
