import { ColumnHeaderData, FilterType } from 'tds-component-library';

export class LicenseColumnModel {
	columns: Array<ColumnHeaderData>;

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Client',
				filterable: true,
				property: 'client.name',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Project',
				filterable: true,
				property: 'project.name',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Contact Email',
				filterable: true,
				property: 'email',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Status',
				filterable: true,
				property: 'status',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Type',
				filterable: true,
				property: 'type',
				type: 'text',
				filterType: FilterType.dropdown,
				dropdownData: [{ text: 'Single', value: 'SINGLE_PROJECT' }, { text: 'Global', value: 'MULTI_PROJECT' }],
				width: 160,
			}, {
				label: 'Method',
				filterable: true,
				property: 'method.name',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Server/Tokens',
				filterable: true,
				property: 'method.max',
				filterType: 'text',
				width: 160,
			}, {
				label: 'Inception',
				filterable: true,
				property: 'activationDate',
				filterType: 'date',
				format: dateFormat,
				width: 170
			}, {
				label: 'Expiration',
				filterable: true,
				property: 'expirationDate',
				filterType: 'date',
				format: dateFormat,
				width: 170
			}, {
				label: 'Environment',
				filterable: true,
				property: 'environment',
				filterType: 'text',
				width: 160,
			}
		];
	}
}

export class RequestLicenseModel {
	email: string;
	environment?: any;
	project?: any;
	clientName?: string;
	specialInstruction?: string;
}

export class LicenseModel {
	id?: number;
	key?: string;
	name: string;
	status?: string;
	description?: string;
	expirationDate?: string;
	activationDate?: string;
	comment?: string;
	email?: string;
	client?: any;
	dateCreated?: Date;
	method?: any;
	requestNote?: string;
	environment?: string;
	replaced?: string;
	project?: any;
}

export enum LicenseType {
	MULTI_PROJECT = 'MULTI_PROJECT',
	SINGLE_PROJECT = 'SINGLE_PROJECT'
}

export enum LicenseStatus {
	PENDING = 'PENDING',
	ACTIVE = 'ACTIVE',
	CORRUPT = 'CORRUPT'
}

export enum LicenseEnvironment {
	ENGINEERING = 'ENGINEERING',
	TRAINING = 'TRAINING',
	DEMO = 'DEMO',
	PRODUCTION = 'PRODUCTION'
}

export const MethodOptions = [
	{
		name: 'MAX_SERVERS',
		text: 'Servers'
	},
	{
		name: 'TOKEN',
		text: 'Tokens'
	},
	{
		name: 'CUSTOM',
		text: 'Custom'
	}
];
