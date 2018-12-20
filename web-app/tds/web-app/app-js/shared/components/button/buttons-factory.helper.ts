export interface TDSButton {
	icon: string;
	title: string;
}

const registeredButtons: {[key: string]: TDSButton} = {
	'tds-button-asset-edit':     { icon: 'edit',        title: 'Edit Asset' },
	'tds-button-asset-clone':    { icon: 'clone',       title: 'Clone Asset' },
	'tds-button-task-create':    { icon: 'file-text-o', title: 'Create a Task' },
	'tds-button-task-list':      { icon: 'list-alt',    title: 'List existing Tasks'},
	'tds-button-comment-create': { icon: 'comment',     title: 'Create a Comment' },
	'tds-button-comment-list':   { icon: 'comments',    title: 'List existing Comments'},
};

export class ButtonsFactory {
	static create(key: string): TDSButton {
		return registeredButtons[key] || null;
	}

	static getButtonsSelector(): string {
		return Object.keys(registeredButtons).join(',');
	}
}