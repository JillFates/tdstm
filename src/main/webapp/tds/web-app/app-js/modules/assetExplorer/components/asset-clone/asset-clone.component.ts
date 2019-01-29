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
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/asset-clone/asset-clone.component.html'
})
export class AssetCloneComponent extends UIExtraDialog implements OnInit {

	public modalOptions: DecoratorOptions;
	public ASSET_NAME_POPUP_MESSAGE = `Cloned asset will have Environment = 'Select...' and the next available Asset Tag number for device class`;
	protected asset: any;
	protected assetName: string;
	protected uniqueAssetName: boolean;
	protected existAsset: any;
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
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * Validates if user has Permission to clone assets
	 */
	protected canCloneAssets() {
		return this.permissionService.hasPermission(Permission.AssetCreate);
	}

	/**
	 * Validate the name uniqueness of assets
	 */
	protected isAssetUnique() {
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