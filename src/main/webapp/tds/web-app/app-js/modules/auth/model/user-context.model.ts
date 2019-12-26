export class UserContextModel {
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
	defaultProject?: any;
}

export enum USER_CONTEXT_REQUEST {
	USER = 0,
	LICENSE = 1,
	PERMISSIONS = 2,
	NOTICES = 3
}
