import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Subject } from 'rxjs/Subject';

import {CheckboxState, CheckboxStates} from '../../../shared/components/tds-checkbox/model/tds-checkbox.model';
import { AssetExplorerService } from './asset-explorer.service';
import { ViewSpec } from '../model/view-spec.model';

@Injectable()
export class BulkCheckboxService {
	public setStateSubject: Subject<CheckboxState> = new Subject<CheckboxState>();
	private currentState: CheckboxStates;
	private pageSize: number = null;
	bulkItems: any;
	private excludedBag: ExcludedAssetsBag;
	private availableAssets: Array<any>;
	private idFieldName = 'common_id';

	constructor(private assetExplorerService: AssetExplorerService) {
		this.currentState = CheckboxStates.unchecked;
		this.bulkItems = {};
		this.availableAssets = [];
		this.excludedBag = new ExcludedAssetsBag();
	}

	setPageSize(size: number): void {
		this.pageSize = size;
	}

	setIdFieldName(name: string): void {
		this.idFieldName = name;
	}

	changeState(state: CheckboxState): void {
		this.currentState = state.current;

		if (!this.isIndeterminateState()) {
			this.excludedBag.clean();
		}

		if (state.affectItems) {
			const select = this.canSelectAll();
			this.changeCheckStateBulkItems(select);
		}
	}

	setCurrentState(state: CheckboxStates): void {
		this.currentState = state;
	}

	// on init or page change
	initializeKeysBulkItems(assets: Array<any>): void {
		this.bulkItems = {};
		this.availableAssets = [...assets];
		assets.map(asset => asset[this.idFieldName]).forEach((id: string) => this.bulkItems[id] = false);
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
	}

	hasSelectedItems(): boolean {
		return this.getBulkItemsSelectedAsArray().length > 0;
	}

	getSelectedItemsCount(allCounter: number): number {
		const items =  this.getBulkItemsSelectedAsArray() || [];
		return this.isIndeterminateState()  ? allCounter - this.excludedBag.getAssets().length : items.length;
	}

	handleFiltering(): void {
		if (this.canSelectAll()) {
			// on filtering select all
			this.excludedBag.clean();
			this.changeCheckStateBulkItems(true);
		}

	}

	getBulkSelectedItems(params: any, getAllIds: any = null): Observable<any> {
		return new Observable((observer: any) => {
			if (this.isIndeterminateState()) {
				let bulkIds = getAllIds;
				if (params) {
					const {viewId, model, justPlanning} = params;
					bulkIds = this.getBulkAssetIdsFromView(viewId, model, justPlanning);
				}
				return bulkIds
					.subscribe((result: any) => {
						const assets = result && result.assets || [];
						// TODO side effect
						const selectedAssetsIds = assets
							.map((asset) => asset[this.idFieldName])
							.filter((asset) => this.excludedBag.getAssets().indexOf(asset) === -1);
						const selectedAssets = assets
							.filter((asset) => this.excludedBag.getAssets().indexOf(asset) === -1);

						return observer.next({selectedAssetsIds: selectedAssetsIds, selectedAssets:  selectedAssets});
					});
			} else {
				let selectedAssetsIds = this.getBulkItemsSelectedAsArray();
				let selectedAssets: Array<any> = [];
				selectedAssetsIds.forEach( (id: number) => {
					const match = this.availableAssets.find( asset => asset['common_id'] === id);
					if (match) {
						selectedAssets.push(match);
					}
				});
				return observer.next({selectedAssetsIds: selectedAssetsIds, selectedAssets: selectedAssets});
			}
		});
	}

	uncheckItems(): void {
		this.setStateSubject.next({current: CheckboxStates.unchecked, affectItems: true});
	}

	private getBulkItemsSelectedAsArray(): Array<number> {
		return Object.keys(this.bulkItems)
			.filter(key => this.bulkItems[key])
			.map(value => parseInt(value, 10));
	}

	private hasSelectedAllItems(): boolean {
		return this.getBulkItemsSelectedAsArray().length === this.pageSize
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

	private getBulkAssetIdsFromView(viewId: number, model: ViewSpec, justPlanning: boolean): Observable<any> {
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

		return this.assetExplorerService.query(viewId, params);
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
