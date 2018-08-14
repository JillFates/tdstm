import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import {CheckboxStates} from '../tds-checkbox/model/tds-checkbox.model';
import { AssetExplorerService } from './asset-explorer.service';
import { ViewSpec } from '../model/view-spec.model';

@Injectable()
export class DataGridCheckboxService {
	private currentState: CheckboxStates;
	private pageSize: number = null;
	bulkItems: any;
	private bulkSelectedItems: number[];

	constructor(private assetExplorerService: AssetExplorerService) {
		this.currentState = CheckboxStates.unchecked;
		this.bulkItems = {};
		this.bulkSelectedItems = [];
	}

	setPageSize(size: number): void {
		this.pageSize = size;
	}

	resetSelectedBulkItems() {
		this.bulkItems = {};
		this.bulkSelectedItems = [];
	}

	changeStateByUserInteraction(changingPage = false): CheckboxStates {
		if (changingPage) {
			if (this.currentState === CheckboxStates.checked) {
				this.currentState = CheckboxStates.unchecked;
				this.resetSelectedBulkItems();
				return this.currentState;
			}
			if (this.currentState === CheckboxStates.unchecked) {
				this.resetSelectedBulkItems();
			}
			return null;
		}

		this.currentState =  (this.bulkSelectedItems.length === this.pageSize) ? CheckboxStates.checked : CheckboxStates.unchecked;

		return this.currentState;
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

	hasSelectedItems(): boolean {
		return Boolean(this.bulkSelectedItems && this.bulkSelectedItems.length);
	}

	refreshBulkSelectedItems() {
		const keys = Object.keys(this.bulkItems);

		// update edit bulk items
		this.bulkSelectedItems = keys
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));
	}

	getBulkSelectedItems(id: number, model: ViewSpec, justPlanning: boolean): Promise<number[]> {
		return new Promise((resolve, reject) => {
			if (this.getCurrentState() === CheckboxStates.indeterminate) {
				return this.getBulkAssetIds(id, model, justPlanning)
					.then((result: any) => {
						const assets = result && result.assets || [];
						this.bulkSelectedItems = assets.map((asset) => asset.common_id)
						return resolve(this.bulkSelectedItems);
					})
					.catch((err) => reject(err))
			}
			return resolve(this.bulkSelectedItems);
		});
	}

	getBulkAssetIds(id: number, model: ViewSpec, justPlanning: boolean): Promise<any> {
		let params = {
				forExport: true,
				offset: 0,
				limit: 0,
				sortDomain: model.sort.domain,
				sortProperty: model.sort.property,
				sortOrder: model.sort.order,
				filters: {
					domains: model.domains,
					columns: model.columns
				}
		};

		if (justPlanning) {
			params['justPlanning'] = true;
		}

		return new Promise((resolve, reject) => {
			this.assetExplorerService.query(id, params)
				.subscribe(result => resolve(result), err => reject(err));
		});

	}
}