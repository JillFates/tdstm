// Angular
import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
// Model
import {Permission} from '../../../../shared/model/permission.model';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetModalModel} from '../../model/asset-modal.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import * as R from 'ramda';

@Component({
	selector: `tds-asset-clone-modal`,
	template: `
		<div class="asset-clone-component">
			<form role="form" class="form-horizontal">
				<div class="row">
					<div class="col-md-12">
						<div class="form-group">
							<label for="newAssetName" class="col-sm-4 control-label"> New asset name</label>
							<div class="col-sm-6">
								<input type="text" id="newAssetName" name="newAssetName" [(ngModel)]="assetName" class="form-control" (keyup)="isAssetUnique()"/>
								<label class="asset-name-validations">
									<span class="asset-unique-name" *ngIf="!uniqueAssetName && !existAsset"> Change name appropriately </span>
									<span *ngIf="assetName.length == 0"> Asset name is required </span>
									<span *ngIf="existAsset"> Name already exists
								<span (click)="showAssetDetailView()" class="link-detail-asset-view"> click here to view</span>
							</span>
								</label>
							</div>
							<div class="align-controls">
								<tds-popup-asset-message
										[message]="ASSET_NAME_POPUP_MESSAGE"></tds-popup-asset-message>
							</div>
						</div>
						<div class="form-group">
							<label for="includeDependencies" class="col-sm-4 control-label"> Include
								Dependencies</label>
							<div class="col-sm-2 align-controls">
								<clr-checkbox-wrapper class="inline">
								<input
									clrCheckbox
									type="checkbox"
									id="includeDependencies"
									name="includeDependencies"
									#includeDependencies
								/>
                				</clr-checkbox-wrapper>

								<tds-popup-asset-message
										[message]="'Clone all existing dependencies as well but will change the status of each to Questioned.'"></tds-popup-asset-message>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	`
})
export class AssetCloneComponent extends Dialog implements OnInit {
	@Input() data: any;

	public ASSET_NAME_POPUP_MESSAGE = `Cloned asset will have Environment = 'Select...' and the next available Asset Tag number for device class`;
	protected asset: any;
	public assetName = '';
	public uniqueAssetName: boolean;
	public existAsset: any;
	@ViewChild('includeDependencies', {static: false}) includeDependencies: ElementRef;

	public cloneModalModel: AssetModalModel;
	private dataSignature: string;

	constructor(
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifierService: NotifierService,
		private translatePipe: TranslatePipe) {
		super();

		this.uniqueAssetName = false;
	}

	ngOnInit() {
		this.cloneModalModel = R.clone(this.data.cloneModalModel);

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.canCloneAssets(),
			disabled: () => this.assetName.length === 0,
			type: DialogButtonType.ACTION,
			action: this.cloneAssetValidations.bind(this, true)
		});

		this.buttons.push({
			name: 'cloneAsset',
			icon: 'copy',
			show: () => this.canCloneAssets(),
			disabled: () => this.assetName.length === 0,
			type: DialogButtonType.ACTION,
			action: this.cloneAssetValidations.bind(this, false)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.assetExplorerService.getAsset(this.cloneModalModel.assetId)
			.subscribe((res) => {
				this.asset = res;
				this.assetName = res.assetName;

				this.dataSignature = JSON.stringify({asset: this.asset, assetName: this.assetName});
			}, (err) => console.log(err));

	}

	/**
	 * Close Dialog
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
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
			this.dialogService.confirm(
				'Asset already exists',
				'The Asset Name you want to create already exists, do you want to proceed?'
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {

						this.cloneAsset(displayEdit);
					}
				});
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
		};

		this.assetExplorerService.cloneAsset(assetToClone)
			.subscribe((res) => {
				if (res && res.status === 'success') {
					this.notifyClone(displayEdit, res.data.assetId);
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
		super.onAcceptSuccess(cloneCloseModel);
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
			showEditView: false,
			assetId: null
		};

		if (displayEdit) {
			cloneCloseModel.showEditView = true;
			cloneCloseModel.assetId = id;
		}

		super.onAcceptSuccess(cloneCloseModel);

	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify({asset: this.asset, assetName: this.assetName});
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
