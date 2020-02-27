// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Component
import {RequestLicenseComponent} from '../request/request-license.component';
import {CreatedLicenseComponent} from '../created-license/created-license.component';
import {LicenseDetailComponent} from '../detail/license-detail.component';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
// Service
import {LicenseAdminService} from '../../service/license-admin.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Permission} from '../../../../shared/model/permission.model';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {
	LicenseColumnModel,
	LicenseModel
} from '../../model/license.model';
// Other
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: 'tds-license-list',
	templateUrl: 'license-list.component.html',
})
export class LicenseListComponent implements OnInit {
	private gridRowActions: GridRowAction[];

	private headerActions: HeaderActionButtonData[];

	private gridSettings: GridSettings = {
		defaultSort: [{field: 'name', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		selectableSettings: {enabled: true, mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	private columnModel: ColumnHeaderData[];
	public gridModel: GridModel;
	private dateFormat = '';

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService,
		private translateService: TranslatePipe
	) {
	}

	async ngOnInit() {
		this.gridRowActions = [
			{
				name: 'Edit',
				show: true,
				disabled: !this.isCreateAvailable(),
				onClick: this.onEdit,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isCreateAvailable(),
				onClick: this.onDelete,
			},
		];

		this.headerActions = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateLicense,
			},
		];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			loadData: this.loadData,
		};

		this.dateFormat = await this.preferenceService.getUserDatePreferenceAsKendoFormat().toPromise();

		this.columnModel = new LicenseColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	/**
	 * Open the License
	 * @param event
	 */
	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0) {
			await this.openLicenseViewEdit(event.dataItem.id, ActionType.VIEW);
		}
	}

	/**
	 * Delete the selected License
	 * @param dataItem
	 */
	public onDelete = async (dataItem: LicenseModel): Promise<void> => {
		try {
			if (this.isCreateAvailable()) {
				const confirmation = await this.dialogService.confirm(
					'Confirmation Required',
					'You are about to delete the selected license. Do you want to proceed?'
				).toPromise();
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					this.licenseAdminService.deleteLicense(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Request a New License
	 */
	private onCreateLicense = async (): Promise<void> => {
		try {
			const data = await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: RequestLicenseComponent,
				data: {},
				modalConfiguration: {
					title: 'Request New License',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: CreatedLicenseComponent,
				data: {
					requestLicenseModel: data.requestLicenseModel
				},
				modalConfiguration: {
					title: 'License Request Completed',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: LicenseModel): Promise<void> => {
		try {
			if (this.isCreateAvailable()) {
				await this.openLicenseViewEdit(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	}

	private loadData = async (): Promise<LicenseModel[]> => {
		try {
			return await this.licenseAdminService.getLicenses().toPromise();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Opens the selected License View
	 * @param licenseModel
	 */
	private async openLicenseViewEdit(licenseModel: LicenseModel, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: LicenseDetailComponent,
				data: {
					licenseModel: licenseModel
				},
				modalConfiguration: {
					title: 'Request New License',
					draggable: true,
					modalSize: ModalSize.LG
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Determines if user has the permission to create licences
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.LicenseAdministration);
	}
}
