
export class DependencyAnalyzerDataModel {

	asset: string;
	date: Date;
	dependencyType: [];
	dependencyConsoleList: [];
	assetDependency: [];
	moveBundle: [];
	allMoveBundles: [];
	planStatusOptions: [];
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
