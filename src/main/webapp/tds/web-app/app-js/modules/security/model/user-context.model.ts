export class UserContextModel {
	bundle: any;
	dateFormat: string;
	event: any;
	person: any;
	project: any;
	timezone: string;
	user: any;
	licenseInfo: any;
	permissions: any[];
}

export enum USER_CONTEXT_REQUEST {
	USER_INFO = 0,
	LICENSE_INFO = 1,
	PERMISSIONS = 2
}