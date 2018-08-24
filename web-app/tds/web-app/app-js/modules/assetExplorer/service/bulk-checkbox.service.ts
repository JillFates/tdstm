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

	private hasSelectedAllItems(): boolean {
		return this.bulkSelectedItems.length === this.pageSize
	}

	changeState(state: CheckboxState): void {
		this.currentState = state.current;

		if (state.affectItems) {
			const select = this.canSelectAll();

			this.changeCheckStateBulkItems(select);
		}
	}

	private getCurrentState(): CheckboxStates {
		return this.currentState;
	}

	private canSelectAll(): boolean {
		return [CheckboxStates.checked, CheckboxStates.indeterminate].indexOf(this.currentState) >= 0;
	}

	clearSelectedItems(): void {
		this.bulkSelectedItems = [];
	}

	// on init or page change
	initializeKeysBulkItems(ids: string[]): void {
		this.bulkItems = {};
		ids.forEach((id: string) => this.bulkItems[id] = false);

		this.handlePageChange();
	}

	private handlePageChange(): void {
		let state = this.currentState === CheckboxStates.indeterminate ? CheckboxStates.indeterminate : CheckboxStates.unchecked;
		this.setStateSubject.next({current: state, affectItems: true});
	}

	private selectBulkItem(id: string, checked: boolean): void {
		this.bulkItems[id] = checked;
		this.refreshBulkSelectedItems();

		this.currentState =  this.hasSelectedAllItems() ? CheckboxStates.checked : CheckboxStates.unchecked;
		this.setStateSubject.next({current: this.currentState, affectItems: false});
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
		return this.currentState === CheckboxStates.indeterminate ? allCounter : items.length;
	}

	private refreshBulkSelectedItems() {
		const keys = Object.keys(this.bulkItems);

		// update edit bulk items
		this.bulkSelectedItems = keys
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));
	}

	getBulkSelectedItems(viewId: number, model: ViewSpec, justPlanning: boolean): Promise<number[]> {
		return new Promise((resolve, reject) => {
			if (this.getCurrentState() === CheckboxStates.indeterminate) {
				return this.getBulkAssetIds(viewId, model, justPlanning)
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

	setCurrentState(state: CheckboxStates): void {
		this.currentState = state;
	}

	checkItem(id: string, checked: boolean, currentPageSize: number): void {
		this.setPageSize(currentPageSize);
		this.selectBulkItem(id, checked);
	}

}