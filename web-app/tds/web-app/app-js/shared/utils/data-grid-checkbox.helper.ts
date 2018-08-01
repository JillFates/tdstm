import {CheckboxStates} from '../components/tds-checkbox/model/tds-checkbox.model';

export class DataGridCheckboxHelper {
	private currentState: CheckboxStates;
	private pageSize: number = null;
	bulkItems: any;
	private bulkSelectedItems: number[];

	constructor() {
		this.currentState = CheckboxStates.unchecked;
		this.bulkItems = {};
		this.bulkSelectedItems = [];
	}

	setPageSize(size: number): void {
		this.pageSize = size;
	}

	getOverrideState(changingPage = false): CheckboxStates {
		if (changingPage) {
			if (this.currentState === CheckboxStates.checked) {
				return CheckboxStates.unchecked;
			}
			return null;
		}

		return (this.bulkSelectedItems.length === this.pageSize) ? CheckboxStates.checked : CheckboxStates.unchecked;
	}

	changeState(currentState: CheckboxStates): void {
		this.currentState = currentState;

		if (!this.selectAllIfApplicable() ) {
			this.selectBulkItems(false);
		}
	}

	getCurrentState(): CheckboxStates {
		return this.currentState;
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

	clearSelectedItems(): void {
		this.bulkSelectedItems = [];
	}

	// on init or page change
	initializeKeysBulkItems(ids: string[]): void {
		this.bulkItems = {};
		ids.forEach((id: string) => this.bulkItems[id] = false);

		if (this.currentState === CheckboxStates.indeterminate) {
			this.selectBulkItems(true);
		}
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

	getBulkSelectedItems(): number[] {
		return this.bulkSelectedItems;
	}
}