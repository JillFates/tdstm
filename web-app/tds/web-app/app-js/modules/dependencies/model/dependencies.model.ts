import {TagModel} from '../../assetTags/model/tag.model';

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
	assetTags: TagModel[];
	dependencyTags: TagModel[];
}

export interface DependencyResults {
	data: Dependency[];
	total: number;
}

export interface TagState {
	field: string;
	tags: string;
}
