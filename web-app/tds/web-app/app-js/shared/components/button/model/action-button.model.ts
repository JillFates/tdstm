export interface TDSButton {
	icon: string;
	title: string;
	justIcon?: boolean;
}

export enum TDSActionsButton {
	AssetEdit,
	AssetClone,
	BulkEdit,
	FilterClear,
	GenericExport,
	GenericSave,
	GenericSaveAs,
	TaskCreate,
	TaskList,
	TaskSave,
	CommentCreate,
	CommentList
}

export enum TDSActionsIcon {
	GenericRefresh = 200,
	GenericIsFavorite,
	GenericIsNotFavorite,
	GenericConfiguration
}
