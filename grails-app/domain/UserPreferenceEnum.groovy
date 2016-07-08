enum UserPreferenceEnum {
	ASSET_LIST_SIZE("assetListSize"),
	ASSET_JUST_PLANNING("assetJustPlanning"),
	MAX_ASSET_LIST("MAX_ASSET_LIST"),
	VIEW_UNPUBLISHED("viewUnpublished"),
	LAST_MANUFACTURER("lastManufacturer"),
	SHOW_ALL_ASSET_TASKS("showAllAssetTasks"),
	JUST_REMAINING("JUST_REMAINING"),
	MOVE_EVENT("MOVE_EVENT"),
	MOVE_BUNDLE("MOVE_BUNDLE"),
	TASKMGR_REFRESH("TASKMGR_REFRESH"),
	DEP_GRAPH("depGraph"),
	ARCH_GRAPH('archGraph'),
	CURR_PROJ("CURR_PROJ"),
	CURR_TZ("CURR_TZ"),
	CURR_BUNDLE("CURR_BUNDLE"),
	CURR_DT_FORMAT("CURR_DT_FORMAT"),
	CURR_ROOM("CURR_ROOM"),
	START_PAGE("START_PAGE"),
	DASHBOARD_REFRESH("DASHBOARD_REFRESH"),
	LAST_TYPE("lastType"),
	MYTASKS_MOVE_EVENT_ID("MYTASKS_MOVE_EVENT_ID"),
	MYTASKS_REFRESH("MYTASKS_REFRESH"),
	PRINT_LABEL_QUANTITY("printLabelQuantity"),
	PRINTER_NAME("PRINTER_NAME"),
	AUDIT_VIEW("AUDIT_VIEW"),
	HIGHLIGHT_TASKS("highlightTasks"),
	ROOM_TABLE_SHOW_ALL("roomTableShowAll"),
	DRAGGABLE_RACK("DraggableRack"),
	SHOW_ADD_ICONS("ShowAddIcons"),
	PARTY_GROUP("PARTYGROUP"),
	CURR_POWER_TYPE("CURR_POWER_TYPE"),
	SHOW_ASSIGNED_STAFF("ShowAssignedStaff"),
	SHOW_CLIENT_STAFF("ShowClientStaff"),
	STAFFING_ROLE("StaffingRole"),
	STAFFING_LOCATION("StaffingLocation"),
	STAFFING_PHASES("StaffingPhases"),
	STAFFING_SCALE("StaffingScale"),
	DEP_CONSOLE_COMPACT('depConsoleCompact'),
	ASSIGNED_GROUP("AssignedGroup"),
	ImportApplication,
	ImportServer,
	ImportDatabase,
	ImportStorage,
	ImportDependency,
	ImportCabling,
	ImportComment,
	ImportRoom,
	ImportRack

///// SINGLETON FUNCTIONS ///////////////////////
	static getImportPreferenceKeys() {
		return [ImportApplication, ImportServer, ImportDatabase, ImportStorage, ImportDependency, ImportCabling, ImportComment]
	}

	static getExportPreferenceKeys() {
		return [ImportApplication,ImportServer,ImportDatabase,ImportStorage,ImportDependency,ImportRoom,ImportRack, ImportCabling,ImportComment]
	}

///// Instance ///////////////////////////////
	private final String value

	/**
	 * Base constructor for Enum
	 * @param s
	 * @param i
	 */
	private UserPreferenceEnum(String s, Integer i) {
		super(s, i)
	}

	/**
	 * Constructor to add value to the enum
	 * @param s
	 */
	private UserPreferenceEnum(String s) {
		value = s
	}

	public String value(){
		return value ?: name()
	}

	public String toString() {
		return this.value()
	}

}
