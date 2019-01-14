export enum DependencyType {
	dependencyA,
	dependencyB
}

export interface DependencyChange {
	type: DependencyType;
	dependencies: any;
}
