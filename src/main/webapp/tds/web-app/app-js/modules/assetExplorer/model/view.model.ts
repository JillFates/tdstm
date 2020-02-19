import { ViewSpec } from './view-spec.model';

export class ViewModel {
	id: number;
	name: string;
	isOwner: boolean;
	isSystem: boolean;
	isShared: boolean;
	hasOverride: boolean;
	isFavorite?: boolean;
	isOverride?: boolean;
	saveAsOption?: string;
	schema?: ViewSpec;
	overridesView?: any;
	querystring?: any;
	constructor() {
		this.schema = new ViewSpec();
		this.isOwner = true;
	}
}

export class ViewGroupModel {
	name: string;
	items: Array<ViewModel>;
	open: boolean;
	type: ViewType;
}

export enum ViewType {
	ALL,
	RECENT,
	FAVORITES,
	MY_VIEWS,
	SHARED_VIEWS,
	SYSTEM_VIEWS
}
export enum ViewFolderIcon {
	FOLDER,
	START
}