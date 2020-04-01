import {INTERVAL} from '../../../shared/model/constants';
import {CHECK_ACTION} from '../../../shared/components/check-action/model/check-action.model';
import {AgentMethodModel} from './agent.model';
import {FilterType} from 'tds-component-library';

export enum EventReactions {
	Status,
	Success,
	Default,
	Error,
	Failed,
	Lapsed,
	Stalled,
	BeforeInvocation,
	PostInvocation,
};

export class APIActionColumnModel {
	columns: any[];

	constructor(dateFormat: string) {

		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 186
			}, {
				label: 'Provider',
				property: 'provider.name',
				filterType: FilterType.text,
				width: 180
			}, {
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 300
			},
			{
				label: 'Type',
				property: 'actionType',
				filterType: FilterType.text,
				width: 125
			},
			{
				label: 'Method',
				property: 'dictionaryMethodName',
				filterType: FilterType.text,
				width: 125
			}, {
				label: 'Data',
				property: 'producesData',
				type: FilterType.boolean,
				width: 100
			}, {
				label: 'Default DataScript',
				property: 'defaultDataScript.name',
				filterType: FilterType.text,
				width: 180
			}, {
				label: 'Date Created',
				property: 'dateCreated',
				format: dateFormat,
				filterType: FilterType.date,
				width: 150
			}, {
				label: 'Last Updated',
				property: 'lastUpdated',
				format: dateFormat,
				filterType: FilterType.date,
				width: 150
			}
		];
	}
}

export class APIActionParameterColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 64
			},
			{
				label: 'custom',
				property: 'required',
				type: 'boolean',
				width: 42
			},
			{
				label: 'custom',
				property: 'readonly',
				type: 'boolean',
				width: 42
			},
			{
				label: 'Name',
				property: 'paramName',
				type: 'text',
				width: 198
			}, {
				label: 'Context',
				property: 'context.value',
				type: 'text',
				width: 140
			}, {
				label: 'Value',
				property: 'value',
				type: 'text',
				width: 218
			},
			{
				label: 'custom',
				property: 'encoded',
				type: 'boolean',
				width: 42
			},
			{
				label: 'Description',
				property: 'desc',
				type: 'text',
				width: 268
			}
		];
	}
}

export const EVENT_STATUS_TEXT = '// Check the HTTP response code for a 200 OK \n if (response.status == SC.OK) { \n \t return SUCCESS \n } else { \n \t return ERROR \n}';
export const EVENT_SUCCESS_TEXT = '// Update the task status that the task completed\n task.done()';
export const EVENT_DEFAULT_TEXT_SCRIPT = `// Put the task on hold and add a comment with the cause of the error
 task.error( response.stderr )
`;
export const EVENT_DEFAULT_TEXT_WEB_API = `// Put the task on hold and add a comment with the cause of the error
 task.error( response.error )

//If you are using TMD to run the Web API check the Error Event above,
//and use the commented out:
//task.error(response.stderr)
`;
export const EVENT_BEFORE_CALL_TEXT = `// Setting Content Type, default 'application/json'
// request.config.setProperty('Content-Type', 'text/csv')

// Setting content type Accepted, default 'application/json'
// request.config.setProperty('Accept', 'application/xml;q=0.9')`;
export const EVENT_DEFAULT_ERROR_SCRIPT = `// Put the task on hold and add a comment with the cause of the error
 task.error( response.stderr )`;
export const EVENT_DEFAULT_ERROR_WEB_API = `// Put the task on hold and add a comment with the cause of the error
 task.error( response.error )
//If you are using TMD to run the web API use this reaction script instead, of the default above.
//task.error( response.stderr )
`;

export enum APIActionType {
	HTTP_API = 1,
	SCRIPT
}

export class APIActionModel {
	id?: number;
	name: string;
	dictionaryMethodName?: string;
	tabActionType?: APIActionType;
	actionType?: any;
	commandLine?: string;
	isRemote?: boolean;
	script?: any;
	remoteInvocation?: boolean;
	debugEnabled?: boolean;
	description: string;
	agentMethod?: AgentMethodModel;
	httpMethod: string;
	dictionary?: {
		id?: number,
		name?: string
	};
	asyncQueue?: string;
	callbackMethod?: string;
	callbackMode?: string;
	methodParams?: string;
	isPolling?: boolean;
	polling?: {
		frequency?: {
			value: number;
			interval: string;
		};
		lapsedAfter?: {
			value: number;
			interval: string;
		};
		stalledAfter?: {
			value: number;
			interval: string;
		}
	};
	timeout?: number;
	provider?: {
		id?: number,
		name: string
	};
	defaultDataScript?: {
		id?: number,
		name?: string
	};
	producesData?: boolean;
	dateCreated?: Date;
	lastUpdated?: Date;
	credential?: {
		id?: number,
		name?: string
	};
	endpointUrl?: string;
	docUrl?: string;
	eventReactions?: EventReaction[];
	version?: number;
	remoteCredentialMethod?: any;
	providedCredential?: any;

