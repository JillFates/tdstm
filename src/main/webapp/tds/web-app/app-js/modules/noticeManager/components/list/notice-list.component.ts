// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Components
import { NoticeViewEditComponent } from '../view-edit/notice-view-edit.component';
import {DialogConfirmAction, DialogService, GridComponent, ModalSize} from 'tds-component-library';
// Service
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { NoticeService } from '../../service/notice.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

import { PreferenceService } from '../../../../shared/services/preference.service';
// Model
import { Permission } from '../../../../shared/model/permission.model';
import {
	NoticeColumnModel,
	NoticeModel,
	NoticeTypes,
} from '../../model/notice.model';
import { ActionType } from '../../../../shared/model/action-type.enum';
import {
	GridRowAction,
	HeaderActionButtonData,
	ColumnHeaderData,
	GridSettings,
	GridModel,
	DropdownFilterData,
	DropdownData,
} from 'tds-component-library';

// Kendo
import { CellClickEvent } from '@progress/kendo-angular-grid';

@Component({
	selector: 'tds-notice-list',
	templateUrl: 'notice-list.component.html',
})
export class NoticeListComponent implements OnInit {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{ field: 'title', dir: 'asc' }],
		sortSettings: { mode: 'single' },
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

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private noticeService: NoticeService,
		private translateService: TranslatePipe
	) {
		this.noticeTypes = [...NoticeTypes];
	}

	/**
	 * Initialize the grid settings.
	 */
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
				onClick: this.onCreateNotice,
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

		let noticeDropdownData: DropdownData[] = [];
		this.noticeTypes.forEach(x =>
			// We need to use the name as the value, instead of the typeId, as
			// we don't have a way of passing a formatting function to the tds-
			// grid for this column;
			noticeDropdownData.push({ text: x.name, value: x.name })
		);

		let noticeTypesDropdown: DropdownFilterData = {
			data: noticeDropdownData,
			defaultItem: { text: 'Please Select', value: null },
		};

		this.columnModel = new NoticeColumnModel(
			this.dateFormat,
			noticeTypesDropdown
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openNotice(event.dataItem.id, ActionType.View, false);
		}
	}

	public loadData = async (): Promise<NoticeModel[]> => {
		try {
			let data = await this.noticeService.getNoticesList().toPromise();
			// Get the description text of the notices types
			data.forEach((notice: NoticeModel) => {
				notice.typeId = this.noticeTypes.find(
					x => x.typeId === notice.typeId
				).name;
			});

			return data;
		} catch (error) {
			console.error(error);
		}
	};

	public onEdit = async (dataItem: NoticeModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openNotice(dataItem.id, ActionType.Edit, true);
				await this.gridComponent.reloadData();
			}
		} catch (error) {
			console.error(error);
		}
	};

	public onDelete = async (dataItem: NoticeModel): Promise<void> => {
		try {
			const confirmation = await this.dialogService.confirm(
				'Confirmation Required',
				'You are about to delete the selected notice. Do you want to proceed?',
			).toPromise();
			if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
				await this.noticeService
					.deleteNotice(dataItem.id.toString())
					.toPromise();
				await this.gridComponent.reloadData();
			}
		} catch (error) {
			console.error(error);
		}
	};

	public onCreateNotice = async (): Promise<void> => {
		try {
			const data = await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: NoticeViewEditComponent,
				data: {
					noticeModel: new NoticeModel(),
					actionType: ActionType.Create,
					openFromList: false
				},
				modalConfiguration: {
					title: 'Notice Create',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-notice-edit-view-create'
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	};

	public async openNotice(id: number, action: ActionType, openFromList = false): Promise<void> {
		try {
			const notice = await this.noticeService.getNotice(id).toPromise();
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: NoticeViewEditComponent,
				data: {
					noticeModel: notice as NoticeModel,
					actionType: action,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Notice',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-notice-edit-view-create'
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Determine if the user has the permission to edit notices
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeEdit);
	}

	/**
	 * Determine if the user has the permission to create notices
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeCreate);
	}
}
