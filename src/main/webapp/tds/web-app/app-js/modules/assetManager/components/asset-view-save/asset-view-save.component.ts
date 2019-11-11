import {AfterViewInit, Component} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ViewModel, ViewGroupModel} from '../../../assetExplorer/model/view.model';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {Permission} from '../../../../shared/model/permission.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {SaveOptions} from '../../../../shared/model/save-options.model';

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
                <form name="noticeForm" role="form" data-toggle="validator" class="form-horizontal left-alignment"
                      #noticeForm='ngForm'>
                    <div class="box-body">
                        <div>
                            <label for="name" class="col-sm-3 control-label">
                                {{ 'ASSET_EXPLORER.SYSTEM_VIEW' | translate }}:
                            </label>
                        </div>
                        <div class="form-group" style="padding-left:160px;">
                            <div *ngIf="hasMaintainAssetList()">
                                <div class="radio">
                                    <div>
                                        <label>
                                            <input type="radio"
                                                   name="radio-mode"
                                                   [value]="SAVE_AS_OPTIONS.MY_VIEW.value"
                                                   [attr.disabled]="SAVE_AS_OPTIONS.MY_VIEW.disabled || null"
                                                   (change) = "onChangeMode()"
                                                   [(ngModel)]="currentSaveOption"
                                                   checked>
                                            <span>{{ 'ASSET_EXPLORER.SAVE_IN_MY_VIEWS' | translate }}</span>
                                        </label>
                                    </div>
                                    <div class="col-sm-9" >
                                        <label for="name" style="padding: 0;font-weight: bold">
                                            {{ 'GLOBAL.VIEW_NAME' | translate }}:*
                                        </label>
                                        <input type="text"
                                               name="name"
                                               id="name"
                                               class="form-control"
                                               placeholder="View Name"
                                               [disabled]="!isSaveInMyViewMode()"
                                               (keyup)="onNameChanged()"
                                               [(ngModel)]="model.name"
                                               required>
                                        <span *ngIf="!isUnique"
                                              class="error">{{'DATA_INGESTION.DATA_VIEW' | translate }} name must be unique</span>

                                        <div class="checkbox">
                                            <label>
                                                <input type="checkbox" name="shared" [disabled]="!isSaveInMyViewMode()" [(ngModel)]="model.isShared">
                                                <span>{{ 'GLOBAL.SHARE_WITH_USERS' | translate }}</span>
                                            </label>
                                        </div>
                                        <div class="checkbox" (click)="onFavorite()" disabled>
                                            <i class="fa fa-star-o text-yellow"
                                               style="margin-left: -43px;padding:0 10px 10px 10px;font-size: 20px;top:2px;left:30px;position: relative"
                                               [ngClass]="{'fa-star':model.isFavorite,'fa-star-o':!model.isFavorite,'disabled':!isSaveInMyViewMode()} "></i>
                                            <label style="margin-left:5px">
                                                <input type="checkbox" name="favorite" [disabled]="!isSaveInMyViewMode()"
                                                       style="visibility:hidden"> {{ 'GLOBAL.ADD_FAVORITES' | translate }}
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div *ngIf="hasMaintainAssetList()">
                                <div class="radio" style="position:inherit">
                                    <label for="overrideMe">
                                        <input id="overrideMe" type="radio" name="radio-mode"
											   [value]="SAVE_AS_OPTIONS.OVERRIDE_FOR_ME.value"
                                               [attr.disabled]="SAVE_AS_OPTIONS.OVERRIDE_FOR_ME.disabled || null"
											   (change) = "onChangeMode()"
                                               [(ngModel)]="currentSaveOption">
                                        <span>{{ 'ASSET_EXPLORER.OVERRIDE_EXISTING_VIEW_ME' | translate }}</span>
                                    </label>
                                </div>
                            </div>
                            <div *ngIf="hasMaintainSystemList()">
                                <div class="radio">
                                    <label for="overrideAll">
                                        <input id="overrideAll" type="radio" name="radio-mode"
											   [value]="SAVE_AS_OPTIONS.OVERRIDE_FOR_ALL.value"
                                               [attr.disabled]="SAVE_AS_OPTIONS.OVERRIDE_FOR_ALL.disabled || null"
											   (change) = "onChangeMode()"
                                               [(ngModel)]="currentSaveOption">
                                        <span>{{ 'ASSET_EXPLORER.OVERRIDE_EXISTING_VIEW_ALL_USERS' | translate }}</span>
                                    </label>
                                </div>
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
	public model: ViewModel;
	public saveOptions: SaveOptions;
	private preModel: ViewModel;
	public isUnique = true;
	public SAVE_AS_OPTIONS = {
		MY_VIEW: {value: 'MY_VIEW', disabled: false},
		OVERRIDE_FOR_ME: {value: 'OVERRIDE_FOR_ME', disabled: false },
		OVERRIDE_FOR_ALL: {value: 'OVERRIDE_FOR_ALL', disabled: false }
	};

	public currentSaveOption: string;

	constructor(
		model: ViewModel,
		private favorites: ViewGroupModel,
		saveOptions: SaveOptions,
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private userContextService: UserContextService,
		private notifier: NotifierService) {
		this.preModel = model;
		this.startModel(model);
		this.saveOptions = saveOptions;
		this.currentSaveOption = this.saveOptions.saveAsOptions[0] || this.SAVE_AS_OPTIONS.MY_VIEW.value;
		this.setDisabling();
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
		if (this.isOverrideAllUsersMode() || this.isOverrideForMeMode()) {
			this.startModel(this.preModel);
		}
		this.assetExpService.saveReport(this.model)
			.subscribe(result => result && this.activeDialog.close(result),
				error => this.activeDialog.dismiss(error));
	}

	public isSaveInMyViewMode(): boolean {
		return this.currentSaveOption === this.SAVE_AS_OPTIONS.MY_VIEW.value;
	}

	public isOverrideForMeMode(): boolean {
		return this.currentSaveOption === this.SAVE_AS_OPTIONS.OVERRIDE_FOR_ME.value;
	}

	public isOverrideAllUsersMode(): boolean {
		return this.currentSaveOption === this.SAVE_AS_OPTIONS.OVERRIDE_FOR_ALL.value;
	}

	public isValid(): boolean {
		return this.model.name && this.model.name.trim() !== '' && this.isUnique;
	}

	public startModel(model) {
		const changes = { name: `Copy of ${model.name}`};
		this.model = {...this.model, ...changes};
	}

	/**
	 * Disable the System View checkbox if the user does not have the proper permission
	 * @returns {boolean}
	 */
	public isSystemCreatePermitted(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate);
	}

	/**
	 * Validate if user can maintain asset lists
	 * @returns {boolean}
	 */
	public hasMaintainAssetList(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	/**
	 * Validate if user can maintain system lists
	 * @returns {boolean}
	 */
	public hasMaintainSystemList(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerSystemList);
	}

	private setDisabling(): void {
		const options = Object.entries(this.SAVE_AS_OPTIONS);
		for (const [key, value] of options) {
			value.disabled = !this.saveOptions.saveAsOptions.includes(key);
		}
	}

	public onFavorite() {
		if(!this.isSaveInMyViewMode()) {
			return;
		}
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