import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ManufacturerService} from '../../../manufacturer/service/manufacturer.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {CellClickEvent} from '@progress/kendo-angular-grid';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {ModelColumnModel, ModelModel} from '../../model/model.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {ModelService} from '../../service/model.service';
import {ModelViewEditComponent} from '../../view-edit/model-view-edit.component';

@Component({
	selector: 'model-list',
	templateUrl: 'model-list.component.html'
})

export class ModelListComponent implements OnInit {
	private manufacturerList;
	private assetTypeList;
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
		private modelService: ModelService,
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
				onClick: this.onCreateModel,
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

		this.columnModel = new ModelColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;

		// fetch additional collections
		this.getAssetTypeList();
		this.getManufacturerList();
	}

	private getManufacturerList(): void {
		this.manufacturerService.getManufacturerList()
			.subscribe((list: any) => {
				this.manufacturerList = list;
			})
	}

	private getAssetTypeList(): void {

    }

	private loadData = async (): Promise<ModelModel[]> => {
		try {
			return await this.modelService.getModels().toPromise();
		} catch (error) {
			console.error(error);
		}
	};

	private onCreateModel = async (): Promise<void> => {
		/*try {
			let providerModel: ProviderModel = {
				name: '',
				description: '',
				comment: '',
			};
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ProviderViewEditComponent,
				data: {
					providerModel: providerModel,
					actionType: ActionType.CREATE,
					openFromList: false
				},
				modalConfiguration: {
					title: 'Provider',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}*/
	};

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openModel(event.dataItem, ActionType.VIEW);
		}
	}

	/**
	 * Open The Dialog to Create, View or Edit the Model
	 * @param {ModelModel} modelModel
	 * @param {number} actionType
	 */
	private async openModel(modelModel: ModelModel, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ModelViewEditComponent,
				data: {
					modelModel,
					manufacturerList: this.manufacturerList,
					assetTypeList: this.assetTypeList,
					actionType,
					openFromList
				},
				modalConfiguration: {
					title: 'Model',
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
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: ProviderModel): Promise<void> => {
		/*try {
			if (this.isEditAvailable()) {
				await this.openProvider(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}*/
	};

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	private onDelete = async (dataItem: ProviderModel): Promise<void> => {
		/*try {
			if (this.isDeleteAvailable()) {
				const context = await this.providerService.deleteContext(dataItem.id).toPromise();

				const confirmation = await this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: ProviderAssociatedComponent,
					data: {
						providerAssociatedModel: context,
					},
					modalConfiguration: {
						title: 'Confirmation Required',
						draggable: true,
						modalSize: ModalSize.MD
					}
				}).toPromise();
				if (confirmation) {
					if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
						await this.providerService.deleteProvider(dataItem.id).toPromise();
						await this.gridComponent.reloadData();
					}
				}
			}
		} catch (error) {
			console.error(error);
		}*/
	};

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