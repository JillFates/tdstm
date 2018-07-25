/**
 * Created by David Ontiveros on 18/05/2017.
 */

export const en_DICTIONARY: Object = {
	'GLOBAL': {
		'OK': 'Ok',
		'ADD': 'Add',
		'ACTION': 'Action',
		'ARCHIVE': 'Archive',
		'CANCEL': 'Cancel',
		'CREATE': 'Create',
		'CLOSE': 'Close',
		'DELETE': 'Delete',
		'EDIT': 'Edit',
		'FILTER': 'Filter',
		'LIST': 'List',
		'LEGEND': 'Legend',
		'LOAD': 'Load',
		'NAME': 'Name',
		'MERGE': 'Merge',
		'SAVE': 'Save',
		'SAVE_ALL': 'Save All',
		'SAVE_AS': 'Save As',
		'DEFAULT': 'Default',
		'SORT': 'Sort',
		'ADD_FAVORITES': 'Add to Favorites',
		'SHARE_WITH_USERS': 'Share with other users',
		'CLEAR_FILTERS': 'Clear filters',
		'FREEZE': 'Freeze',
		'CLEAR': 'Clear',
		'PLEASE_SELECT': 'Please Select..',
		'CONTINUE': 'Continue',
		'PROJECTS': 'Projects',
		'UPLOAD': 'Upload',
		'FETCH': 'Fetch',
		'CONFIRM': 'Confirm',
		'CONFIRMATION_PROMPT' : {
			'CONFIRMATION_REQUIRED': 'Confirmation Required',
			'UNSAVED_CHANGES_MESSAGE' : 'You have changes that have not been saved. Do you want to continue and lose those changes?',
			'DELETE_ITEM_CONFIRMATION' : 'Are you sure you want to delete this item?',
			'DELETE_ITEMS_CONFIRMATION' : 'Are you sure you want to delete these items?'
		}
	},
	'ASSETS': {
		'APPLICATION': 'Application',
		'DATABASE': 'Database',
		'DEVICE': 'Device',
		'STORAGE': 'Storage',
		'ASSETS': 'Assets',
		'COMMON': 'Common'
	},
	'ASSET_EXPLORER': {
		'ASSET_EXPLORER': 'View Manager',
		'CREATE': 'Create View',
		'EDIT': 'Edit View',
		'SHOW': 'Show View',
		'GRID': {
			'NO_RECORDS': 'No records available',
			'SCHEMA_CHANGE': 'New results, click Preview button to see them',
			'INITIAL_VALUE': 'Hit Preview to populate grid'
		},
		'DEPENDENCY_DETAIL': 'Dependency Detail',
		'INDEX': {
			'SAVED_VIEWS': 'Saved Views',
			'ACTION': 'Action',
			'FAVORITE': 'Favorite',
			'NAME': 'Name',
			'CREATED_BY': 'Created By',
			'CREATED_ON': 'Created On',
			'UPDATED_ON': 'Updated On',
			'SHARED': 'Shared',
			'SYSTEM': 'System',
		},
		'SYSTEM_VIEW': 'System View',
		'BULK_CHANGE' : {
			'TITLE' : 'Bulk Change',
			'EDIT': {
				'ADD_FIELD': 'Add Field',
				'AFFECTED_ASSETS': 'This action will affect {param1} Assets'
			}
		}
	},
	'ASSET_TAGS': {
		'TAG': 'Tag',
		'MANAGE_TAGS': 'Manage Tags',
		'CREATE_TAG': 'Create Tag',
		'TAG_LIST': {
			'TAG_MERGE': 'Tag Merge',
			'MERGE_INTO': 'Merge into',
			'REMOVE_CONFIRMATION': 'This Tag is removed from all linked records and will be deleted. There is no undo for this action.',
			'MERGE_CONFIRMATION': 'Confirm merging of Tags. There is no undo for this action.'
		}
	},
	'DATA_INGESTION': {
		'ADD_PARAMETER': 'Add Parameter',
		'API_ACTIONS': 'API Actions',
		'DATA_INGESTION': 'Data Ingestion',
		'DATA_SCRIPTS': 'DataScripts',
		'ETL_SCRIPTS': 'ETL Scripts',
		'DATA_VIEW': 'View',
		'DATA_SCRIPT': 'DataScript',
		'PROVIDERS': 'Providers',
		'CREATE_CREDENTIAL': 'Create Credential',
		'CREATE_DATA_SCRIPT': 'Create DataScript',
		'CREATE_ETL_SCRIPT': 'Create ETL Script',
		'CREATE_PROVIDER': 'Create Provider',
		'CREATE_ACTION': 'Create Action',
		'CREDENTIALS': 'Credentials',
		'SAMPLE_DATA_TITLE': 'Sample Data',
		'ETL_BUILDER_TITLE': 'DataScript Designer',
		'CONSOLE_TITLE': 'ETL Console',
		'ETL_BUILDER': {
			'TEST': 'Test',
			'CHECK_SYNTAX': 'Check Syntax',
			'LOAD_SAMPLE_DATA': 'Load Sample Data',
			'VIEW_CONSOLE': 'View Console'
		},
		'DATASCRIPT': {
			'DESIGNER': {
				'SAMPLE_DATA_PREVIEW': 'Sample Data Preview',
				'TRANSFORMED_DATA_PREVIEW': 'Transformed Data Preview',
				'SYNTAX_ERRORS': 'Syntax Errors',
				'ETL_BUILDER_TITLE': 'ETL Script Designer',
				'CONSOLE_TITLE': 'ETL Console',
				'SAMPLE_DATA_TITLE': 'Sample Data',
				'TEST': 'Test',
				'CHECK_SYNTAX': 'Check Syntax',
				'LOAD_SAMPLE_DATA': 'Load Sample Data',
				'VIEW_CONSOLE': 'View Console',
				'FILE_TYPE': 'File Type:',
				'CONTENT': 'Content:',
				'PASTE_CONTENT': 'Paste content (CSV, JSON, XML)',
				'UPLOAD_FILE': 'Upload file (Excel, CSV, JSON, XML)',
				'FETCH_DATA_FROM_WEBSERVICE': 'Fetch data from web service',
				'PRESENTLY_THERE_ARE_NO_DATASOURCES': 'Presently there are no data source actions defined.',
				'FIELD_WILL_BE_INITIALIZED': 'Field will be initialized with the value shown for new records or existing records where the field has no value.'
			}
		}
	},
	'FIELD_SETTINGS': {
		'ASSET_FIELD_SETTING': 'Asset Field Settings',
		'FIELD_NOT_LONGER_EXIST_ON_DOMAIN': 'Field not longer exist on domain',
		'ENTER_FIELD_NAME_FILTER': 'Enter field name to filter',
		'PROJECT_LIST': 'Project List',
		'CREATE_CUSTOM': 'Add Custom Field',
		'PLAN_METHODOLOGY_DELETE_WARNING': 'Field is used as Project Plan Methodology, it can\'t be deleted.',
		'MIN_MAX': {
			'MIN_LENGTH': 'Min Length',
			'MAX_LENGTH': 'Max Length',
			'MIN_LENGTH_ERROR': 'Value must be between 0 and {param1}',
			'MAX_LENGTH_ERROR': 'Value must be between {param1} and 255'
		}
	},
	'IMPORT_ASSETS': {
		'MANUAL_IMPORT': {
			'IMPORT_ASSETS_ETL': 'Import Assets (ETL)',
			'MANUAL_ASSET_IMPORT': 'Manual Asset Import',
			'FETCH_WITH_DATA_ACTION': 'Fetch with Data Action:',
			'FETCH_WITH_FILE_UPLOAD': 'Fetch with File Upload:',
			'CURRENTLY_USED': 'Currently used',
			'OR': 'or',
			'TRANSFORM_WITH_DATA_SCRIPT': 'Transform with ETL Script:',
			'LOAD_TRANSFORMED_DATA_INTO_IMPORT': 'Load transformed data into Import Batches:',
			'GOTO_MANAGE_ASSET_BATCHES': 'Go to Manage Asset Batches',
			'FETCH': 'Fetch',
			'TRANSFORM': 'Transform',
			'IMPORT': 'Import',
			'VIEW_DATA': 'View Data'
		}
	},
	'IMPORT_BATCH': {
		'MANAGE_LIST': 'Manage Dependency Batches',
		'DEPENDENCY_BATCH': 'Dependency Batch',
		'LIST': {
			'QUEUE_TO_BE_PROCESSED': 'Queue to be processed',
			'REMOVE_FROM_QUEUE': 'Remove from queue',
			'STOP_PROCESSING': 'Stop processing',
			'VIEW_ARCHIVED': 'View Archived',
			'UNARCHIVE': 'Unarchive',
			'STOP_BATCH_CONFIRMATION': 'Are you sure you want to stop running the batch process?',
			'ARCHIVE_ITEM_CONFIRMATION': 'Are you sure you want to archive this item?',
			'ARCHIVE_ITEMS_CONFIRMATION': 'Are you sure you want to archive these items?',
			'UNARCHIVE_ITEM_CONFIRMATION': 'Are you sure you want to unarchive this item?',
			'UNARCHIVE_ITEMS_CONFIRMATION': 'Are you sure you want to unarchive these items?'
		},
		'DETAIL': {
			'CLICK_TO_TOGGLE_IGNORE': 'Click to toggle selected records status to/from IGNORED state. Ignored records will not be posted to the database.',
			'CLICK_TO_IMMEDIATE_PROCESS' : 'Click to immediate attempt to post selected PENDING records to the the database. Ignored records will not be processed.\n'
		}
	},
	'TASK_MANAGER': {
		'CURRENTLY_LIST_OF_AVAILABLE_TASKS': 'Current list of available tasks',
		'CREATE': 'Create',
		'CREATE_TASK': 'Create Task',
		'TASK': 'Task',
		'TASK_MANAGER': 'Task Manager'
	},
	'NOTICE_MANAGER': {
		'ADMIN': 'Admin',
		'CREATE_NOTICE': 'Create Notice',
		'EDIT_NOTICE': 'Edit Notice',
		'NOTICE_ADMINISTRATION': 'Notice Administration',
		'NOTICE': 'Notice',
	},
	'PAGES': {
		'ERROR_TITLE': 'Oops! Something went wrong.',
		'ERROR_MESSAGE': 'The TransitionManager team will fix this as soon as possible.',
		'UNAUTHORIZED_TITLE': 'You don\'t have permission to view this page.',
		'UNAUTHORIZED_MESSAGE': 'Please contact the Project Manager to help you resolve this.',
		'NOT_FOUND_TITLE': 'Oops! Nothing Found.',
		'NOT_FOUND_MESSAGE': 'We cannot find what you are looking for. Perhaps the page is broken, or has been moved. Please contact the Project Manager to help you resolve this.'
	}
};
