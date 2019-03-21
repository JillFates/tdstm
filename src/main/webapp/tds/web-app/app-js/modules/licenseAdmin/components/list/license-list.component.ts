// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Component
import {RequestLicenseComponent} from '../request/request-license.component';
import {CreatedLicenseComponent} from '../created-license/created-license.component';
import {LicenseDetailComponent} from '../detail/license-detail.component';
// Service
import {LicenseAdminService} from '../../service/license-admin.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UserContextService} from '../../../security/services/user-context.service';
// Model
import {COLUMN_MIN_WIDTH, ActionType} from '../../../dataScript/model/data-script.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE, DIALOG_SIZE} from '../../../../shared/model/constants';
import {
	LicenseColumnModel,
	LicenseType,
	LicenseStatus,
	LicenseEnvironment,
	LicenseModel, RequestLicenseModel
} from '../../model/license.model';
// Kendo
import {State, process, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult} from '@progress/kendo-angular-grid';
import {UserContextModel} from '../../../security/model/user-context.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
declare var jQuery: any;

@Component({
	selector: 'tds-license-list',
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
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
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

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private userContextService: UserContextService) {
		this.resultSet = this.route.snapshot.data['licenses'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.userContextService.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(userContext.dateFormat);
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
					this.licenseAdminService.deleteLicense(dataItem.id).subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Request a New License
	 */
	protected onCreateLicense(): void {
		this.dialogService.open(RequestLicenseComponent, []).then((requestLicenseModel: RequestLicenseModel) => {
			setTimeout(() => {
				this.openCreatedLicenseDialog(requestLicenseModel);
			}, 500);
			if (requestLicenseModel) {
				this.reloadData();
			}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Opens a dialog to show to the user that the request has been created and next steps to follow
	 */
	private openCreatedLicenseDialog(requestLicenseModel: RequestLicenseModel): void {
		this.dialogService.open(CreatedLicenseComponent, [
			{provide: RequestLicenseModel, useValue: requestLicenseModel}
		]).then(() => {
			console.log('Dismissed Dialog');
		}).catch(() => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Reload the list with the latest created/edited license
	 */
	protected reloadData(): void {
		this.licenseAdminService.getLicenses().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
			},
			(err) => console.log(err));
	}

	/**
	 * Opens the selected License View
	 * @param licenseModel
	 */
	private openLicenseViewEdit(licenseModel: LicenseModel): void {
		this.dialogService.open(LicenseDetailComponent, [
			{ provide: LicenseModel, useValue: licenseModel }
		], DIALOG_SIZE.LG, false).then( (result: LicenseModel) => {
			if (result && result.id) {
				//
			}
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
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}
}