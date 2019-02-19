import { INTERVAL } from '../../../shared/model/constants';
import {CHECK_ACTION} from '../../../shared/components/check-action/model/check-action.model';
import {AgentMethodModel} from './agent.model';

export class APIActionColumnModel {
	columns: any[];

	constructor(dateFormat: string) {

		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 106,
				locked: true
			}, {
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 150,
				locked: true
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 180
			}, {
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 300
			}, {
				label: 'Method',
				property: 'agentMethod.name',
				type: 'text',
				width: 125
			}, {
				label: 'Data',
				property: 'producesData',
				type: 'boolean',
				width: 90
			}, {
				label: 'Default DataScript',
				property: 'defaultDataScript.name',
				type: 'text',
				width: 180
			}, {
				label: 'Created',
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
				width: 160
			}, {
				label: 'Last Updated',
				property: 'lastUpdated',
				type: 'date',
				format: dateFormat,
				width: 160
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
				width: 64,
				locked: true
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
export const EVENT_DEFAULT_TEXT = '// Put the task on hold and add a comment with the cause of the error\n task.error( response.error )';
export const EVENT_BEFORE_CALL_TEXT  = `// Setting Content Type, default 'application/json'
// request.config.setProperty('Content-Type', 'text/csv')

// Setting content type Accepted, default 'application/json'
// request.config.setProperty('Accept', 'application/xml;q=0.9')`;
export enum APIActionType {
	HTTP_API = 1,
	SCRIPT
}

export class APIActionModel {
	id?: number;
	name: string;
	actionType: APIActionType;
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

	constructor() {
		this.name = '';
		this.description = '';
		this.provider = {id: null, name: ''};
		this.dictionary = {id: null, name: ''};
		this.agentMethod = new AgentMethodModel();
		this.defaultDataScript = {id: null, name: ''};
		this.isPolling = false;
		this.producesData = false;
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

	public static createBasicReactions(apiActionModel: APIActionModel): void {
		apiActionModel.eventReactions = [];
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, true, EVENT_STATUS_TEXT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, true, EVENT_SUCCESS_TEXT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, true, EVENT_DEFAULT_TEXT));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, false, ''));
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
	fieldName?: string;
	value?: string;
	currentFieldList?: Array<any>;
	sourceFieldList?: Array<any>;
}

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
