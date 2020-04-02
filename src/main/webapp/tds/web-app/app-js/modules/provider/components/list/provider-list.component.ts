// Angular
import {
	Component,
	ComponentFactoryResolver,
	OnInit,
	ViewChild,
} from '@angular/core';
// Services
import {ProviderService} from '../../service/provider.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Components
import {ProviderViewEditComponent} from '../view-edit/provider-view-edit.component';
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
// Models
import {ActionType} from '../../../dataScript/model/data-script.model';
import {ProviderColumnModel, ProviderModel} from '../../model/provider.model';
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

@Component({
	selector: 'provider-list',
	templateUrl: 'provider-list.component.html'
})
export class ProviderListComponent implements OnInit {
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
		private providerService: ProviderService,
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
				onClick: this.onCreateProvider,
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

		this.columnModel = new ProviderColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openProvider(event.dataItem, ActionType.VIEW);
		}
	}

	private onCreateProvider = async (): Promise<void> => {
		try {
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
		}
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: ProviderModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openProvider(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	private onDelete = async (dataItem: ProviderModel): Promise<void> => {
		try {
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
		}
	}

	private loadData = async (): Promise<ProviderModel[]> => {
		try {
			return await this.providerService.getProviders().toPromise();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private async openProvider(providerModel: ProviderModel, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ProviderViewEditComponent,
				data: {
					providerModel: providerModel,
					actionType: actionType,
					openFromList: openFromList
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
