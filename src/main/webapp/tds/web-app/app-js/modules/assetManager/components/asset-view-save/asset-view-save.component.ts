import {AfterViewInit, Component} from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel, ViewGroupModel } from '../../../assetExplorer/model/view.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import {Permission} from '../../../../shared/model/permission.model';
@Component({
	selector: 'asset-explorer-view-save',
	template: `
        <div class="modal-content asset-explorer-view-save-component">
            <div class="modal-header">
                <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
                <h4 class="modal-title">Save List View</h4>
            </div>
            <div class="modal-body">
                <form name="noticeForm" role="form" data-toggle="validator" class="form-horizontal left-alignment" #noticeForm='ngForm'>
                    <div class="box-body">
                        <div class="form-group">
                            <label for="name" class="col-sm-3 control-label">View Name:
                                <span class="required_field">*</span>
                            </label>
                            <div class="col-sm-9">
                                <input type="text" (keyup)="onNameChanged()" name="name" id="name" class="form-control" placeholder="View Name" [(ngModel)]="model.name" required>
                                <span *ngIf="!isUnique" class="error">{{'DATA_INGESTION.DATA_VIEW' | translate }} name must be unique</span>
                            </div>
                        </div>
                        <div class="form-group">
                            <div *ngIf="isSystemCreatePermitted()" class="checkbox" style="padding-left:160px;">
                                <label>
                                    <input type="checkbox" name="isSystem" [(ngModel)]="model.isSystem" (change)="onIsSystemChange()"> {{ 'ASSET_EXPLORER.SYSTEM_VIEW' | translate }}
                                </label>
                            </div>
                            <div class="checkbox" style="padding-left:160px;">
                                <label [ngClass]="{'disabled-input' : model.isSystem}">
                                    <input type="checkbox" [disabled]="model.isSystem" name="shared" [(ngModel)]="model.isShared">
                                    <span>{{ 'GLOBAL.SHARE_WITH_USERS' | translate }}</span>
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="checkbox" style="padding-left:160px;" (click)="onFavorite()">
                                <i class="fa fa-star-o text-yellow" style="margin-left: -43px;padding:0 10px 10px 10px;font-size: 20px;top:2px;left:28px;position:relative"
                                   [ngClass]="{'fa-star':model.isFavorite,'fa-star-o':!model.isFavorite}"></i>
                                <label style="margin-left:5px">
                                    <input type="checkbox" name="favorite" style="visibility:hidden"> {{ 'GLOBAL.ADD_FAVORITES' | translate }}</label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer form-group-center">
                <tds-button-save
                        class="btn-primary pull-left"
                        (click)="confirmCloseDialog()"
                        [disabled]="!isValid()">
                </tds-button-save>
                <tds-button-cancel
                        (click)="cancelCloseDialog()"
                        class="pull-right">
                </tds-button-cancel>
            </div>
        </div>
	`
})
export class AssetViewSaveComponent implements AfterViewInit {
	model: ViewModel;
	public isUnique = true;
	constructor(
		model: ViewModel,
		private favorites: ViewGroupModel,
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService) {

		this.model = { ...model };
		if (this.model.id) {
			this.model.name = `Copy of ${this.model.name}`;
			this.model.id = null;
			this.model.isSystem = false;
			this.model.isFavorite = false;
		}
		if (this.model.isSystem) {
			this.model.isShared = false;
		}
	}

	ngAfterViewInit(): void {
		if (this.model.name) {
			this.validateUniquenessDataViewByName(this.model.name);
		}
	}

	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	public confirmCloseDialog() {
		this.assetExpService.saveReport(this.model)
			.subscribe(result => result && this.activeDialog.close(result),
			error => this.activeDialog.dismiss(error));
	}

	public isValid(): boolean {
		return this.model.name && this.model.name.trim() !== '' && this.isUnique;
	}

	/**
	 * Disable the System View checkbox if the user does not have the proper permission
	 * @returns {boolean}
	 */
	public isSystemCreatePermitted(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate);
	}

	/**
	 * Should turn isShared to false when isSystem is selected as true.
	 */
	private onIsSystemChange(): void {
		if (this.model.isSystem && this.model.isShared) {
			this.model.isShared = false;
		}
	}

	public onFavorite() {
		if (this.model.isFavorite) {
			this.model.isFavorite = false;
			if (this.model.id) {
				const reportIndex = this.favorites.items.findIndex(x => x.id === this.model.id);
				if (reportIndex !== -1) {
					this.favorites.items.splice(reportIndex, 1);
				}
			}
		} else {
			if (this.assetExpService.hasMaximumFavorites(this.favorites.items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.model.isFavorite = true;
			}
		}

	}

	public onNameChanged() {
		this.validateUniquenessDataViewByName(this.model.name);
	}

	private validateUniquenessDataViewByName(dataViewName = '') {
		if (!dataViewName.trim()) {
			// handle empty string
			this.isUnique = false;
		} else {
			this.assetExpService.validateUniquenessDataViewByName(dataViewName)
				.subscribe((isUnique: boolean) => this.isUnique = isUnique,
					(error) => console.log(error.message));
		}

	}
}