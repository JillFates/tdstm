import { INTERVAL } from '../../../shared/model/constants';
import {CHECK_ACTION} from '../../../shared/components/check-action/model/check-action.model';

export class APIActionColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 108,
				locked: true
			}, {
				label: 'Name',
				property: 'name',
				type: 'text',
				locked: true
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 200
			}, {
				label: 'Description',
				property: 'description',
				type: 'text'
			}, {
				label: 'Method',
				property: 'agentMethod.name',
				type: 'text',
				width: 200
			}, {
				label: 'Data',
				property: 'producesData',
				type: 'boolean',
				width: 100
			}, {
				label: 'Default DataScript',
				property: 'defaultDataScript.name',
				type: 'text'
			}, {
				label: 'Created',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 170
			}, {
				label: 'Last Modified',
				property: 'lastModified',
				type: 'date',
				format: '{0:d}',
				width: 170
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
				width: 68,
				locked: true
			}, {
				label: 'Name',
				property: 'param',
				type: 'text',
				width: 210
			}, {
				label: 'Context',
				property: 'context.value',
				type: 'text',
				width: 160
			}, {
				label: 'Value',
				property: 'value',
				type: 'text',
				width: 265
			}, {
				label: 'Description',
				property: 'desc',
				type: 'text',
				width: 310
			}
		];
	}
}

export class APIActionModel {
	id?: number;
	name: string;
	description: string;
	agentMethod?: {
		id?: number,
		name?: string
	};
	agentClass?: {
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
	lastModified?: Date;
	credential?: {
		id?: number,
		name?: string
	};
	endpointUrl?: '';
	endpointPath?: '';
	eventReactions?: EventReaction[];
	version?: number;

	constructor() {
		this.name = '';
		this.description = '';
		this.provider = { id: null, name: '' };
		this.agentClass = { id: null, name: '' };
		this.agentMethod = { id: null, name: ''};
		this.defaultDataScript = {id: null, name: ''};
		this.isPolling = false;
		this.producesData = false;
		this.endpointUrl = '';
		this.endpointPath = '';
		this.polling =  {
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
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, true, '// Check the HTTP response code for a 200 OK \n if (response.status == SC_OK) { \n \t return SUCCESS \n } else { \n \t return ERROR \n}'));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, true, '// Update the task status that the task completed\n task.done()'));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, true, '// Put the task on hold and add a comment with the cause of the error\n task.error( response.error )'));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FAILED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.LAPSED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STALLED, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.PRE, false, ''));
		apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FINAL, false, ''));
	}

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
	param?: string;
	desc?: string;
	type?: string;
	context?: string;
	property?: string;
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
