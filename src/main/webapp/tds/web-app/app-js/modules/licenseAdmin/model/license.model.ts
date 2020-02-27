import { ColumnHeaderData, FilterType } from 'tds-component-library';

export class LicenseColumnModel {
	columns: Array<ColumnHeaderData>;

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Client',
				property: 'client.name',
				filterType: FilterType.text
			}, {
				label: 'Project',
				property: 'project.name',
				filterType: FilterType.text
			}, {
				label: 'Contact Email',
				property: 'email',
				filterType: FilterType.text
			}, {
				label: 'Status',
				property: 'licenseStatus',
				filterType: FilterType.text
			}, {
				label: 'Type',
				property: 'licenseType',
				filterType: FilterType.text
			}, {
				label: 'Method',
				property: 'method.name',
				filterType: FilterType.text
			}, {
				label: 'Server/Tokens',
				property: 'method.max',
				filterType: FilterType.text
			}, {
				label: 'Inception',
				property: 'activationDate',
				format: dateFormat,
				filterType: FilterType.date
			}, {
				label: 'Expiration',
				property: 'expirationDate',
				format: dateFormat,
				filterType: FilterType.date
			}, {
				label: 'Environment',
				property: 'licenseEnvironment',
				filterType: FilterType.text
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
