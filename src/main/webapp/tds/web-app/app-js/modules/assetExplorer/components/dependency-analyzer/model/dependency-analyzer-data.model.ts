
export class DependencyAnalyzerDataModel {

	asset: string;
	date: Date;
	dependencyType: [];
	dependencyConsole: {
		group: [],
		application: [],
		serversPhysical: [],
		serversVirtual: [],
		databases: [],
		storage: [],
		statusClass: []
	};
	dependencyStatus: [];
	assetDependency: {
		dataFlowDirection: string;
		comment: string;
		dataFlowFreq: string,
		type: string,
		status: string
	};
	planningBundles: [];
	allMoveBundles: [];
	planStatusOptions: [];
	isAssigned: boolean;
	depGrpCrt: {
		modifiedDate: string,
		modifiedBy: string,
		connectionTypes: [],
		statusTypes: []
	};
	compactPref: string;
	showTabs: any;
	tabName: any;
	groupId: any;
	assetName: any;
	tagIds: [];
	tagMatch: string;
}

export class DependencyBundleModel {
	appCount: number;
	serverCount: number;
	vmCount: number;
	dbCount: number;
	storageCount: number;
	statusClass: string;
}
