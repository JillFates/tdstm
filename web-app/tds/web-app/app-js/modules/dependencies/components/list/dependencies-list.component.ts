import {Component, OnInit} from '@angular/core';
import { State } from '@progress/kendo-data-query';
import { GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import {BehaviorSubject} from 'rxjs';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {DependenciesService} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';

import {tap, map, mergeMap} from 'rxjs/operators';

@Component({
	selector: 'tds-dependencies-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/list/dependencies-list.component.html'
})
export class DependenciesListComponent implements OnInit {
	protected gridStateSubject: BehaviorSubject<State>;
	protected skip = 0;
	protected pageSize = GRID_DEFAULT_PAGE_SIZE;
	protected maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	protected dependenciesColumnModel: DependenciesColumnModel;
	public gridData: GridDataResult;
	protected state: State;

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private dependenciesService: DependenciesService) {
	}

	ngOnInit() {
		this.gridData = { data: [], total: 0 };
		this.dependenciesColumnModel = new DependenciesColumnModel();
		this.gridStateSubject = new BehaviorSubject(this.getInitialGridState());

		this.gridStateSubject
			.pipe(
				tap((state: State) => this.state = state),
				mergeMap((state) => this.dependenciesService.getDependencies(state)),
				map((results) => ({data: results.assets, total: results.pagination.total}))
			)
			.subscribe((results) => this.gridData = results);
	}

	/**
	 * Handle pageChange, filterChange, sortChange events
	 * @param {DataStateChangeEvent} state
	 */
	protected dataStateChange(state: DataStateChangeEvent): void {
		this.gridStateSubject.next(state);
	}

	/**
	 * Get the initial state of the grid
	 * @returns {State} default grid state properties
	 */
	getInitialGridState(): State {
		return {
			sort: [{
				dir: 'asc',
				field: 'name'
			}],
			filter: {
				filters: [],
				logic: 'and'
			},
			take: GRID_DEFAULT_PAGE_SIZE,
			skip: 0
		}
	}

	/**
	 * Get the current selected items counter
	 * @returns {number}
	 */
	getSelectedItemsCount(): number {
		return 0;
	}

	hasSelectedItems(): boolean {
		return false;
	}

	onClickBulkButton(): void {
	}

	onBulkOperationResult(operation: any): void {
	}

}
