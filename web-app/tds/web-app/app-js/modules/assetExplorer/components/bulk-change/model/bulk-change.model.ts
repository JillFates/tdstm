export enum BulkActions {
	Edit,
	Delete
}

export interface IdTextItem {
	id: string;
	text: string;
}

export class BulkChangeModel {
	public selectedItems: number[];
}

export interface BulkActionResult {
	action: BulkActions;
	success: boolean;
	message?: string;
}

export interface BulkEditAction {
	domain: string;
	fields: IdTextItem[];
	actions: IdTextItem[];
}
