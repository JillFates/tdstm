
export class ArchitecutreGraphPreferencesModel {
	assetId: number;
	assetName: string;
	levelsUp: number;
	levelsDown: number;
	assetClassesForSelect: any;
	dependencyStatus: [];
	dependencyType: [];
	assetTypes: {
		Application: any,
		Database: any,
		Server: any,
		VM: any,
		Files: any,
		Storage: any,
		Network: any,
		Other: any
	};
	defaultPrefs: any;
	graphPrefs: {
		levelsUp: number;
		levelsDown: number;
		showCycles: boolean;
		appLbl: boolean;
		labelOffset: number;
		assetClasses: string;
	};
	assetClassesForSelect2: [];
}
