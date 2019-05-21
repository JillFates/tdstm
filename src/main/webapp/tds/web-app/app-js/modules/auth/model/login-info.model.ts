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