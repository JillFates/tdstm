export interface IArchitectureGraphAsset {
	assetId?: number;
	key?: number;
	assetTypes?: IAssetType;
	assetTypesJson?: IAssetType;
	environment?: string;
	levelsUp?: number;
	levelsDown?: number;
	links?: IAssetLink[]
	nodes?: IAssetNode[];
	nodeTooltipData?: any;
}

export interface IAssetType {
	[key: string]: {
		frontEndName: string,
		frontEndNamePlural: string,
		internalName: string,
		labelHandles: string,
		labelPreferenceName: string,
		labelText: string
	};
}
export interface IAssetLink {
	child?: number;
	childId?: number;
	future?: boolean;
	id?: number;
	mutual?: any;
	notApplicable?: boolean;
	opacity?: number;
	parent?: number;
	parentId?: number;
	questioned?: boolean;
	redundant?: boolean;
	unresolved?: boolean;
	validated?: boolean;
	value?: number;
}

export interface IAssetNode {
	key?: number;
	assetClass?: string;
	checked?: boolean;
	children?: number[];
	color?: string;
	id?: number;
	name?: string;
	parents: any[];
	shape?: string;
	siblings?: any[];
	size?: number;
	title?: string;
	type?: string;
	tooltipData?: any;
	iconPath?: any
}
