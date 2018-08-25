import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';

import {CheckboxStates, CheckboxState} from '../tds-checkbox/model/tds-checkbox.model';
import { AssetExplorerService } from './asset-explorer.service';
import { ViewSpec } from '../model/view-spec.model';

@Injectable()
export class BulkCheckboxService {
	public setStateSubject: Subject<CheckboxState> = new Subject<CheckboxState>();
	private currentState: CheckboxStates;
	private pageSize: number = null;
	bulkItems: any;
	private bulkSelectedItems: number[];
	private excludedBag: ExcludedAssetsBag;

	constructor(private assetExplorerService: AssetExplorerService) {
		this.currentState = CheckboxStates.unchecked;
		this.bulkItems = {};
		this.bulkSelectedItems = [];
		this.excludedBag = new ExcludedAssetsBag();
	}

	setPageSize(size: number): void {
		this.pageSize = size;
	}

	resetSelectedBulkItems() {
		this.bulkItems = {};
		this.bulkSelectedItems = [];
	}


	changeState(state: CheckboxState): void {
		this.currentState = state.current;

		if (!this.isIndeterminateState()) {
			this.excludedBag.clean();
		}

		if (state.affectItems) {
			// this.resetExcluded();
			const select = this.canSelectAll();
			this.changeCheckStateBulkItems(select);
		}
	}

	setCurrentState(state: CheckboxStates): void {
		this.currentState = state;
	}

	// on init or page change
	initializeKeysBulkItems(ids: string[]): void {
		this.bulkItems = {};
		ids.forEach((id: string) => this.bulkItems[id] = false);

		this.handlePageChange();
		this.setExcludedAssets();
	}

	checkItem(id: number, checked: boolean, currentPageSize: number): void {
		this.setPageSize(currentPageSize);
		this.selectBulkItem(id, checked);
	}

	getValueBulkItem(id: string): boolean {
		return this.bulkItems[id];
	}

	changeCheckStateBulkItems(checkState: boolean): void {
		const keys = Object.keys(this.bulkItems);

		// update bulk items
		keys.forEach(key => this.bulkItems[key] = checkState);
		this.refreshBulkSelectedItems();
	}

	hasSelectedItems(): boolean {
		return Boolean(this.bulkSelectedItems && this.bulkSelectedItems.length);
	}

	getSelectedItemsCount(allCounter: number): number {
		const items = this.bulkSelectedItems || [];
		console.log('Items length:', items.length);
		return this.isIndeterminateState()  ? allCounter - this.excludedBag.getAssets().length : items.length;
	}

	handleFiltering(): void {
		if (this.canSelectAll()) {
			// on filtering select all
			this.excludedBag.clean();
			this.changeCheckStateBulkItems(true);
		}

	}

	getBulkSelectedItems(viewId: number, model: ViewSpec, justPlanning: boolean): Promise<number[]> {
		return new Promise((resolve, reject) => {
			if (this.isIndeterminateState()) {
				return this.getBulkAssetIds(viewId, model, justPlanning)
					.then((result: any) => {
						const assets = result && result.assets || [];
						// TODO side effect
						this.bulkSelectedItems = assets.map((asset) => asset.common_id)
							.filter((asset) => this.excludedBag.getAssets().indexOf(asset) === -1);

						return resolve(this.bulkSelectedItems);
					})
					.catch((err) => reject(err))
			}
			return resolve(this.bulkSelectedItems);
		});
	}

	uncheckItems(): void {
		this.setStateSubject.next({current: CheckboxStates.unchecked, affectItems: true});
	}


	private refreshBulkSelectedItems() {
		const keys = Object.keys(this.bulkItems);

		// update edit bulk items
		this.bulkSelectedItems = keys
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));
	}

	private hasSelectedAllItems(): boolean {
		return this.bulkSelectedItems.length === this.pageSize
	}

	private canSelectAll(): boolean {
		return [CheckboxStates.checked, CheckboxStates.indeterminate].indexOf(this.currentState) >= 0;
	}

	private handlePageChange(): void {
		let state = this.isIndeterminateState() ? CheckboxStates.indeterminate : CheckboxStates.unchecked;
		this.setStateSubject.next({current: state, affectItems: true});
	}

	private setExcludedAssets(): void {
		if (this.isIndeterminateState()) {
			const bulkKeys = Object.keys(this.bulkItems);

			this.excludedBag.getAssets()
				.filter((key: number) => bulkKeys.indexOf(key.toString()) >= 0)
				.forEach((id: number) => this.bulkItems[id] = false);
		}
	}

	private isIndeterminateState(): boolean {
		return this.currentState === CheckboxStates.indeterminate;
	}

	private selectBulkItem(id: number, checked: boolean): void {
		this.bulkItems[id] = checked;
		this.refreshBulkSelectedItems();

		if (this.isIndeterminateState()) {
			if (checked) {
				this.excludedBag.remove(id);
			} else {
				this.excludedBag.add(id);
			}
		} else {
			this.excludedBag.clean();
			this.currentState =  this.hasSelectedAllItems() ? CheckboxStates.checked : CheckboxStates.unchecked;
			this.setStateSubject.next({current: this.currentState, affectItems: false});
		}
	}

	private getBulkAssetIds(viewId: number, model: ViewSpec, justPlanning: boolean): Promise<any> {
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
			this.assetExplorerService.query(viewId, params)
				.subscribe(result => resolve(result), err => reject(err));
		});
	}


}

// handle store temporally assets excluded from items selected
class ExcludedAssetsBag {
	private assets: number[];

	constructor() {
		this.assets = [];
	}

	clean(): void {
		this.assets = [];
	}
	add(id): void {
		this.remove(id);
		this.assets.push(id);
	}

	remove(id: number): boolean {
		const index = this.assets.indexOf(id);

		if (index >= 0) {
			this.assets.splice(index, 1);
			return true;
		}
		return false;
	}

	getAssets(): number[] {
		return [...this.assets];
	}
}
