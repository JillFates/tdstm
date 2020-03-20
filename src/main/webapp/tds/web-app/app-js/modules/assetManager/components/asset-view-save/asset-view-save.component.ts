// Angular
import {AfterViewInit, Component, OnInit} from '@angular/core';
// Model
import {ViewModel, ViewGroupModel} from '../../../assetExplorer/model/view.model';
import {AlertType} from '../../../../shared/model/alert.model';
import {Permission} from '../../../../shared/model/permission.model';
import {Dialog, DialogButtonType} from 'tds-component-library';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';

@Component({
	selector: 'asset-explorer-view-save',
	template: `
		<div class="asset-explorer-view-save-component">
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
						<div class="col-sm-9 col-sm-offset-3">
							<div *ngIf="isSystemCreatePermitted()" class="checkbox">
								<clr-checkbox-wrapper class="inline">
									<input clrCheckbox type="checkbox"
												 [name]="'isSystem'"
												 (change)="onIsSystemChange()"
												 [(ngModel)]="model.isSystem">
									<label class="clr-control-label inline">
										{{ 'ASSET_EXPLORER.SYSTEM_VIEW' | translate }}
									</label>
								</clr-checkbox-wrapper>
							</div>
							<div class="checkbox" >
								<clr-checkbox-wrapper class="inline">
									<input clrCheckbox type="checkbox"
												 [name]="'shared'"
												 [disabled]="model.isSystem"
												 [(ngModel)]="model.isShared">
									<label class="clr-control-label inline" [ngClass]="{'disabled-input' : model.isSystem}">
										{{ 'GLOBAL.SHARE_WITH_USERS' | translate }}
									</label>
								</clr-checkbox-wrapper>
							</div>
						</div>
					</div>
					<div class="form-group">
						<div class="col-sm-9 col-sm-offset-3">
							<div class="checkbox favorite inline"
									 (click)="onFavorite()">
								<i class="fa fa-star-o text-yellow"
									 [ngClass]="{'fa-star':model.isFavorite,'fa-star-o':!model.isFavorite}"></i>
								<label (click)="onFavorite()">
									<input type="checkbox"
												 name="favorite"
												 style="visibility:hidden"
												 (click)="onFavorite()">
									{{ 'GLOBAL.ADD_FAVORITES' | translate }}
								</label>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	`
})
export class AssetViewSaveComponent extends Dialog implements OnInit, AfterViewInit {
	public isUnique = true;
	public model: ViewModel;
	public favorites: ViewGroupModel;

	constructor(
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService) {
		super();
	}

	ngOnInit(): void {
		this.model = R.clone(this.data.viewModel);
		this.favorites = R.clone(this.data.viewGroupModel);

		if (this.model.id) {
			this.model.name = `Copy of ${this.model.name}`;
			this.model.id = null;
			this.model.isSystem = false;
			this.model.isFavorite = false;
		}
		if (this.model.isSystem) {
			this.model.isShared = false;
		}

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.isValid(),
			type: DialogButtonType.ACTION,
			action: this.confirmCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});
	}

	ngAfterViewInit(): void {
		if (this.model.name) {
			this.validateUniquenessDataViewByName(this.model.name);
		}
	}

	public cancelCloseDialog(): void {
		super.onCancelClose();
	}

	public confirmCloseDialog() {
		this.assetExpService.saveReport(this.model)
			.subscribe(result => result && super.onAcceptSuccess(result),
			error => super.onCancelClose(error));
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
				const reportIndex = this.favorites.views.findIndex(x => x.id === this.model.id);
				if (reportIndex !== -1) {
					this.favorites.views.splice(reportIndex, 1);
				}
			}
		} else {
			if (this.assetExpService.hasMaximumFavorites(this.favorites.views.length + 1)) {
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

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
