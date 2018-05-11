/**
 * This is not longer use since the custom are created on fly
 * However, it has the structure of possible values for the Database Model
 * up to date 13/03/2018
 *
 * This can be also used to get the definition of an element, useful for some data being empty like Scale
 * that requires an structure to work with widgets, and so on.
 */
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
	asset: any;

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
		this.asset = {};
	}
}