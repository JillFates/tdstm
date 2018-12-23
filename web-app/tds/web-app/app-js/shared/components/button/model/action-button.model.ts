export interface TDSButton {
	icon: string;
	title: string;
	tooltip?: string;
}

export enum TDSActionsButton {
	AssetEdit,
	AssetClone,
	BulkEdit,
	CommentCreate,
	CommentList,
	FilterClear,
	GenericConfiguration,
	GenericExport,
	GenericIsFavorite,
	GenericIsNotFavorite,
	GenericRefresh,
	GenericSave,
	GenericSaveAs,
	TaskCreate,
	TaskList,
	TaskSave,
	ViewCreate,
	ViewEdit,
	ViewDelete
}
