import {Component, Inject, OnInit} from '@angular/core';
import {CompositeFilterDescriptor, State, process} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult} from '@progress/kendo-angular-grid';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {DependenciesService} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {ActionType} from '../../../dataScript/model/data-script.model';

@Component({
	selector: 'tds-dependencies-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/list/dependencies-list.component.html'
})
export class DependenciesListComponent implements OnInit {
	protected assets: any[];
	protected skip = 0;
	protected pageSize = GRID_DEFAULT_PAGE_SIZE;
	protected dependenciesColumnModel: DependenciesColumnModel;
	public gridData: GridDataResult;
	protected state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private dependenciesService: DependenciesService,
		private preferenceService: PreferenceService) {
	}

	ngOnInit() {
		this.dependenciesColumnModel = new DependenciesColumnModel();
		this.state.take = this.pageSize;
		this.state.skip = this.skip;

		console.log('dependencies init');
		this.dependenciesService.getDependencies()
			.subscribe((results) => {
				this.assets = results.data.assets;
				this.gridData = process(this.assets, this.state);
				console.log('The results are');
				console.log(results);
			})
	}

	onClickTemplate(dataItem: any) {
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
		this.gridData = process(this.assets, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.assets, this.state);
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
		this.gridData = process(this.assets, this.state);
	}

}
