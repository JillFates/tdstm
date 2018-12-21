export interface TDSButton {
	icon: string;
	title: string;
}

export enum TDSActionsButton {
	AssetEdit,
	AssetClone,
	TaskCreate,
	TaskList,
	CommentCreate,
	CommentList
}

const registeredButtons: {[key: string]: TDSButton} = {
	[TDSActionsButton.AssetEdit] :     { icon: 'edit',        title: 'Edit Asset' },
	[TDSActionsButton.AssetClone]:    { icon: 'clone',       title: 'Clone Asset' },
	[TDSActionsButton.TaskCreate]:    { icon: 'file-text-o', title: 'Create a Task' },
	[TDSActionsButton.TaskList]:      { icon: 'list-alt',    title: 'List existing Tasks'},
	[TDSActionsButton.CommentCreate]: { icon: 'comment',     title: 'Create a Comment' },
	[TDSActionsButton.CommentList]:   { icon: 'comments',    title: 'List existing Comments'},
};

export class ButtonsFactory {
	static create(key: TDSActionsButton): TDSButton {
		return registeredButtons[key] || null;
	}
}