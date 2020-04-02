import {FilterType} from 'tds-component-library';

export class CredentialColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text
			}, {
				label: 'Description',
				property: 'description',
				filterType: FilterType.text
			}, {
				label: 'Provider',
				property: 'provider.name',
				filterType: FilterType.text,
				width: 200
			}, {
				label: 'Environment',
				property: 'environment',
				filterType: FilterType.text,
				width: 130
			}, {
				label: 'Status',
				property: 'status',
				filterType: FilterType.text,
				width: 100
			}, {
				label: 'Auth Method',
				property: 'authMethod',
				filterType: FilterType.text,
				width: 130
			}, {
				label: 'Date Created',
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
				filterType: FilterType.date,
				width: 150
			}
		];
	}
}

export class CredentialModel {
	id?: number;
	name?: string;
	description?: string;
	provider?: {
		id?: number,
		name: string
	};
	environment?: string;
	status?: string;
	authMethod?: string;
	dateCreated?: Date;

	// Authentication Methods
	username?: string;
	password?: string;
	authenticationUrl?: string;
	renewTokenUrl?: string;
	terminateUrl?: string;
	requestMode?: REQUEST_MODE;

	// cookie auth Method
	httpMethod?: string;
	// Token / Cookie Session Name
	sessionName?: string;
	version?: number;
	validationExpression?: string;

	constructor() {
		this.name = '';
		this.description = '';
		this.provider = { id: null, name: '' };
		this.environment = '';
		this.status = '';
		this.authMethod = '';
		this.username = '';
		this.password = '';
		this.authenticationUrl = '';
		this.renewTokenUrl = '';
		this.terminateUrl = '';
		this.httpMethod = '';
		this.sessionName = '';
		this.validationExpression = '';
	}
}

export enum REQUEST_MODE {
	BASIC_AUTH = 'BASIC_AUTH',
	FORM_VARS = 'FORM_VARS'
}

export enum AUTH_METHODS {
	BASIC_AUTH = 'Basic Auth',
	COOKIE = 'Cookie Session',
	HEADER = 'Header Session',
	JWT = 'JSON Web Tokens'
}

export enum ENVIRONMENT {
	DEVELOPMENT = 'Development',
	OTHER = 'Other',
	PRODUCTION = 'Production',
	SANDBOX = 'Sandbox'
}

export enum HTTP_METHOD {
	POST = 'POST',
	GET = 'GET',
	PUT = 'PUT'
}

export enum CREDENTIAL_STATUS {
	ACTIVE = 'Active',
	INACTIVE = 'Inactive'
}
