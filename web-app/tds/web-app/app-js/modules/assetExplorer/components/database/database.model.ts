import * as R from 'ramda';

export class DatabaseModel {
	assetName: string;
	description: string;
	dbFormat: string;
	supportType: string;
	environment: string;
	size: string;
	scale: {
		name: string
	};
	retireDate: string;
	moveBundle: string;
	rateOfChange: string;
	maintExpDate: string;
	planStatus: string;
	externalRefId: string;
	validation: string;

	constructor() {
		this.assetName = '';
		this.description = '';
		this.dbFormat = '';
		this.supportType = '';
		this.environment = '';
		this.size = '';
		this.scale = {
			name: ''
		};
		this.retireDate = '';
		this.moveBundle = '';
		this.rateOfChange = '';
		this.maintExpDate = '';
		this.planStatus = '';
		this.externalRefId = '';
		this.validation = '';
	}
}