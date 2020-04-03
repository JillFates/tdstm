
export class DependencyAnalyzerDataModel {

	asset: string;
	date: Date;
	dependencyType: [];
	dependencyConsoleList: [];
	assetDependency: [];
	moveBundle: [];
	allMoveBundles: [];
	planStatusOptions: [];
	dependencyStatus: [];
	gridStats: [];
	isAssigned: [];
	moveBundleList: [];
	depGrpCrt: {
		modifiedDate: number,
		modifiedBy: string,
		connectionTypes: [],
		statusTypes: []
	};
	compactPref: string;
	showTabs: boolean;
	tabName: string;
	groupId: string;
	assetName: string;
	tagsIds: [];
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
