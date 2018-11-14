import {Component, OnInit} from '@angular/core';
import { GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import {BehaviorSubject} from 'rxjs';
import {CompositeFilterDescriptor, State, process} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';

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
	protected maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	protected assets: any[];
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
		this.state = this.getInitialGridState();
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


	onEdit(dataItem: any) {
	}

	onDelete(dataItem: any) {
	}

	clearValue(dataItem: any) {
	}

	onFilter(dataItem: any) {
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		// this.gridData = process(this.assets, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		// this.gridData = process(this.assets, this.state);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
	}

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		// this.gridData = process(this.assets, this.state);
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		console.log('State:');
		console.log(state);

		/*
		if (state.sort[0]) {
			// Invert the Order to remove the Natural/Default from the UI (no arrow)
			if (!state.sort[0].dir) {
				state.sort[0].dir = (this.model.sort.order === 'a' ? 'desc' : 'asc');
			}

			let field = state.sort[0].field.split('_');
			this.model.sort.domain = field[0];
			this.model.sort.property = field[1];
			this.model.sort.order = state.sort[0].dir === 'asc' ? 'a' : 'd';
		}
		this.updateGridState(state);
		this.modelChange.emit();
		*/
	}

	getInitialGridState(): any {
		return {
			sort: [{
				dir: 'asc',
				field: 'name'
			}],
			filter: {
				filters: [],
				logic: 'and'
			}
		}
	}

}
