/**
 * Created by David Ontiveros on 18/05/2017.
 */

export const en_DICTIONARY: Object = {
	'GLOBAL': {
		'OK': 'Ok',
		'YES': 'Yes',
		'NO': 'No',
		'ADD': 'Add',
		'ADMIN': 'Admin',
		'ACTION': 'Action',
		'ARCHITECTURE_GRAPH': 'Arch Graph',
		'ACTIVE': 'Active',
		'ARCHIVE': 'Archive',
		'CANCEL': 'Cancel',
		'CONFIGURE': 'Configure',
		'CREATE': 'Create',
		'CLOSE': 'Close',
		'CLONE': 'Clone',
		'DELETE': 'Delete',
		'EDIT': 'Edit',
		'EXPORT': 'Export',
		'FILTER': 'Filter',
		'LIST': 'List',
		'LEGEND': 'Legend',
		'LOAD': 'Load',
		'NAME': 'Name',
		'MERGE': 'Merge',
		'REFRESH': 'Refresh',
		'SAVE': 'Save',
		'SAVE_ALL': 'Save All',
		'SAVE_AS': 'Save As',
		'UNDO': 'Undo',
		'DEFAULT': 'Default',
		'SHOW': 'Show',
		'SORT': 'Sort',
		'UPDATE': 'Update',
		'ADD_FAVORITES': 'Add to Favorites',
		'REMOVE_FAVORITES': 'Remove from Favorites',
		'SHARE_WITH_USERS': 'Share with other users',
		'CLEAR_FILTERS': 'Clear filters',
		'CLEAR_FILTER': 'Clear filter',
		'FREEZE': 'Freeze',
		'CLEAR': 'Clear',
		'PLEASE_SELECT': 'Please Select..',
		'PROJECT': 'Project',
		'PENDING': 'Pending',
		'CONTINUE': 'Continue',
		'PROJECTS': 'Projects',
		'UPLOAD': 'Upload',
		'FETCH': 'Fetch',
		'CONFIRM': 'Confirm',
		'CONFIRMATION_PROMPT' : {
			'CONFIRMATION_REQUIRED': 'Abandon Changes?',
			'UNSAVED_CHANGES_MESSAGE' : 'You have unsaved changes. Click Confirm to abandon your changes.',
			'DELETE_ITEM_CONFIRMATION' : 'Are you sure you want to delete this item?',
			'DELETE_ITEMS_CONFIRMATION' : 'Are you sure you want to delete these items?'
		},
		'SELECT_PLACEHOLDER': 'Select...',
		'ARTIFACTS': {
			'ARCHITECTURE_GRAPH': 'Arch Graph',
			'ASSET' : 'Asset',
			'ASSETS' : 'Assets',
			'COMMENT' : 'Comment',
			'COMMENTS' : 'Comments',
			'TASK' : 'Task',
			'TASKS' : 'Tasks',
			'VIEW' : 'View',
			'VIEWS' : 'Views'
		},
		'TOTAL': 'Total'
	},
	'ASSETS': {
		'APPLICATION': 'Application',
		'DATABASE': 'Database',
		'DEVICE': 'Device',
		'STORAGE': 'Storage',
		'ASSETS': 'Assets',
		'COMMON': 'Common',
		'SINGLE_NAME': 'Asset',
		'PLURAL_NAME': 'Assets',
		'COMMENTS': 'Comments'
	},
	'API_ACTION': {
		'CREATE_ACTION': 'Create Action',
		'API_ACTION': 'Action',
		'API_ACTIONS': 'Actions',
	},
	'ASSET_EXPLORER': {
		'ASSET_EXPLORER': 'Manage Views',
		'CREATE': 'Create View',
		'EDIT': 'Edit View',
		'SHOW': 'Show View',
		'CREATE_APPLICATION': 'Create App',
		'CREATE_DATABASE': 'Create DB',
		'CREATE_DEVICE': 'Create Device',
		'CREATE_STORAGE': 'Create Storage',
		'SHOW_COMMENTS': 'Show Comments',
		'SHOW_TASKS': 'Show Tasks',
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
				'EFFECTED_ASSETS': 'This action will affect {param1} {param2}',
				'CONFIRM_UPDATE': 'You are about to update {param1} {param2}. There is no undo for this action. Click confirm to update the {param3}, otherwise click Cancel',
				'EFFECTED_DEPENDENCIES': 'This action will affect {param1} {params2}'
			},
			'DELETE': {
				'CONFIRM_DELETE_ASSETS': 'You are about to delete {param1} {param2}. There is no undo for this action. Click confirm to delete the {param3}, otherwise click Cancel',
				'CONFIRM_DELETE_DEPENDENCIES': 'You are about to delete {param1} {param2}. There is no undo for this action. Click confirm to delete the {param3}, otherwise click Cancel'
			},
			'ACTIONS': {
				'ADD': 'Add to existing',
				'CLEAR': 'Clear field',
				'REPLACE': 'Replace with',
				'REMOVE': 'Remove these'
			}
		}
	},
	'ASSET_SUMMARY': {
		'ASSET_SUMMARY': 'Asset Summary',
		'SUMMARY': 'Summary'
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
	'CREDENTIAL': {
		'CREATE_CREDENTIAL': 'Create Credential',
		'CREDENTIAL': 'Credential',
		'CREDENTIALS': 'Credentials',
	},
	'DATA_INGESTION': {
		'ADD_PARAMETER': 'Add Parameter',
		'DATA_INGESTION': 'Data Ingestion',
		'DATA_SCRIPTS': 'DataScripts',
		'ETL_SCRIPTS': 'ETL Scripts',
		'DATA_VIEW': 'View',
		'DATA_SCRIPT': 'DataScript',
		'CREATE_DATA_SCRIPT': 'Create DataScript',
		'CREATE_ETL_SCRIPT': 'Create ETL Script',
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
				'PASTE_CONTENT': 'Paste content (CSV, JSON)',
				'UPLOAD_FILE': 'Upload file (Excel, CSV, JSON)',
				'FETCH_DATA_FROM_WEBSERVICE': 'Fetch data from web service',
				'PRESENTLY_THERE_ARE_NO_DATASOURCES': 'Presently there are no data source actions defined.',
				'FIELD_WILL_BE_INITIALIZED': 'Field will be initialized with the value shown for new records or existing records where the field has no value.'
			}
		},
		'PROJECT': 'Project'
	},
	'DEPENDENCIES': {
		'MENU_TITLE': 'Dependencies',
		'LIST_TITLE': 'Dependencies List',
		'SINGLE_NAME': 'Dependency',
		'PLURAL_NAME': 'Dependencies',
		'CONFIRM_DELETE_DEPENDENCY': 'Are you sure you would like to delete the dependency?'
	},
	'EVENT': {
		'EVENT': 'Event',
		'EVENTS': 'Events',
		'CREATE_EVENT': 'Create Event',
		'LIST': 'Event List',
		'TITLE_DASHBOARD': 'Event Dashboard',
		'DASHBOARD': 'Dashboard',
		'INCLUDE_UNPUBLISHED': 'Include Unpublished Tasks'
	},
	'REPORTS': {
		'REPORTS': 'Reports',
		'APPLICATION_CONFLICTS': 'Application Conflicts',
		'DATABASE_CONFLICTS': 'Database Conflicts',
		'REPORT': 'Report',
		'PRE_EVENT_CHECKLIST': 'Pre-Event Checklist',
		'TASK_REPORT': 'Task Report',
		'SERVER_CONFLICTS': 'Server Conflicts',
		'APPLICATION_EVENT_RESULTS': 'Application Event Results'
	},
	'PLANNING': {
		'PLANNING': 'Planning',
		'PRE_EVENT_CHECKLIST': 'Pre-Event Checklist',
		'BUNDLES': {
			'BUNDLES': 'Bundles',
			'LIST': 'Bundle List',
		}
	},
	'FIELD_SETTINGS': {
		'ASSET_FIELD_SETTING': 'Asset Field Settings',
		'FIELD_NO_LONGER_EXISTS_ON_DOMAIN': 'Field no longer exists on domain',
		'ENTER_FIELD_NAME_FILTER': 'Filter by field or label',
		'PROJECT': 'Project',
		'CREATE_CUSTOM': 'Add Custom Field',
		'PLAN_METHODOLOGY_DELETE_WARNING': 'Field is used as Project Plan Methodology, it can\'t be deleted.',
		'MIN_MAX': {
			'MIN_LENGTH': 'Min Length',
			'MAX_LENGTH': 'Max Length',
			'MIN_LENGTH_ERROR': 'Value must be between 0 and {param1}',
			'MAX_LENGTH_ERROR': 'Value must be between {param1} and 255'
		},
		'CLEAR_UNDERLAYING_DATA': 'Should underlying data be cleared for deleted custom field(s)?\n Yes to clear data and save changes or No to preserve data and save changes.'
	},
	'LICENSE': {
		'ADMIN': 'License Admin',
		'DEMO': 'Demo',
		'CREATE_LICENSE': 'Create License',
		'GLOBAL': 'Global',
		'IMPORT_LICENSE_REQUEST': 'Import License Request',
		'PASTE_LICENSE_BLOCK': 'Paste in license text block from emailed License Request and then click Import.',
		'SINGLE': 'Single',
		'MANAGER': 'License Manager',
		'ENGINEERING': 'Engineering',
		'REQUEST_NEW_LICENSE': 'Request New License',
		'LICENSE_REQUEST_COMPLETED': 'License Request Completed',
		'LICENSE_DETAIL': 'License Detail',
		'TRAINING': 'Training',
		'PRODUCTION': 'Production',
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
			'GOTO_MANAGE_ASSET_BATCHES': 'Manage Import Batches (ETL)',
			'FETCH': 'Fetch',
			'TRANSFORM': 'Transform',
			'IMPORT': 'Import',
			'VIEW_DATA': 'View Data'
		}
	},
	'IMPORT_BATCH': {
		'MANAGE_LIST': 'Manage Import Batches (ETL)',
		'IMPORT_BATCH': 'Import Batch',
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
	'NEWS': {
		'EDIT_NEWS': 'News Edit',
		'CREATE_NEWS': 'News Create'
	},
	'LOGIN': {
		'BACK_TO_LOGIN': 'Back to Login',
		'CHECK_YOUR_EMAIL': 'Check Your Email',
		'DOMAIN': 'Domain',
		'FORGOT_PASSWORD': 'Forgot Password',
		'LOGIN': 'Login',
		'PLEASE_SELECT': 'Please select',
		'SEND': 'Send',
		'START_YOUR_SESSION': 'Sign in to start your session',
		'SESSION_EXPIRED': 'Your session has expired. Please log in.',
		'PASSWORD_ASSISTANT': 'Password Assistant',
		'THANKS_FOR_REQUEST': 'Thank you for requesting a password reset. If your account is currently active,\n' +
			'\t\t\tyou will receive an email with a reset link. If you do not receive this email,\n' +
			'\t\t\tplease check your spam filter or contact your system administrator to confirm your account is still active.',
		'WE_WILL_SEND_EMAIL': 'We will send an e-mail to you that contains a link to a page where you can create new password.'
	},
	'NOTICE': {
		'ADMIN': 'Admin',
		'SHOW_NOTICE': 'Notice Detail',
		'CREATE_NOTICE': 'Create Notice',
		'EDIT_NOTICE': 'Notice Edit',
		'NOTICE_ADMINISTRATION': 'Notice Administration',
		'NOTICE': 'Notice',
		'NOTICES': 'Notices',
		'VIEW_HTML': 'View HTML',
		'AGREEMENT': 'By clicking Accept, you are agreeing to the terms above.',
		'DO_NOT_SHOW_AGAIN': 'Don\'t show again',
		'POST_NOTICES': 'Post Notices',
		'TOOLTIP_TITLE': 'Notice title',
		'TOOLTIP_NOTICE_TYPES': `Pre Login: Appear on Login Form
Post Login: Used for general notices after Login
Mandatory Acknowledgement: Appear post login and user must Accept otherwise will be logged out`,
		'TOOLTIP_ACTIVE': 'Notice will only appear when Active as long as it is within the Activation/Expiration date range if set',
		'TOOLTIP_ACTIVATION_DATE': 'When set the notice will not appear before the date set otherwise it will be shown',
		'TOOLTIP_EXPIRATION_DATE': 'When set the notice will stop being shown after the date',
		'TOOLTIP_LOCKED': 'Once a notice is locked the Message Text and Post Message Text fields will become permanently read-only and can not be unlocked',
		'TOOLTIP_SEQUENCE': 'Used to control the order of notices when 2 or more are being displayed where the lowest numbered notice is listed first',
		'TOOLTIP_MESSAGE': 'HTML text to display in main user dialog window',
		'TOOLTIP_POSTMESSAGE': 'Appears above the Accept button for Mandatory Acknowledgements'
	},
	'PAGES': {
		'ERROR_TITLE': 'Oops! Something went wrong.',
		'ERROR_MESSAGE': 'The TransitionManager team will fix this as soon as possible.',
		'UNAUTHORIZED_TITLE': 'You don\'t have permission to view this page.',
		'UNAUTHORIZED_MESSAGE': 'Please contact the Project Manager to help you resolve this.',
		'NOT_FOUND_TITLE': 'Oops! Nothing Found.',
		'NOT_FOUND_MESSAGE': 'We cannot find what you are looking for. Perhaps the page is broken, or has been moved. Please contact the Project Manager to help you resolve this.'
	},
	'PROVIDER': {
		'CREATE_PROVIDER': 'Create Provider',
		'PROVIDER': 'Provider',
		'PROVIDERS': 'Providers',
	},
	'TASK_MANAGER': {
		'CURRENTLY_LIST_OF_AVAILABLE_TASKS': 'Current list of available tasks',
		'CREATE': 'Create',
		'CREATE_TASK': 'Create Task',
		'TASK': 'Task',
		'TASK_MANAGER': 'Task Manager',
		'DELETE_TASK' : 'Are you sure you want to delete this task? There is no undo for this action',
		'CREATE_PREDECESSOR': 'Create new predecessor task',
		'CREATE_SUCCESSOR': 'Create new successor task',
		'EDIT': {
			'REQUIRED_FIELD': 'Field is required',
			'ERROR_DUPLICATE_ENTRIES': 'There are duplicate entries',
			'ERROR_DOUBLE_ASSIGNMENT': 'One or more tasks are assigned as both a Predecessor and Successor which is not allowed.',
			'SELECT_START_DATE': 'Select a start date',
			'SELECT_END_DATE': 'Select an end date'
		}
	}
};
