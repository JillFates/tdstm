export interface TDSButton {
	icon: string;
	title: string;
	justIcon?: boolean;
}

export enum TDSActionsButton {
	AssetEdit,
	AssetClone,
	BulkEdit,
	CommentCreate,
	CommentList,
	FilterClear,
	GenericCreate,
	GenericExport,
	GenericSave,
	GenericSaveAs,
	TaskCreate,
	TaskList,
	TaskSave,
	ViewEdit,
	ViewDelete
}

export enum TDSActionsIcon {
	GenericConfiguration = 200,
	GenericIsFavorite,
	GenericIsNotFavorite,
	GenericRefresh
}
