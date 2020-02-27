// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Services
import {CredentialService} from '../../service/credential.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Components
import {CredentialViewEditComponent} from '../view-edit/credential-view-edit.component';
// Models
import {Permission} from '../../../../shared/model/permission.model';
import {CredentialColumnModel, CredentialModel} from '../../model/credential.model';
import {
	ActionType
} from '../../../../shared/model/data-list-grid.model';
import {
	ColumnHeaderData,
	DialogConfirmAction,
	DialogService, GridComponent, GridModel,
	GridRowAction, GridSettings,
	HeaderActionButtonData,
	ModalSize
} from 'tds-component-library';
// Kendo
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: 'credential-list',
	templateUrl: 'credential-list.component.html',
})
export class CredentialListComponent implements OnInit {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{field: 'name', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		selectableSettings: {enabled: true, mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected noticeTypes = [];
	protected actionType = ActionType;
	protected dateFormat = '';

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private credentialService: CredentialService,
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

		this.columnModel = new CredentialColumnModel(
			this.dateFormat,
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	/**
	 * Create a new API Action
	 */
	private onCreate = async (): Promise<void> => {
		try {
			let creationModel = new CredentialModel();
			this.openCredentialDialogViewEdit(creationModel, ActionType.CREATE);
		} catch (error) {
			console.log(error);
		}
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	public onEdit = async (dataItem: any): Promise<void> => {
		try {
			let credential: CredentialModel = await this.credentialService.getCredential(dataItem.id).toPromise();
			await this.openCredentialDialogViewEdit(credential, ActionType.EDIT, true);
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	protected onDelete = async (dataItem: any): Promise<void> => {
		try {
			const confirmation = await this.dialogService.confirm('Confirmation Required', 'Confirm deletion of ' + dataItem.name + ' credential?').toPromise();
			if (confirmation) {
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					await this.credentialService.deleteCredential(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openCredentialDialogViewEdit(event.dataItem, ActionType.VIEW);
		}
	}

	/**
	 * Load the Data
	 */
	private loadData = async (): Promise<CredentialModel[]> => {
		try {
			return await this.credentialService.getCredentials().toPromise();
		} catch (error) {
			console.log(error);
		}
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {CredentialModel} credentialModel
	 * @param {number} actionType
	 */
	private openCredentialDialogViewEdit = async (credentialModel: CredentialModel, actionType: ActionType, openFromList = false): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: CredentialViewEditComponent,
				data: {
					credentialModel: credentialModel,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Credential',
					draggable: true,
					modalSize: ModalSize.LG
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.log(error);
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
