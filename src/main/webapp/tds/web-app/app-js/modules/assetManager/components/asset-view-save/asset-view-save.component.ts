// Angular
import { AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
// Model
import { ViewModel, ViewGroupModel } from '../../../assetExplorer/model/view.model';
import { AlertType } from '../../../../shared/model/alert.model';
import { Permission } from '../../../../shared/model/permission.model';
import { Dialog, DialogButtonType, KEYSTROKE } from 'tds-component-library';
// Service
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import { ActivatedRoute } from '@angular/router';
import { AssetViewSaveOptions } from '../../models/asset-view-save-options.model';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { ProjectService } from '../../../project/service/project.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'asset-explorer-view-save',
	template: `
		<div class="asset-explorer-view-save-component">
			<form name="saveForm" role="form" data-toggle="validator" class="form-horizontal left-alignment"
						#saveForm='ngForm'>
				<div class="box-body">
					<div class="form-group save-views-container">
						<div class="col-sm-12">
							<div class="checkbox">
								<clr-radio-wrapper class="inline">
										<input clrRadio type="radio"
												 [value]="saveAsOptions.MY_VIEW.value"
												 [name]="'radio-mode'"
												 (change)="onIsSystemChange()"
												 [disabled]="saveAsOptions.MY_VIEW.disabled || null"
												 [(ngModel)]="model.saveAsOption">
									<label class="clr-control-label inline">
										{{ 'ASSET_EXPLORER.SAVE_IN_MY_VIEWS' | translate }}
									</label>
								</clr-radio-wrapper>
							</div>
						</div>
						<label for="name" class="col-sm-4 control-label">View Name:
							<span class="required_field">*</span>
						</label>
						<div class="col-sm-8">
							<input #inputText type="text" (keyup)="onNameChanged()" name="name" id="name" class="form-control"
										 [disabled]="!isSaveInMyViewMode()"
										 placeholder="{{'ASSET_EXPLORER.PLACEHOLDER_VIEW_NAME' | translate}}" [(ngModel)]="model.name" required>
							<span *ngIf="!isUnique" class="error">{{'DATA_INGESTION.DATA_VIEW' | translate }}
								name must be unique</span>
						</div>
						<div class="col-sm-8 col-sm-offset-4">
							<div class="checkbox">
								<clr-checkbox-wrapper class="inline">
									<input clrCheckbox type="checkbox"
												 [name]="'shared'"
												 [disabled]="!isSaveInMyViewMode()"
												 [(ngModel)]="model.isShared">
									<label class="clr-control-label inline" [ngClass]="{'disabled-input' : model.isSystem}">
										{{ 'GLOBAL.SHARE_WITH_USERS' | translate }}
									</label>
								</clr-checkbox-wrapper>
							</div>
						</div>
						<div class="col-sm-8 col-sm-offset-4">
							<div class="checkbox favorite inline"
									 (click)="isSaveInMyViewMode() && onFavorite()">
								<i class="fa fa-star-o text-yellow"
									 [ngClass]="{'fa-star':model.isFavorite,'fa-star-o':!model.isFavorite}"></i>
                                <label class="clr-control-label clr-control-label-sm inline">
                                    {{ 'GLOBAL.ADD_FAVORITES' | translate }}
                                </label>
							</div>
						</div>
					</div>
					<div class="form-group save-views-container no-margin-bottom" *ngIf="saveOptions.canOverride">
						<div class="col-sm-12">
							<div class="checkbox">
								<clr-radio-wrapper class="inline"
																	 [ngClass]="{'disabled': saveAsOptions.OVERRIDE_FOR_ME.disabled || null}">
									<input clrRadio type="radio"
												 [value]="saveAsOptions.OVERRIDE_FOR_ME.value"
												 [name]="'radio-mode'"
												 [attr.disabled]="saveAsOptions.OVERRIDE_FOR_ME.disabled || null"
												 [(ngModel)]="model.saveAsOption">
									<label class="clr-control-label inline">
										{{ 'ASSET_EXPLORER.OVERRIDE_EXISTING_VIEW_ME' | translate }}
									</label>
								</clr-radio-wrapper>
							</div>
						</div>
					</div>
					<div class="form-group save-views-container" *ngIf="saveOptions.canOverride">
						<div class="col-sm-12">
							<div class="checkbox">
								<clr-radio-wrapper class="inline"
																	 [ngClass]="{'disabled': saveAsOptions.OVERRIDE_FOR_ALL.disabled || null}">
									<input clrRadio type="radio"
												 [value]="saveAsOptions.OVERRIDE_FOR_ALL.value"
												 [name]="'radio-mode'"
												 [(ngModel)]="model.saveAsOption">
									<label class="clr-control-label inline">
										{{ 'ASSET_EXPLORER.OVERRIDE_EXISTING_VIEW_ALL_USERS' | translate }}
									</label>
								</clr-radio-wrapper>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	`
})
export class AssetViewSaveComponent extends Dialog implements OnInit, AfterViewInit {
	@ViewChild('inputText', {static: false}) inputText: ElementRef;
	model: ViewModel;
	saveOptions: AssetViewSaveOptions;
	preModel: ViewModel;
	public isUnique = true;
	public favorites: ViewGroupModel;
	public saveAsOptions = {
		MY_VIEW: {value: E_SAVE_AS_OPTIONS.MY_VIEW, disabled: false, isOverride: false},
		OVERRIDE_FOR_ME: {value: E_SAVE_AS_OPTIONS.OVERRIDE_FOR_ME, disabled: false, isOverride: true },
		OVERRIDE_FOR_ALL: {value: E_SAVE_AS_OPTIONS.OVERRIDE_FOR_ALL, disabled: false, isOverride: true }
	};
	private readonly defaultSaveOptions = {
		canOverride: true,
		canShare: true,
		save: true,
		saveAsOptions: [ E_SAVE_AS_OPTIONS.MY_VIEW ]
	}
	private isUserOnDefaultProject: boolean;

