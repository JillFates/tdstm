import {Component, OnInit} from '@angular/core';
import {UIExtraDialog} from '../../../../services/ui-dialog.service';
import {TranslatePipe} from '../../../../pipes/translate.pipe';

import {BulkChangeModel, BulkChangeType} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../directives/ui-prompt.directive';
import {UIDialogService} from '../../../../services/ui-dialog.service';
import {BulkActions, BulkActionResult} from '../../model/bulk-change.model';
import {BulkChangeService} from '../../../../services/bulk-change.service';
import {Permission} from '../../../../model/permission.model';
import {PermissionService} from '../../../../services/permission.service';
import {BulkChangeEditComponent} from '../bulk-change-edit/bulk-change-edit.component';
import { APIActionService } from '../../../../../modules/apiAction/service/api-action.service';
import { NotifierService } from '../../../../services/notifier.service';
import {AlertType} from '../../../../model/alert.model';
@Component({
	selector: 'tds-bulk-change-actions',
	templateUrl: 'bulk-change-actions.component.html',
	providers: [TranslatePipe, APIActionService]
})
export class BulkChangeActionsComponent extends UIExtraDialog  implements OnInit {
	private readonly SELECT_DATA_MODEL = {
		id: -1,
		name: 'GLOBAL.PLEASE_SELECT',
		isAutoProcess: false
	};
	protected bulkChangeType: BulkChangeType;
	protected selectedItems: number[] = [];
	protected selectedAction: BulkActions;
	protected affected: number;
	protected ACTION = BulkActions; // Make enum visible to the view
	protected ACTION_TYPE = BulkChangeType;
	protected itemType: string;
	public showEdit: boolean;
	public showDelete: boolean;
	public showRun = false;
	public sendEmailNotification = false;
	public dataScriptOptions: Array<any> = [this.SELECT_DATA_MODEL];
	public selectedScriptOption = this.dataScriptOptions[0];

	constructor(
		private bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private bulkChangeService: BulkChangeService,
		private permissionService: PermissionService,
		private dialogService: UIDialogService,
		private apiActionService: APIActionService,
		private notifier: NotifierService,
		private translatePipe: TranslatePipe) {
			super('#bulk-change-action-component');
			this.selectedItems = this.bulkChangeModel.selectedItems || [];
			this.affected = this.bulkChangeModel.affected;
			this.selectedAction = this.bulkChangeModel.showEdit ? this.ACTION.Edit : this.ACTION.Delete;
			this.showDelete = this.bulkChangeModel.showDelete;
			this.showEdit = this.bulkChangeModel.showEdit;
			this.bulkChangeType = this.bulkChangeModel.bulkChangeType;
			this.itemType =  this.bulkChangeType === BulkChangeType.Assets ?
				this.getSinglePluralAssetName() : this.getSinglePluralDependenceName()
	}

	ngOnInit() {
		// Enable ETL options just for Assets
		if (this.bulkChangeType === BulkChangeType.Assets) {
			this.apiActionService.getDataScripts({useWithAssetActions: true, isAutoProcess: true}).subscribe(result => {
				if (Array.isArray(result) && result.length > 0) {
					this.showRun = true;
					this.dataScriptOptions = [this.SELECT_DATA_MODEL, ...result];
				} else {
					this.showRun = false;
				}
			});
		}
	}

	public cancelCloseDialog(bulkOperationResult: BulkActionResult): void {
		this.dismiss(bulkOperationResult || {action: null, success: false});
	}

	public closeDialog(bulkOperationResult: BulkActionResult): void {
		this.close(bulkOperationResult);
	}

	onNext(): void {
		switch (this.selectedAction) {
			case this.ACTION.Edit:
				this.editAction();
				break;
			case this.ACTION.Delete:
				this.deleteAction();
				break;
			case this.ACTION.Run:
				this.runAction();
				break;
		}
	}

	private editAction(): void {
		const bulkChangeModel: BulkChangeModel = {
			selectedItems: this.selectedItems,
			selectedAssets: this.bulkChangeModel.selectedAssets,
			affected: this.bulkChangeModel.affected
		};

		this.dialogService.extra(BulkChangeEditComponent, [
			UIDialogService,
			{provide: BulkChangeModel, useValue: bulkChangeModel}
		], true, false).then(bulkOperationResult => {
			this.closeDialog(bulkOperationResult);
		}).catch(err => {
			this.cancelCloseDialog(err);
		});
	}

	private deleteAction(): void  {
		this.confirmDelete()
			.then(this.deleteBulk.bind(this))
			.then(this.closeDialog.bind(this))
			.catch((result) => this.cancelCloseDialog(result))
	}

