export class ExportAssetModel {
	notices: [];
	buildVersion: string;
	sessionExpired: boolean;
	config: any = {};

	constructor() {
		this.notices = [];
		this.buildVersion = '';
		this.sessionExpired = false;
		this.config = {
			authorityList: [],
			authorityName: ''
		};
	}
}