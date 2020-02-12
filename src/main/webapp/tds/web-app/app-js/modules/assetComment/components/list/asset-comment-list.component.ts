import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalType} from '../../../../shared/model/constants';
import {CellClickEvent} from '@progress/kendo-angular-grid';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetCommentService} from '../../service/asset-comment.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetCommentColumnModel, AssetCommentModel} from '../../model/asset-comment.model';
import {Permission} from '../../../../shared/model/permission.model';
import {
	ColumnHeaderData,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData
} from 'tds-component-library';
import {AssetCommentViewEditComponent} from '../view-edit/asset-comment-view-edit.component';

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
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected dateFormat = '';

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;
	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private assetCommentService: AssetCommentService
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
			}
		];

		this.headerActions = [];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			showDataReloadButton: false,
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

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
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
			await this.dialogService.extra(AssetCommentViewEditComponent, [
				{
					provide: AssetCommentModel,
					useValue: commentModel,
				}
			]);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	}

	/**
	 * Determine if the user has the permission to edit comments
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}
}
