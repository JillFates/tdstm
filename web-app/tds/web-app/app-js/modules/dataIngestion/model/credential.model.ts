export class CredentialColumnModel {
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
				label: 'Description',
				property: 'description',
				type: 'text'
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 200
			}, {
				label: 'Credential Type',
				property: 'credentialType.name',
				type: 'text',
				width: 200
			}, {
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 100
			}, {
				label: 'Auth Method',
				property: 'authMethod.name',
				type: 'text',
				width: 200
			}, {
				label: 'Created',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 170
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
	credentialType?: {
		id?: number,
		name?: string
	};
	status?: string;
	authMethod?: string;
	dateCreated?: Date;

	// Authentication Methods
	username?: string;
	password?: string;
	authenticationTestURL?: string;
	// cookie auth Method
	cookieSessionMethod?: {
		httpMethod?: string;
		cookieName?: string;
	};

	constructor() {
		this.name = '';
		this.description = '';
		this.provider = { id: null, name: '' };
		this.credentialType = { id: null, name: ''};
		this.status = '';
		this.authMethod = '';
		this.username = '';
		this.password = '';
		this.authenticationTestURL = '';
		this.cookieSessionMethod = {
			httpMethod: '',
			cookieName: ''
		};
	}
}

export const AUTH_METHODS = ['HTTP_BASIC', 'HTTP_COOKIE'];