import {IAssetType} from './architecture-graph-asset.model';

export interface ArchitectreGraphAssetPreference {
	assetId?: number;
	assetName?: string;
	levelsUp?: number;
	levelsDown?: number;
	assetClassesForSelect?: IAssetClassesForSelect;
	dependencyStatus?: string[];
	dependencyType?: string[];
	assetTypes?: IAssetType;
	defaultPRefs?: any;
	graphPRefs?: IGraphPreferences;
	assetForSelect2?: any;
}

export interface IAssetClassesForSelect {
	[key: string]: string;
}

export interface IGraphPreferences {
	levelsUp?: number | string;
	levelDown?: number | string;
	showCycles?: boolean;
	appLbl?: boolean;
	labelOffset?: number | string;
	assetClasses?: string;
}
