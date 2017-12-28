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
				label: 'Default Data Script',
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
				property: 'name',
				type: 'text',
				width: 180
			}, {
				label: 'Data Type',
				property: 'dataType',
				type: 'text',
				width: 110
			}, {
				label: 'Context',
				property: 'context.value',
				type: 'text',
				width: 140
			}, {
				label: 'value',
				property: 'value',
				type: 'text',
				width: 240
			}, {
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 277
			}
		];
	}
}

export class APIActionModel {
	id?: number;
	name: string;
	description: string;
	agentMethod?: {
		id?: string,
		name?: string
	};
	agentClass?: {
		id?: string,
		name?: string
	};
	asyncQueue?: string;
	callbackMethod?: string;
	callbackMode?: string;
	methodParams?: string;
	pollingInterval?: boolean;
	polling?: {
		frequency?: {
			value?: number;
			interval?: string;
		};
		lapsedAfter?: {
			value?: number;
			interval?: string;
		};
		stalledAfter?: {
			value?: number;
			interval?: string;
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
		id?: string,
		name?: string
	};
	eventReactions?: EventReaction[];
}

export class EventReaction {
	type: EventReactionType;
	selected?: boolean;
	value?: string;
	open?: boolean;
	valid?: boolean;
	error?: string;

	constructor(type: EventReactionType, selected: boolean, value: string) {
		this.type = type;
		this.selected = selected;
		this.value = value;
		this.open = true;
		this.valid = true;
		this.error = '';
	}
}

export class APIActionParameterModel {
	id?: number;
	name?: string;
	description?: string;
	dataType?: string;
	context?: ParameterContextModel;
	value?: string;
	field?: string;
	currentFieldList?: Array<any>;
}

export class ParameterContextModel {
	assetClass?: string;
	value?: string;
}

export enum EventReactionType {
	STATUS = 'status',
	SUCCESS = 'success',
	DEFAULT = 'default',
	ERROR = 'error',
	FAILED = 'failed',
	TIMEDOUT = 'timedout',
	LAPSED = 'lapsed',
	STALLED = 'stalled',
	PRE_API_CALL = 'preApiCall',
	FINALIZED_API_CALL = 'finalizedApiCall'
};