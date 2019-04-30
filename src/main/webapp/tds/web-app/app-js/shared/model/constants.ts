/**
 * Use this when comparing String.indexOf() not found match. ie. myString.indexOf() === NOT_FOUND_INDEX.
 * @type {number}
 */
export const NOT_FOUND_INDEX = -1;
export const SEARCH_QUITE_PERIOD = 600;
export const LAST_VISITED_PAGE = 'LAST_VISITED_PAGE';
export const LAST_SELECTED_FOLDER = 'LAST_SELECTED_FOLDER';
export const PROMPT_DEFAULT_TITLE_KEY = 'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED';
export const PROMPT_DEFAULT_MESSAGE_KEY = 'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE';
export const PROMPT_DELETE_ITEM_CONFIRMATION = 'GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION';
export const PROMPT_DELETE_ITEMS_CONFIRMATION = 'GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEMS_CONFIRMATION';
export const PROMPT_CONFIRM = 'GLOBAL.CONFIRM';
export const PROMPT_CANCEL = 'GLOBAL.CANCEL';
export const LOADER_IDLE_PERIOD = 150;
export const PROGRESSBAR_INTERVAL_TIME = 1 * 1000; // Seconds
// Pagination
export const GRID_DEFAULT_PAGINATION_OPTIONS = [25, 50, 100, 250, 500, 1000];
export const GRID_DEFAULT_PAGE_SIZE = 25;

// KENDO UPLOAD FILE
export const REMOVE_FILENAME_PARAM = 'filename';
export const FILE_UPLOAD_REMOVE_URL = 'removeUrl'
export const SAVE_FILENAME_PARAM = 'file';
export const FILE_UPLOAD_SAVE_URL = 'saveUrl'
export const FILE_UPLOAD_TYPE_PARAM = 'uploadType';
export const ETL_SCRIPT_FILE_UPLOAD_TYPE = 'ETLScript';
export const ASSET_IMPORT_FILE_UPLOAD_TYPE = 'assetImport';

export const NULL_OBJECT_LABEL = '(null)';
export const DEFAULT_ENABLE_ESC = false;

// Intervals
export enum INTERVAL {
	SECONDS = 'Seconds',
	MINUTES = 'Minutes',
	HOURS = 'Hours'
}

export const INTERVALS = [INTERVAL.SECONDS, INTERVAL.MINUTES, INTERVAL.HOURS];

export enum KEYSTROKE {
	ENTER = 'Enter',
	TAB = 'Tab',
	SHIFT_RIGHT = 'ShiftRight',
	SHIFT_LEFT = 'ShiftLeft',
	ESCAPE = 'Escape'
};

export enum DIALOG_SIZE {
	SM = 'sm',
	MD = 'md',
	LG = 'lg',
	XLG = 'xlg',
	XXL = 'xxl'
};

export const ACTIVE_INACTIVE = ['Active', 'Inactive'];

export const ERROR_STATUS = 'error';

export enum DOMAIN {
	APPLICATION = 'APPLICATION',
	COMMON = 'COMMON',
	DATABASE = 'DATABASE',
	DEVICE = 'DEVICE',
	STORAGE = 'STORAGE',
	TASK = 'TASK'
};

export enum ModalType {
	VIEW,
	CREATE,
	EDIT
};

export const DEFAULT_DIALOG_SIZE = DIALOG_SIZE.MD;
export type MODAL_SIZE = DIALOG_SIZE.SM | DIALOG_SIZE.MD | DIALOG_SIZE.LG | DIALOG_SIZE.XLG | DIALOG_SIZE.XXL;

export enum CUSTOM_FIELD_TYPES {
	List = 'List',
	String = 'String',
	YesNo = 'YesNo',
	Date = 'Date',
	DateTime = 'DateTime',
	Number = 'Number'
}

export const YesNoList = [
	{value: false, name: 'No'},
	{value: true, name: 'Yes'}
];