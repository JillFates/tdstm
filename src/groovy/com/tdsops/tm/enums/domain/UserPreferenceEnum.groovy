package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
import net.transitionmanager.service.InvalidParamException

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
	TASK_LIST_SIZE('TaskListSize'),
	TASK_CREATE_EVENT,
	TASK_CREATE_CATEGORY,
	TASK_CREATE_STATUS,
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