	constructor(
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService,
		private activatedRoute: ActivatedRoute,
		private promptService: UIPromptService,
		private projectService: ProjectService,
		private translatePipe: TranslatePipe) {
		super();
		this.projectService.isUserOnDefaultProject().subscribe(result => this.isUserOnDefaultProject = result);
	}

	ngOnInit(): void {
		this.model = R.clone(this.data.viewModel);
		this.favorites = R.clone(this.data.viewGroupModel);
		if (this.model.id) {
			this.model.name = `Copy of ${ this.model.name }`;
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
		this.preModel = this.data.viewModel;
		if (!this.data.saveOptions) {
			this.saveOptions = this.defaultSaveOptions;
		} else {
			this.saveOptions = this.data.saveOptions;
		}
		this.activatedRoute.queryParams.subscribe( (params) => {
			this.preModel.querystring = params;
		})
		this.startModel();
		this.setDisabling();
	}

	ngAfterViewInit(): void {
		if (this.model.name) {
			this.validateUniquenessDataViewByName(this.model.name);
		}
		// focus form to enable the exit on ESC key event.
		setTimeout(() => this.inputText.nativeElement.focus(), 500);
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	public cancelCloseDialog(): void {
		super.onCancelClose();
	}

	confirmCloseDialog() {
		if (this.isOverrideAllUsersMode() && this.isUserOnDefaultProject) {
			this.confirmSaveForGlobalSharedView().then(result => {
				if (result) {
					this.saveModel();
				} else {
					this.cancelCloseDialog();
				}
			})
		} else {
			this.saveModel();
		}
	}

	/**
	 * Saves the view.
	 */
	private saveModel(): void {
		const tmpModel = this.extractModel(this.model);
		this.assetExpService.saveReport(tmpModel)
			.subscribe(result => {
				if (result) {
					this.cancelCloseDialog();
				}
			}, error => this.activeDialog.dismiss(error));
	}

	/**
	 * Prompts for a confirmation on changing a global shared view.
	 */
	private confirmSaveForGlobalSharedView(): Promise<boolean> {
		const message = 'Adding or Changing this Global view will affect all existing and future projects. Click Confirm to update view, otherwise click Cancel';
		return this.promptService.open(this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONTINUE_WITH_CHANGES'),
			message,
			this.translatePipe.transform('GLOBAL.CONFIRM'),
			this.translatePipe.transform('GLOBAL.CANCEL'));
	}

	isSaveInMyViewMode(): boolean {
		return this.model.saveAsOption === this.saveAsOptions.MY_VIEW.value;
	}

	isOverrideForMeMode(): boolean {
		return this.model.saveAsOption === this.saveAsOptions.OVERRIDE_FOR_ME.value;
	}

	isOverrideAllUsersMode(): boolean {
		return this.model.saveAsOption === this.saveAsOptions.OVERRIDE_FOR_ALL.value;
	}

	public isValid(): boolean {
		if (this.isSaveInMyViewMode()) {
			return this.model.name && this.model.name.trim() !== '' && this.isUnique;
		}
		return true;
	}

	public startModel() {
		const changes = this.preModel.name ? { name: `Copy of ${this.preModel.name}`} : '';
		this.model = {...this.preModel, ...changes};
		if (this.saveOptions) {
			this.model.saveAsOption = this.saveOptions.saveAsOptions[0];
		} else {
			this.model.saveAsOption = this.saveAsOptions.MY_VIEW.value;
		}
	}

	private setDisabling(): void {
		if (this.saveOptions) {
			const options = Object.entries(this.saveAsOptions);
			for (const [key, value] of options) {
				value.disabled = !this.saveOptions.saveAsOptions.includes(key);
			}
		}
	}

	/**
	 * Disable the System View checkbox if the user does not have the proper permission
	 * @returns {boolean}
	 */
	public isSystemCreatePermitted(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate);
	}

	public onFavorite() {
		if (!this.isSaveInMyViewMode()) {
			return;
		}
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

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Should turn isShared to false when isSystem is selected as true.
	 */
	private onIsSystemChange(): void {
		if (this.model.isSystem && this.model.isShared) {
			this.model.isShared = false;
		}
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

	private extractModel(model: ViewModel) {
		const tmpModel = Object.assign({}, model);
		const saveOption = this.saveAsOptions[this.model.saveAsOption];
		if (saveOption && saveOption.isOverride) {
			tmpModel.id = null
			tmpModel.overridesView = model.id;
			tmpModel.name = this.preModel.name;
		}
		return tmpModel;
	}
}

export enum E_SAVE_AS_OPTIONS {
	MY_VIEW= 'MY_VIEW',
	OVERRIDE_FOR_ME = 'OVERRIDE_FOR_ME',
	OVERRIDE_FOR_ALL = 'OVERRIDE_FOR_ALL'
}
