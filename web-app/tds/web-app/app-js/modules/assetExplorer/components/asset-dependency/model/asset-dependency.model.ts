export enum DependencyType {
	dependencyA,
	dependencyB
}

export interface DependencyChange {
	type: DependencyType;
	dependencies: any;
}

export interface Dependency {
	dataFlowFreq: string;
	dataFlowDirection: string;
	status: string;
	type: string;
	c1: string;
	c2: string;
	c3: string;
	c4: string;
	comment: string;
}
