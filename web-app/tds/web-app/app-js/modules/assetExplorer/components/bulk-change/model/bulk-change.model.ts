export enum BulkActions {
	Edit,
	Delete
}

export class BulkChangeModel {
	public selectedItems: string[];
}

export interface BulkOperationResult {
	action: BulkActions;
	success: boolean;
	message?: string;
}
