// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Component
import {RequestImportComponent} from '../requestImport/request-import.component';
import {LicenseDetailComponent} from '../detail/license-detail.component';
// Service
import {LicenseManagerService} from '../../service/license-manager.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {ActionType, COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {
	DIALOG_SIZE,
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
	LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY,
	ModalType
} from '../../../../shared/model/constants';
import {
	LicenseColumnModel,
	LicenseEnvironment,
	LicenseModel,
	LicenseStatus,
	LicenseType
} from '../../model/license.model';
// Kendo
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult} from '@progress/kendo-angular-grid';
import {BooleanFilterData} from '../../../../shared/model/data-list-grid.model';

declare var jQuery: any;

@Component({
	selector: 'tds-license-manager-list',
	templateUrl: 'license-list.component.html'
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
	private _pageSize;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public licenseColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: any[];
	public dateFormat = '';
	public licenseType = LicenseType;
	public licenseStatus = LicenseStatus;
	public licenseEnvironment = LicenseEnvironment;
	public booleanFilterData = BooleanFilterData;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private licenseManagerService: LicenseManagerService,
		private preferenceService: PreferenceService,
		private prompt: UIPromptService,
		private route: ActivatedRoute) {
		this.resultSet = this.route.snapshot.data['licenses'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.state.skip = 0;
		this.pageSize = parseFloat(localStorage.getItem(LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY));

		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.licenseColumnModel = new LicenseColumnModel(`{0:${this.dateFormat}}`);
			});
	}

	get pageSize(): number {
		return this._pageSize;
	}
	set pageSize(pageSize: number) {
		if ( !pageSize ) {
			pageSize = GRID_DEFAULT_PAGE_SIZE;
		}
		this._pageSize = pageSize;
		localStorage.setItem(LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY, pageSize.toFixed());
		this.applyPageSize();
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
		const root = this.licenseManagerService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.licenseManagerService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.openLicenseViewEdit(event['dataItem']);
		}
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
	 * Delete the selected License
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'You are about to delete the selected license. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.licenseManagerService.deleteLicense(dataItem.id).subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Import a new Request License
	 */
	protected onImportLicenseRequest(): void {
		this.dialogService.open(RequestImportComponent, []).then((license: any) => {

			setTimeout(() => {
				this.openLicenseViewEdit(license);
			}, 1000);

			if (license) {
				this.reloadData();
			}

		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Reload the list with the latest created/edited license
	 */
	protected reloadData(): void {
		this.licenseManagerService.getLicenses().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
			},
			(err) => console.log(err));
	}

	/**
	 * Opens the selected License View
	 * @param licenseModel
	 */
	private openLicenseViewEdit(licenseModel: LicenseModel, openEdit = false): void {
		let modelType = ModalType.VIEW;
		if (openEdit) {
			modelType = ModalType.EDIT;
		}
		this.dialogService.open(LicenseDetailComponent, [
			{provide: LicenseModel, useValue: licenseModel}
		], DIALOG_SIZE.LG, false, modelType).then((result: LicenseModel) => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.pageSize = event.take || this.state.take;
	}

	private applyPageSize(): void {
		this.state.take = this.pageSize;
		// Limit render result to what is in state (skip, take)
		this.gridData = process(this.resultSet, this.state);
		// Next code crops the viewport to fit the number of rows (re-apply the class)
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');

		// Hack to avoid that the horizontal scroll bar keep showing when resizing
		// 1st we hide it
		jQuery('.k-grid.k-grid-no-scrollbar .k-grid-content').css('overflow-x', 'hidden');
		// then we add a timeout to add the action at the end of the event-loop and re-show the scrollbar after the DOM has rendered
		setTimeout( () => {
			jQuery('.k-grid.k-grid-no-scrollbar .k-grid-content').css('overflow-x', '');
		}, 50);
	}
}