import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetShowComponent} from '../asset/asset-show.component';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetModalModel} from '../../model/asset-modal.model';

@Component({
	selector: `tds-asset-clone-modal`,
	template: `
        <div tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="asset-clone-component modal fade in tds-ui-modal-decorator" id="asset-clone-component" data-backdrop="static" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content resizable tds-angular-component-content"
                     tds-ui-modal-decorator=""
                     [options]="modalOptions"
                     [style.width.px]="750"
                     [style.height]="'auto'">

                    <div class="modal-header">
                        <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                        <h4 class="modal-title">Clone Asset</h4>
                    </div>
                    <div class="modal-body">
                        <form role="form" class="form-horizontal">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="form-group">
                                        <label for="newAssetName" class="col-sm-3 control-label"> New asset name</label>
                                        <div class="col-sm-6">
                                            <input type="text" id="newAssetName" name="newAssetName" [(ngModel)]="assetName" class="form-control" (keyup)="isAssetUnique()" #newAssetName/>
                                            <label class="asset-name-validations">
                                                <span class="asset-unique-name" *ngIf="!uniqueAssetName && !existAsset"> Change name appropriately </span>
                                                <span *ngIf="newAssetName.value.length == 0"> Asset name is required </span>
                                                <span *ngIf="existAsset"> Name already exists
                                            <span (click)="showAssetDetailView()" class="link-detail-asset-view"> click here to view</span>
                                        </span>
                                            </label>
                                        </div>
                                        <div class="align-controls">
                                            <tds-popup-asset-message [message]="ASSET_NAME_POPUP_MESSAGE"></tds-popup-asset-message>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="includeDependencies" class="col-sm-3 control-label"> Include Dependencies</label>
                                        <div class="col-sm-2 align-controls">
                                            <input type="checkbox" id="includeDependencies" name="includeDependencies" #includeDependencies/>
                                            <tds-popup-asset-message [message]="'Clone all existing dependencies as well but will change the status of each to Questioned.'"></tds-popup-asset-message>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer form-group-center">
                        <button type="button" class="btn btn-default pull-left" *ngIf="canCloneAssets()"
                                [disabled]="newAssetName.value.length == 0"
                                (click)="cloneAssetValidations(true)">
                            <span class="glyphicon glyphicon-edit"></span>
                            <span>Clone & Edit</span>
                        </button>
                        <button type="button" class="btn btn-default pull-left" *ngIf="canCloneAssets()"
                                [disabled]="newAssetName.value.length == 0"
                                (click)="cloneAssetValidations(false)">
                            <span  class="glyphicon glyphicon-duplicate"></span>
                            <span>Clone</span>
                        </button>
                        <button type="button" class="btn btn-default pull-right" (click)="cancelCloseDialog()">
                            <span class="glyphicon glyphicon-ban-circle"></span>
                            <span>Cancel</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
	`
})
export class AssetCloneComponent extends UIExtraDialog implements OnInit {

	public modalOptions: DecoratorOptions;
	public ASSET_NAME_POPUP_MESSAGE = `Cloned asset will have Environment = 'Select...' and the next available Asset Tag number for device class`;
	protected asset: any;
	public assetName: string;
	public uniqueAssetName: boolean;
	public existAsset: any;
	@ViewChild('includeDependencies') includeDependencies: ElementRef;

	constructor(
		public cloneModalModel: AssetModalModel,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifierService: NotifierService,
		private dialogService: UIDialogService,
		private prompt: UIPromptService) {
		super('#asset-clone-component');
		this.modalOptions = { isResizable: true, isCentered: true };
		this.uniqueAssetName = false;
	}

	ngOnInit() {
		console.log('AssetClone', this.cloneModalModel);
		this.assetExplorerService.getAsset(this.cloneModalModel.assetId)
			.subscribe( (res) => {
				this.asset = res;
				this.assetName = res.assetName;
			}, (err) => console.log(err));

	}
	/**
	 * Close Dialog
	 */
	public cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * Validates if user has Permission to clone assets
	 */
	public canCloneAssets() {
		return this.permissionService.hasPermission(Permission.AssetCreate);
	}

	/**
	 * Validate the name uniqueness of assets
	 */
	public isAssetUnique() {
		const assetToValid = {
			id: this.cloneModalModel.assetId,
			name: this.assetName
		}
		this.assetExplorerService.checkAssetForUniqueName(assetToValid)
			.subscribe((res) => {
				if (res && res.status === 'success') {
					this.uniqueAssetName = res.data.unique;
					this.existAsset = (!res.data.unique) ? res.data : null;

				}
			}, (err) => console.log(err));
	}

	/**
	 * Validates if assets already exists to show confirmation prompt
	 */
	protected cloneAssetValidations(displayEdit: boolean) {

		if ((this.existAsset && !this.existAsset.unique) || !this.uniqueAssetName) {
			this.prompt.open('Asset already exists',
				'The Asset Name you want to create already exists, do you want to proceed?',
				'Confirm', 'Cancel')
				.then( success => {
					if (success) {
						this.cloneAsset(displayEdit);
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.cloneAsset(displayEdit);
		}
	}

	/**
	 * Perform Clone Asset
	 */

	protected cloneAsset(displayEdit: boolean) {
		const assetToClone = {
			assetId: this.cloneModalModel.assetId,
			cloneDependencies: this.includeDependencies.nativeElement.checked,
			name: this.assetName
		}

		this.assetExplorerService.cloneAsset(assetToClone)
			.subscribe((res) => {
				if (res && res.status === 'success') {
					this.notifyClone(displayEdit, res.data.assetId);
				} else {
					console.log('Cannot Clone Asset', res.errors.join());
				}
			}, (err) => console.log(err));
	}

	/**
	 * Display Asset Detail View
	 */

	protected showAssetDetailView() {

		const cloneCloseModel: CloneCLoseModel = {
			clonedAsset: false,
			showEditView: false,
			assetId: this.existAsset.assetId,
			showView: true
		}
		this.close(cloneCloseModel);
	}

	/**
	 * Nofity asset list when clone has been completed
	 * @param displayEdit
	 */
	private notifyClone(displayEdit: boolean, id: any) {
		this.notifierService.broadcast({
			name: 'reloadCurrentAssetList'
		});

		let cloneCloseModel: CloneCLoseModel = {
			clonedAsset: true,
			showEditView : false,
			assetId: null
		}

		if (displayEdit) {
			cloneCloseModel.showEditView = true;
			cloneCloseModel.assetId = id;
		}

		this.close(cloneCloseModel);

	}
}