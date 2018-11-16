export interface Dependency {
	assetBundle: string;
	assetId: number;
	assetName: string;
	assetType: string;
	c1: string;
	c2: string;
	c3: string;
	c4: string;
	comment: string;
	dependentBundle: string;
	dependentId: number;
	dependentName: string;
	dependentType: string;
	direction: string;
	frequency: string;
	id: number;
	status: string;
	type: string;
}

export interface DependencyResults {
	dependencies: Dependency[];
	total: number;
}