	constructor() {
		this.name = '';
		this.description = '';
		this.provider = {id: null, name: ''};
		this.dictionary = {id: null, name: ''};
		this.agentMethod = new AgentMethodModel();
		this.defaultDataScript = {id: null, name: ''};
		this.isPolling = false;
		this.producesData = false;
		this.debugEnabled = false;
		this.endpointUrl = '';
		this.docUrl = '';
		this.polling = {
			frequency: {
				value: 0,
				interval: INTERVAL.SECONDS
			},
			lapsedAfter: {
				value: 0,
				interval: INTERVAL.MINUTES
			},
			stalledAfter: {
				value: 0,
				interval: INTERVAL.MINUTES
			}
		};
		APIActionModel.createBasicReactions(this);
	}

	/**
	 * Base upon the action type creates the corresponding basic reactions
	 * @param {APIActionModel} apiActionModel
	 * @param {boolean} isWebAPI
	 */
	public static createBasicReactions(apiActionModel: APIActionModel, isWebAPI = false): void {
		apiActionModel.eventReactions = [];
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, true, EVENT_STATUS_TEXT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, true, EVENT_SUCCESS_TEXT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, true, isWebAPI ? EVENT_DEFAULT_TEXT_WEB_API : EVENT_DEFAULT_TEXT_SCRIPT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, false, isWebAPI ? EVENT_DEFAULT_ERROR_WEB_API : EVENT_DEFAULT_ERROR_SCRIPT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FAILED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.LAPSED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STALLED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.PRE, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FINAL, false, ''));
	}

	/**
	 * TODO: I think this can be removed.
	 * @deprecated look at the api-action-view-edit.component.ts -> loadReactionScripts
	 * @param {APIActionModel} apiActionModel
	 * @param {string} reactionScriptsStringModel
	 */
	public static createReactions(apiActionModel: APIActionModel, reactionScriptsStringModel: string) {
		let reactions = JSON.parse(reactionScriptsStringModel);
		apiActionModel.eventReactions = [];
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, reactions['STATUS'] ? true : false, reactions['STATUS'] ? reactions['STATUS'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, reactions['SUCCESS'] ? true : false, reactions['SUCCESS'] ? reactions['SUCCESS'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, reactions['DEFAULT'] ? true : false, reactions['DEFAULT'] ? reactions['DEFAULT'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, reactions['ERROR'] ? true : false, reactions['ERROR'] ? reactions['ERROR'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FAILED, reactions['FAILED'] ? true : false, reactions['FAILED'] ? reactions['FAILED'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.LAPSED, reactions['LAPSED'] ? true : false, reactions['LAPSED'] ? reactions['LAPSED'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STALLED, reactions['STALLED'] ? true : false, reactions['STALLED'] ? reactions['STALLED'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.PRE, reactions['PRE'] ? true : false, reactions['PRE'] ? reactions['PRE'] : ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FINAL, reactions['FINAL'] ? true : false, reactions['FINAL'] ? reactions['FINAL'] : ''));
	}
}

export class EventReaction {
	type: EventReactionType;
	selected?: boolean;
	value?: string;
	open?: boolean;
	state?: CHECK_ACTION;
	error?: string;

	constructor(type: EventReactionType, selected: boolean, value: string) {
		this.type = type;
		this.selected = selected;
		this.value = value;
		this.open = true;
		this.state = CHECK_ACTION.UNKNOWN;
		this.error = '';
	}
}

export class APIActionParameterModel {
	paramName?: string;
	desc?: string;
	type?: string;
	context?: string;
	fieldName?: any;
	value?: string;
	currentFieldList?: Array<any>;
	sourceFieldList?: Array<any>;
}

export const Languages = {
	GROOVY_SCRIPT: 'Groovy',
	POWER_SHELL: 'PowerShell',
	UNIX_SHELL: 'Shell'
};

export enum EventReactionType {
	STATUS = 'STATUS',
	SUCCESS = 'SUCCESS',
	DEFAULT = 'DEFAULT',
	ERROR = 'ERROR',
	FAILED = 'FAILED',
	LAPSED = 'LAPSED',
	STALLED = 'STALLED',
	PRE = 'PRE',
	FINAL = 'FINAL'
};
