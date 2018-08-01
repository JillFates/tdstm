import {CheckboxStates} from '../components/tds-checkbox/model/tds-checkbox.model';

export class DataGridCheckboxHelper {
	private currentState: CheckboxStates;
	bulkItems: any;
	private bulkSelectedItems: number[];

	constructor() {
		this.currentState = CheckboxStates.unchecked;
		this.bulkItems = {};
	}

	getBulkSelectedItems(): number[] {
		return this.bulkSelectedItems;
	}

	changeState(currentState: CheckboxStates): void {
		this.currentState = currentState;

		if (!this.selectAllIfApplicable() ) {
			this.selectBulkItems(false);
		}
	}

	private selectAllIfApplicable(): boolean {
		if (this.canSelectAll()) {
			this.selectBulkItems(true);
			return true;
		}
		return false;
	}

	canSelectAll(): boolean {
		return [CheckboxStates.checked, CheckboxStates.indeterminate].indexOf(this.currentState) >= 0;
	}

	// on init or page change
	initializeKeysBulkItems(ids: string[]): void {
		this.bulkItems = {};
		ids.forEach((id: string) => this.bulkItems[id] = false);
		this.selectAllIfApplicable();
	}

	selectBulkItem(id: string, checked: boolean): void {
		this.bulkItems[id] = checked;
		this.refreshBulkSelectedItems();
	}

	getValueBulkItem(id: string): boolean {
		return this.bulkItems[id];
	}

	selectBulkItems(select: boolean): void {
		const keys = Object.keys(this.bulkItems);

		// update bulk items
		keys.forEach(key => this.bulkItems[key] = select);
		this.refreshBulkSelectedItems();
	}

	refreshBulkSelectedItems() {
		const keys = Object.keys(this.bulkItems);

		// update edit bulk items
		this.bulkSelectedItems = keys
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));
	}

	getSelectedItems(): number[] {
		this.bulkSelectedItems = Object.keys(this.bulkItems)
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));

		return this.bulkSelectedItems;

		// this.selectAll = this.bulkSelectedItems.length === this.gridData.data.length;
	}
}