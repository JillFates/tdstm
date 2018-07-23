export enum BulkActions {
	Edit,
	Delete
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
	fields: ListOption[];
	actions: ListOption[];
}

export interface ListOption {
	id: string;
	text: string;
}
