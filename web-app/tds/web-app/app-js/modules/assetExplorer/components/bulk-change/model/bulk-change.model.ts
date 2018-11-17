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
	public affected: number;
	public showEdit?: boolean;
	public showDelete?: boolean;
	selectedAssets: Array<any>;
}

export interface BulkActionResult {
	action: BulkActions;
	success: boolean;
	message?: string;
}

export interface BulkEditAction {
	fields: IdTextItem[];
	actions: IdTextItem[];
	constraints: Array<any>;
}