	private runAction(): void  {
		this.confirmRun()
			.then(this.runBulk.bind(this))
			.then(this.closeDialog.bind(this))
			.catch((result) => this.cancelCloseDialog(result))
	}

	private confirmDelete(): Promise<boolean> {
		const translationKey = this.bulkChangeType === BulkChangeType.Assets
			? 'ASSET_EXPLORER.BULK_CHANGE.DELETE.CONFIRM_DELETE_ASSETS'
			: 'ASSET_EXPLORER.BULK_CHANGE.DELETE.CONFIRM_DELETE_DEPENDENCIES';

		const singleOrPluralName = this.bulkChangeType === BulkChangeType.Assets ?
			this.getSinglePluralAssetName() : this.getSinglePluralDependenceName();

		const message = this.translatePipe.transform(translationKey,
			[this.affected, singleOrPluralName, singleOrPluralName]);
		return new Promise((resolve, reject) =>  {
			this.promptService.open(this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONTINUE_WITH_CHANGES'),
				message,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then((result) => result ? resolve() : reject({action: BulkActions.Delete, success: false, message: 'canceled'}))
		})
	}

	private confirmRun(): Promise<boolean> {
		const translationKey =
		this.bulkChangeType === BulkChangeType.Assets
			? 'ASSET_EXPLORER.BULK_CHANGE.RUN.CONFIRM_RUN_ASSETS'
			: 'ASSET_EXPLORER.BULK_CHANGE.RUN.CONFIRM_RUN_DEPENDENCIES';
		const singleOrPluralName =
		this.bulkChangeType === BulkChangeType.Assets
			? this.getSinglePluralAssetName()
			: this.getSinglePluralDependenceName();
		const message = this.translatePipe.transform(translationKey, [
			this.affected,
			singleOrPluralName,
			singleOrPluralName
		]);
		return new Promise((resolve, reject) => {
			this.promptService
				.open(
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
					),
					message,
					this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL')
					)
				.then(result =>
					result
						? resolve()
						: reject({
							action: BulkActions.Run,
							success: false,
							message: 'canceled'
						})
				);
			});
	}

	private runBulk(): Promise<BulkActionResult> {
		return new Promise((resolve, reject) => {
			if (this.hasAssetRunPermission()) {
				const userParams = { sortDomain: 'device', sortProperty: 'id', filters: {domains: ['device']}};
				const payload = {
					userParams,
					dataViewId: this.bulkChangeModel.viewId,
					ids: this.bulkChangeModel.selectedItems,
					dataScriptId: this.selectedScriptOption.id
				};
				this.bulkChangeService.bulkRun(payload).subscribe(
					result => {
						this.notifier.broadcast({
							name: AlertType.SUCCESS,
							message: 'The ETL import process was succesfully initiated'
						});

						return resolve({
							action: BulkActions.Run,
							success: true,
							message: result.message || result.resp
						})
					}
					,
					err => {
						this.notifier.broadcast({
							name: AlertType.DANGER,
							message: err.message || err
						});
						return reject({
							action: BulkActions.Run,
							success: false,
							message: err.message || err
						})
					}
					);
				} else {
					reject({
						action: BulkActions.Run,
						success: false,
						message: 'Forbidden operation'
					});
				}
			});
	}

	private deleteBulk(): Promise<BulkActionResult> {
		const items = this.selectedItems.map((value: number) => value.toString());
		return new Promise((resolve, reject) =>  {
			if (this.hasAssetDeletePermission()) {
				this.bulkChangeService
					.bulkDelete(this.bulkChangeType, items)
					.subscribe(
						result => resolve({action: BulkActions.Delete, success: true, message: result.message || result.resp}),
						err => reject({action: BulkActions.Delete, success: false, message: err.message || err}));
			} else {
				reject({action: BulkActions.Delete, success: false, message: 'Forbidden operation' });
			}
		});
	}

	private hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	private hasAssetRunPermission(): boolean {
		return true;
	}

	/*
	* Based on effected elements, get the single/plural entity name
	 */
	protected getSinglePluralDependenceName() {
		const targetKey = this.affected === 1 ?
			'DEPENDENCIES.SINGLE_NAME' : 'DEPENDENCIES.PLURAL_NAME';

		return this.translatePipe.transform(targetKey);
	}

	/*
	* Based on effected elements, get the single/plural asset name
	 */
	protected getSinglePluralAssetName() {
		const targetKey = this.affected === 1 ?
			'ASSETS.SINGLE_NAME' : 'ASSETS.PLURAL_NAME';

		return this.translatePipe.transform(targetKey);
	}

}
