// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Services
import {APIActionService} from '../../service/api-action.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Components
import {APIActionViewEditComponent} from '../view-edit/api-action-view-edit.component';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
// Models
import {APIActionColumnModel, APIActionModel} from '../../model/api-action.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ActionType} from '../../../../shared/model/data-list-grid.model';
// Kendo
import {PreferenceService} from '../../../../shared/services/preference.service';
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: 'api-action-list',
	templateUrl: 'api-action-list.component.html',
	styles: [`
        #btnCreate {
            margin-left: 16px;
        }

        .action-header {
            width: 100%;
            text-align: center;
        }
	`]
})
export class APIActionListComponent implements OnInit {
	// -------------------------------------
	private gridRowActions: GridRowAction[];

	private headerActions: HeaderActionButtonData[];

	private gridSettings: GridSettings = {
		defaultSort: [{field: 'name', dir: 'asc'}],
		sortSettings: {mode: 'single'},
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
		private apiActionService: APIActionService,
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
				onClick: this.onCreate,
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

		this.columnModel = new APIActionColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;

	}

	/**
	 * Create a new API Action
	 */
	private onCreate = async (): Promise<void> => {
		try {
			let apiActionModel = new APIActionModel();
			await this.openAPIActionDialogViewEdit(apiActionModel, ActionType.CREATE);
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param {APIActionModel} dataItem
	 */
	private onEdit = async (dataItem: APIActionModel): Promise<void> => {
		try {
			let apiAction: APIActionModel = await this.apiActionService.getAPIAction(dataItem.id).toPromise();
			await this.openAPIActionDialogViewEdit(apiAction, ActionType.EDIT, true);
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	private onDelete = async (dataItem: APIActionModel): Promise<void> => {
		try {
			if (this.isDeleteAvailable()) {
				const confirmation = await this.dialogService.confirm('Confirmation Required', 'Confirm deletion of ' + dataItem.name + ' Action?').toPromise();
				if (confirmation) {
					if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
						await this.apiActionService.deleteAPIAction(dataItem.id).toPromise();
						await this.gridComponent.reloadData();
					}
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} dataItem
	 */
	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openAPIActionDialogViewEdit(event.dataItem, ActionType.VIEW);
		}
	}

	private loadData = async (): Promise<APIActionModel[]> => {
		try {
			return await this.apiActionService.getAPIActions().toPromise();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {APIActionModel} apiActionModel
	 * @param {number} actionType
	 */
	private async openAPIActionDialogViewEdit(apiActionModel: APIActionModel, actionType: number, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: APIActionViewEditComponent,
				data: {
					apiActionModel: apiActionModel,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Action',
					draggable: true,
					modalSize: ModalSize.LG
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	// Permissions
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionCreate);
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionEdit);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionDelete);
	}

}
