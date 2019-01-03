export interface TDSButton {
	icon: string;
	title: string;
	tooltip?: string;
}

export enum TDSActionsButton {
	ArchitectureGraphShow,
	AssetEdit,
	AssetClone,
	AssetCancel,
	AssetClose,
	AssetDelete,
	AssetUpdate,
	BulkEdit,
	CommentEdit,
	CommentDelete,
	CommentCreate,
	CommentList,
	FilterClear,
	GenericAdd,
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
