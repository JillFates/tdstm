export class LoginInfoModel {
	notices: [];
	buildVersion: string;
	sessionExpired: boolean;
	config: any = {
		authorityList: []
	};

	constructor() {
		this.notices = [];
		this.buildVersion = '';
		this.sessionExpired = false;
		this.config = {
			authorityList: []
		};
	}
}

export interface ILoginModel {
	authority: string;
	username: string;
	password: string;
}

export enum AuthorityOptions {
	SELECT = 'select',
	PROMPT = 'prompt',
	HIDDEN = 'hidden',
	NA = 'na'
}