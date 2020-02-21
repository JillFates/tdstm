// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Services
import {DataScriptService} from '../../service/data-script.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Components
import {DataScriptViewEditComponent} from '../view-edit/data-script-view-edit.component';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DataScriptEtlBuilderComponent} from '../etl-builder/data-script-etl-builder.component';
// Models
import {
	DataScriptColumnModel,
	DataScriptModel,
	ActionType,
} from '../../model/data-script.model';
import {Permission} from '../../../../shared/model/permission.model';
// Others
import {
	CellClickEvent,
} from '@progress/kendo-angular-grid';

@Component({
	selector: 'data-script-list',
	templateUrl: 'data-script-list.component.html'
})
export class DataScriptListComponent implements OnInit {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{field: 'title', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected actionType = ActionType;
	protected dateFormat = '';

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private dataScriptService: DataScriptService,
		private preferenceService: PreferenceService,
		private translateService: TranslatePipe
	) {
	}

	/**
	 * Initialize the grid settings.
	 */
	async ngOnInit() {
		this.gridRowActions = [
			{
				name: 'View',
				show: true,
				disabled: false,
				onClick: this.openView
			},
			{
				name: 'Edit',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.openEdit,
			},
			{
				name: 'Edit Script',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.openEditScript,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.openDelete,
			},
		];

		this.headerActions = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateScript,
			},
		];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			loadData: this.loadData,
		};

		this.dateFormat = await this.preferenceService
			.getUserDatePreferenceAsKendoFormat()
			.toPromise();

		this.columnModel = new DataScriptColumnModel(
			this.dateFormat
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openScript(event.dataItem.id, ActionType.VIEW);
		}
	}

	public loadData = async (): Promise<DataScriptModel[]> => {
		try {
			let data = await this.dataScriptService.getDataScripts().toPromise();
			return data;
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openEdit = async (dataItem: DataScriptModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openScript(dataItem.id, ActionType.EDIT, true);
				await this.gridComponent.reloadData();
			}
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openEditScript = async (dataItem: DataScriptModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openETLBuilder(dataItem.id);
				await this.gridComponent.reloadData();
			}
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openDelete = async (dataItem: DataScriptModel): Promise<void> => {
		try {
			const confirmation = await this.dialogService.confirm(
				'Confirmation Required',
				'You are about to delete the selected data script. Do you want to proceed?'
			).toPromise();
			if (confirmation) {
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					await this.dataScriptService.deleteDataScript(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
				}
			}
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openView = async (dataItem: DataScriptModel): Promise<void> => {
		try {
			await this.openScript(dataItem.id, ActionType.VIEW);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public onCreateScript = async (): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: DataScriptViewEditComponent,
				data: {
					dataScriptModel: new DataScriptModel(),
					actionType: ActionType.CREATE,
					openFromList: false
				},
				modalConfiguration: {
					title: 'ETL Script',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public async openScript(id: number, action: ActionType, openFromList = false): Promise<void> {
		try {
			const script = await this.dataScriptService.getETLScript(id).toPromise();
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: DataScriptViewEditComponent,
				data: {
					dataScriptModel: script.data.dataScript,
					actionType: action,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'ETL Script',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	}

	public async openETLBuilder(id: number): Promise<void> {
		try {
			const script = await this.dataScriptService.getETLScript(id).toPromise();
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: DataScriptEtlBuilderComponent,
				data: {
					dataScriptModel: script.data.dataScript
				},
				modalConfiguration: {
					title: 'ETL Script',
					draggable: true,
					resizable: true,
					modalSize: ModalSize.XL
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	}

	/**
	 * Determine if the user has the permission to edit data scripts
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ETLScriptUpdate);
	}

	/**
	 * Determine if the user has the permission to create data scripts
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ETLScriptCreate);
	}
}
