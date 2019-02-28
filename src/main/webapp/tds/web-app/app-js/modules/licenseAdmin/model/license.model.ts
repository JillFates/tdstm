export class LicenseColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 108,
				locked: true
			}, {
				label: 'Client',
				property: 'client.name',
				type: 'text',
				width: 160,
			}, {
				label: 'Project',
				property: 'project.name',
				type: 'text',
				width: 160,
			}, {
				label: 'Contact Email',
				property: 'email',
				type: 'text',
				width: 160,
			}, {
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 160,
			}, {
				label: 'Type',
				property: 'type',
				type: 'text',
				width: 160,
			}, {
				label: 'Method',
				property: 'method.name',
				type: 'text',
				width: 160,
			}, {
				label: 'Server/Tokens',
				property: 'method.max',
				type: 'text',
				width: 160,
			}, {
				label: 'Inception',
				property: 'activationDate',
				type: 'date',
				format: dateFormat,
				width: 170
			}, {
				label: 'Expiration',
				property: 'expirationDate',
				type: 'date',
				format: dateFormat,
				width: 170
			}, {
				label: 'Environment',
				property: 'environment',
				type: 'text',
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
	ACTIVE = 'ACTIVE'
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