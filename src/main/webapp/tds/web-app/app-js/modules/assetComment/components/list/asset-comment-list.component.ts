// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Model
import {ModalType} from '../../../../shared/model/constants';
import {AssetCommentColumnModel, AssetCommentModel} from '../../model/asset-comment.model';
import {Permission} from '../../../../shared/model/permission.model';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
// Component
import {AssetCommentViewEditComponent} from '../view-edit/asset-comment-view-edit.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetCommentService} from '../../service/asset-comment.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import {CellClickEvent} from '@progress/kendo-angular-grid';
import {TaskService} from '../../../taskManager/service/task.service';

declare var jQuery: any;

@Component({
	selector: `asset-comment-list`,
	templateUrl: 'asset-comment-list.component.html',
})
export class AssetCommentListComponent implements OnInit {
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
	protected dateFormat = '';

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;
	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		public taskManagerService: TaskService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private assetCommentService: AssetCommentService,
		private translatePipe: TranslatePipe
	) {
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
				onClick: this.openEdit,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isCommentDeleteAvailable(),
				onClick: this.onDelete,
			}
		];

		this.headerActions = [];

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

		this.columnModel = new AssetCommentColumnModel(
			this.dateFormat
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	/**
	 * Check the field clicked and if appropriate open the comment view
	 * @param {SelectionEvent} event
	 */
	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex === 1 && this.isShowCommentAvailable()) {
			await this.openComment(event.dataItem, ModalType.VIEW);
		}
	}

	public loadData = async (): Promise<AssetCommentModel[]> => {
		try {
			let data = await this.assetCommentService.getAssetComments().toPromise();
			return data;
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openEdit = async (dataItem: AssetCommentModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openComment(dataItem, ModalType.EDIT);
				await this.gridComponent.reloadData();
			}
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openView = async (dataItem: AssetCommentModel): Promise<void> => {
		try {
			await this.openComment(dataItem, ModalType.VIEW);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public async openComment(comment: any, action: ModalType): Promise<void> {
		try {
			let commentModel: AssetCommentModel = {
				id: comment.id,
				modal: {
					type: action,
				},
				archive: comment.dateResolved !== null,
				comment: comment.comment,
				category: comment.category,
				assetClass: {
					text: comment.assetType,
				},
				asset: {
					id: comment.assetEntityId,
					text: comment.assetName,
				},
				lastUpdated: comment.lastUpdated,
				dateCreated: comment.dateCreated,
			};

			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AssetCommentViewEditComponent,
				data: {
					assetCommentModel: commentModel
				},
				modalConfiguration: {
					title: 'Comment',
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

	/**
	 * Delete the Asset Comment
	 */
	public onDelete = async (dataItem: AssetCommentModel): Promise<void> => {
		if (this.isCommentDeleteAvailable()) {
			const confirmation = await this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				'Confirm deletion of this record. There is no undo for this action.'
			)
				.toPromise();
			if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
				await this.taskManagerService.deleteTaskComment(dataItem.id).toPromise();
				await this.gridComponent.reloadData();
			}
		}
	}

	/**
	 * Determine if the user has the permission to edit comments
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}

	/**
	 * Determine if the user has the permission to see comment details
	 */
	protected isShowCommentAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentView);
	}

	protected isCommentDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentDelete);
	}
}
