export class UserContextModel {
	bundle: any;
	dateFormat: string;
	event: any;
	person: any;
	project: any;
	timezone: string;
	user: any;
	licenseEnabled: boolean;
	permissions: any[];
}

export enum USER_CONTEXT_REQUEST {
	USER_INFO = 0,
	LICENSE_ENABLED = 1,
	PERMISSIONS = 2
}