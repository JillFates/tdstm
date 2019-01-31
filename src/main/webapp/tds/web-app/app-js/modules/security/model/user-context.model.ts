export class UserContextModel {
	userInfo: Object;
	licenseEnabled: boolean;
	permissions: any[]
}

export enum USER_CONTEXT_REQUEST {
	USER_INFO = 0,
	LICENSE_ENABLED = 1,
	PERMISSIONS = 2
}