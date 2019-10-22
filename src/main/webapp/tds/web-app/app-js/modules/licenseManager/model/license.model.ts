export class LicenseColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 52,
				locked: true,
			}, {
				label: 'Owner',
				property: 'owner.name',
				type: 'text',
				width: 160,
			}, {
				label: 'Website Name',
				property: 'websitename',
				type: 'text',
				width: 160,
			}, {
				label: 'Client Name',
				property: 'client.name',
				type: 'text',
				width: 160,
			}, {
				label: 'Project Name',
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
				width: 170,
			}, {
				label: 'Expiration',
				property: 'expirationDate',
				type: 'date',
				format: dateFormat,
				width: 170,
			}, {
				label: 'Reporting',
				property: 'collectMetrics',
				type: 'boolean',
				width: 110,
			}, {
				label: 'Environment',
				property: 'environment',
				type: 'text',
				width: 160,
			}
		];
	}
}

export class LicenseActivityColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Date',
				property: 'dateCreated',
				type: 'date',
				width: 80,
			}, {
				label: 'Whom',
				property: 'author.personName',
				type: 'text',
				width: 80,
			}, {
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 200
			},
		];
	}
}

export class RequestLicenseModel {
	email: string;
	environment?: LicenseEnvironment;
	project?: any;
	clientName?: string;
	specialInstruction?: string;
}

export class LicenseModel {
	id?: number;
	key?: string;
	name: string;
	description?: string;
	comment?: string;
	dateCreated?: Date;
	expirationDate?: Date;
	activationDate?: Date;
	status?: string;
	guid?: string;
	collectMetrics?: boolean;
	bannerMessage?: string;
	requestNote?: string;
	environment?: string;
	gracePeriodDays?: string;
	email?: string;
	requestDate?: string;
	method?: any;
	owner?: any;
	client?: any;
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