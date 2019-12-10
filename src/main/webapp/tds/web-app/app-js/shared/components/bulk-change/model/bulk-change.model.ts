export enum BulkActions {
	Edit,
	Delete,
	Run
}

export enum BulkChangeType {
	Assets,
	Dependencies
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
	public showAction?: boolean;
	public bulkChangeType?: BulkChangeType;
	public viewId?: number;
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
