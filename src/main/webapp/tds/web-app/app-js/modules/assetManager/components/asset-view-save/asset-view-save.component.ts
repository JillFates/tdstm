import {AfterViewInit, Component} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ViewModel, ViewGroupModel} from '../../../assetExplorer/model/view.model';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {Permission} from '../../../../shared/model/permission.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import DEFAULT_PROJECT from '../../../../shared/constants/default-project';


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
                                                   [value]="MODES.SMV"
                                                   (change) = "onChangeMode()"
                                                   [(ngModel)]="mode"
                                                   checked>
                                            <span>{{ 'ASSET_EXPLORER.SAVE_IN_MY_VIEWS' | translate }}</span>
                                        </label>
                                    </div>
                                    <div class="col-sm-9" style="padding-left:20px">
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
                            <div *ngIf="hasMaintainAssetList() && !isDefaultProject">
                                <div class="radio" style="position:inherit">
                                    <label for="overrideMe">
                                        <input id="overrideMe" type="radio" name="radio-mode" [value]="MODES.OEVM" (change) = "onChangeMode()"
                                               [(ngModel)]="mode">
                                        <span>{{ 'ASSET_EXPLORER.OVERRIDE_EXISTING_VIEW_ME' | translate }}</span>
                                    </label>
                                </div>
                            </div>
                            <div *ngIf="hasMaintainSystemList()">
                                <div class="radio">
                                    <label for="overrideAll">
                                        <input id="overrideAll" type="radio" name="radio-mode" [value]="MODES.OEVA" (change) = "onChangeMode()"
                                               [(ngModel)]="mode">
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
	private preModel: ViewModel;
	public isUnique = true;
	private readonly MODES = {
		SMV: 'SMV',
		OEVM: 'OEVM',
		OEVA: 'OEVA'
	};
	public mode = this.MODES.SMV;
	private isDefaultProject = false;

	constructor(
		model: ViewModel,
		private favorites: ViewGroupModel,
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private userContextService: UserContextService,
		private notifier: NotifierService) {
		this.preModel = model;
		this.startModel(model);
		this.userContextService
			.getUserContext()
			.subscribe(userContext => {
				this.isDefaultProject = userContext.project.id === DEFAULT_PROJECT.id;
				this.mode = this.isDefaultProject ? this.MODES.OEVA :  this.MODES.SMV;
			})
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
		return this.mode === this.MODES.SMV;
	}

	public isOverrideForMeMode(): boolean {
		return this.mode === this.MODES.OEVM;
	}

	public isOverrideAllUsersMode(): boolean {
		return this.mode === this.MODES.OEVM;
	}

	public isValid(): boolean {
		return this.model.name && this.model.name.trim() !== '' && this.isUnique;
	}

	public startModel(model) {
		this.model = {...model};
		if (this.model.id) {
			const changes = {
				name: `Copy of ${this.model.name}`
			};
			this.model = {...this.model, ...changes};
		}
		if (this.model.isSystem) {
			this.model = {...this.model, isShared: false};
		}
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

	/**
	 * Should turn isShared to false when isSystem is selected as true.
	 */
	private onIsSystemChange(): void {
		if (this.model.isSystem && this.model.isShared) {
			this.model.isShared = false;
		}
	}

	public onFavorite() {
		if (!this.isSaveInMyViewMode()){
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

	public onChangeMode() {
		// if (this.mode !== this.MODES.SMV) {
		// 	this.model.name = this.preModel.name;
		// }
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