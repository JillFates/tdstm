package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils

@CompileStatic
enum UserPreferenceEnum {
	ASSET_LIST_SIZE('assetListSize'),
	ASSET_JUST_PLANNING('assetJustPlanning'),
	MAX_ASSET_LIST,
	VIEW_UNPUBLISHED('viewUnpublished'),
	LAST_MANUFACTURER('lastManufacturer'),
	SHOW_ALL_ASSET_TASKS('showAllAssetTasks'),
	JUST_REMAINING,
	MOVE_EVENT,
	MOVE_BUNDLE,
	TASKMGR_REFRESH,
	DEP_GRAPH('depGraph'),
	ARCH_GRAPH('archGraph'),
	LEGEND_TWISTIE_STATE('legendTwistieState'),
	CURR_PROJ,
	CURR_TZ,
	CURR_BUNDLE,
	CURR_DT_FORMAT,
	CURR_ROOM,
	START_PAGE,
	DASHBOARD_REFRESH,
	LAST_TYPE('lastType'),
	MYTASKS_MOVE_EVENT_ID,
	MYTASKS_REFRESH,
	PRINT_LABEL_QUANTITY('printLabelQuantity'),
	PRINTER_NAME,
	AUDIT_VIEW,
	HIGHLIGHT_TASKS('highlightTasks'),
	ROOM_TABLE_SHOW_ALL('roomTableShowAll'),
	DRAGGABLE_RACK('DraggableRack'),
	SHOW_ADD_ICONS('ShowAddIcons'),
	PARTY_GROUP('PARTYGROUP'),
	CURR_POWER_TYPE,
	SHOW_ASSIGNED_STAFF('ShowAssignedStaff'),
	SHOW_CLIENT_STAFF('ShowClientStaff'),
	STAFFING_ROLE('StaffingRole'),
	STAFFING_LOCATION('StaffingLocation'),
	STAFFING_PHASES('StaffingPhases'),
	STAFFING_SCALE('StaffingScale'),
	DEP_CONSOLE_COMPACT('depConsoleCompact'),
	ASSIGNED_GROUP('AssignedGroup'),
	TASK_EVENT,
	TASK_CATEGORY,
	TASK_STATUS,
	ImportApplication,
	ImportServer,
	ImportDatabase,
	ImportStorage,
	ImportDependency,
	ImportCabling,
	ImportComment,
	ImportRoom,
	ImportRack

	static final List<UserPreferenceEnum> importPreferenceKeys = [ImportApplication, ImportServer, ImportDatabase,
	                                                              ImportStorage, ImportDependency, ImportCabling,
	                                                              ImportComment].asImmutable()

	static final List<UserPreferenceEnum> exportPreferenceKeys = [ImportApplication, ImportServer, ImportDatabase,
	                                                              ImportStorage, ImportDependency, ImportRoom,
	                                                              ImportRack, ImportCabling, ImportComment].asImmutable()
	//Immutable
	static final Map<String, Object> sessionOnlyPreferences

	static {
		//Initializing Constants
		// a Pref can be an alias of another Pref like TASK_EVENT wich takes its default from MOVE_EVENT
		// Beware of Circular dependency
		Map sessionOnlyPrefsDefaults = [:]
		sessionOnlyPrefsDefaults[TASK_EVENT.toString()] =  MOVE_EVENT
		sessionOnlyPrefsDefaults[TASK_CATEGORY.toString()] = AssetCommentCategory.GENERAL
		sessionOnlyPrefsDefaults[TASK_STATUS.toString()] = AssetCommentStatus.READY

		//Set as Immutable constant
		sessionOnlyPreferences = sessionOnlyPrefsDefaults.asImmutable()
	}

	private final String value

	private UserPreferenceEnum() {
		this(null)
	}

	private UserPreferenceEnum(String value) {
		this.value = value
	}

	String value() {
		value ?: name()
	}

	String toString() { value() }

	static
	boolean isSessionOnlyPreference(String preference){
		return preference in sessionOnlyPreferences.keySet()
	}
}
