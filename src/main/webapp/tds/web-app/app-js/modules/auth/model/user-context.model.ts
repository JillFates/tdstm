export class UserContextModel {
	logged: false;
	bundle?: any;
	dateFormat?: string;
	event?: any;
	person?: any;
	project?: any;
	timezone?: string;
	user?: any;
	license?: any;
	permissions?: any[];
	buildVersion?: string;
	postNotices?: any;
	notices?: {
		redirectUrl?: string;
	};
	error?: string;
	alternativeProjects?: [];
	csrf?: CSRF;
	defaultProject?: any;
}

export class CSRF {
	tokenHeaderName: string;
	token: string;
}

export enum USER_CONTEXT_REQUEST {
	USER = 0,
	LICENSE = 1,
	PERMISSIONS = 2,
	NOTICES = 3
}
