// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Service
import {LicenseAdminService} from '../../service/license-admin.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {COLUMN_MIN_WIDTH, ActionType} from '../../../dataScript/model/data-script.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {LicenseColumnModel, LicenseType, LicenseStatus, LicenseEnvironment} from '../../model/license.model';
// Kendo
import {State, process, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';

@Component({
	selector: 'tds-license-list',
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/list/license-list.component.html'
})
export class LicenseListComponent implements OnInit {

	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	public skip = 0;
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public licenseColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: any[];
	public selectedRows = [];
	public dateFormat = '';
	public licenseType = LicenseType;
	public licenseStatus = LicenseStatus;
	public licenseEnvironment = LicenseEnvironment;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService,
		private prompt: UIPromptService,
		private route: ActivatedRoute) {
		this.resultSet = this.route.snapshot.data['licenses'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.licenseColumnModel = new LicenseColumnModel(`{0:${this.dateFormat}}`);
			});
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		const root = this.licenseAdminService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.licenseAdminService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
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
		this.gridData = process(this.resultSet, this.state);
	}
}