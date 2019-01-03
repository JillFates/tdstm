export interface TDSButton {
	icon: string;
	title: string;
	tooltip?: string;
}

export enum TDSActionsButton {
	ArchitectureGraphShow,
	AssetEdit,
	AssetClone,
	AssetClose,
	AssetDelete,
	BulkEdit,
	CommentEdit,
	CommentDelete,
	CommentCreate,
	CommentList,
	FilterClear,
	GenericConfiguration,
	GenericExport,
	GenericIsFavorite,
	GenericIsNotFavorite,
	GenericRefresh,
	GenericDelete,
	GenericSave,
	GenericSaveAs,
	TaskCreate,
	TaskDelete,
	TaskEdit,
	TaskList,
	TaskSave,
	ViewCreate,
	ViewEdit,
	ViewDelete
}
