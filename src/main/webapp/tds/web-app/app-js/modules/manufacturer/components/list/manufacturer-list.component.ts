// Angular
import {
	Component,
	ComponentFactoryResolver,
	OnInit,
	ViewChild,
} from '@angular/core';
// Services
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ManufacturerService} from '../../service/manufacturer.service';
// Components
import {ManufacturerViewEditComponent} from '../view-edit/manufacturer-view-edit.component';
// Models
import {ActionType} from '../../../dataScript/model/data-script.model';
import {Permission} from '../../../../shared/model/permission.model';
import {
	ColumnHeaderData,
	DialogConfirmAction,
	DialogService, GridComponent,
	GridModel, GridRowAction, GridSettings,
	HeaderActionButtonData,
	ModalSize
} from 'tds-component-library';
import {CellClickEvent} from '@progress/kendo-angular-grid';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ManufacturerColumnModel, ManufacturerModel} from '../../model/manufacturer.model';

@Component({
	selector: 'manufacturer-list',
	templateUrl: 'manufacturer-list.component.html'
})
export class ManufacturerListComponent implements OnInit {
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
		private manufacturerService: ManufacturerService,
		private preferenceService: PreferenceService,
		private translateService: TranslatePipe
	) {
	}

	async ngOnInit() {
		this.gridRowActions = [
			{
				name: 'Edit',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.onEdit,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isEditAvailable(),
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
				onClick: this.onCreateManufacturer,
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

		this.columnModel = new ManufacturerColumnModel().columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openManufacturer(event.dataItem, ActionType.VIEW);
		}
	}

	private onCreateManufacturer = async (): Promise<void> => {
		try {
			let manufacturerModel: ManufacturerModel = {
				name: '',
				description: '',
				alias: '',
				corporateName: '',
				corporateLocation: '',
				website: ''
			};
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ManufacturerViewEditComponent,
				data: {
					manufacturerModel: manufacturerModel,
					actionType: ActionType.CREATE,
					openFromList: false
				},
				modalConfiguration: {
					title: 'Manufacturer',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	};

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: ManufacturerModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openManufacturer(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	};

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	private onDelete = async (dataItem: ManufacturerModel): Promise<void> => {
		try {
			if (this.isDeleteAvailable()) {
				this.dialogService.confirm(
					this.translateService.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'),
					this.translateService.transform('GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION')
				).subscribe((result: any) => {
						if (result.confirm === DialogConfirmAction.CONFIRM) {
							this.manufacturerService.deleteManufacturer(dataItem.id).toPromise();
							this.gridComponent.reloadData();
						}
					});
			}
		} catch (error) {
			console.error(error);
		}
	};

	private loadData = async (): Promise<ManufacturerModel[]> => {
		try {
			return await this.manufacturerService.getManufacturerList().toPromise();
		} catch (error) {
			console.error(error);
		}
	};

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param manufacturerModel
	 * @param {number} actionType
	 * @param openFromList
	 */
	private async openManufacturer(manufacturerModel: ManufacturerModel, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ManufacturerViewEditComponent,
				data: {
					manufacturerModel: manufacturerModel,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Manufacturer',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	private isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderCreate);
	}

	private isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderDelete);
	}

	private isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderUpdate);
	}

}
