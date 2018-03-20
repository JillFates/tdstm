import {INTERVAL} from '../../../shared/model/constants';

export class AgentModel {
	id?: number;
	name?: string;
}

export class CredentialModel {
	id?: number;
	name?: string;
	provider?: {
		id?: number,
		name: string
	};
}

export class AgentMethodModel {
	id?: number;
	name?: string;
	description?: string;
	method?: string;
	params?: {};
	results?: {};
	// To Pre-populate
	url?: string;
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
	producesData?: boolean;
	methodParams?: Array<any>;

	constructor() {
		this.name = '';
		this.description = '';
		this.isPolling = false;
		this.producesData = false;
		this.url = '';
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
		this.methodParams = [];
	}
}