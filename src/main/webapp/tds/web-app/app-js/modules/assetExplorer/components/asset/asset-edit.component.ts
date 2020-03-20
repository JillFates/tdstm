// Angular
import {
	Component,
	AfterViewInit,
	ViewChild,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	Input, ComponentFactoryResolver, OnInit
} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Permission} from '../../../../shared/model/permission.model';
// Component
import {DynamicComponent} from '../../../../shared/components/dynamic.component';
import {AssetCommonEdit} from './asset-common-edit';
import {DatabaseEditComponent} from '../database/database-edit.component';
import {StorageEditComponent} from '../storage/storage-edit.component';
import {DeviceEditComponent} from '../device/device-edit.component';
import {ApplicationEditComponent} from '../application/application-edit.component';
// Service
import {TagService} from '../../../assetTags/service/tag.service';
import {DialogButtonModel, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PermissionService} from '../../../../shared/services/permission.service';
// Other
import {Observable} from 'rxjs';

@Component({
	selector: `tds-asset-all-edit`,
	template: `<div #view></div>`
})
export class AssetEditComponent extends DynamicComponent implements OnInit, AfterViewInit {
	@Input() data: any;

	public modelId;
	public asset;

	@ViewChild('view', {read: ViewContainerRef, static: true}) view: ViewContainerRef;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private assetExplorerService: AssetExplorerService,
		private notifierService: NotifierService,
		private permissionService: PermissionService,
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpClient,
		private tagService: TagService) {
		super(inj, comp, mod);
	}

	ngOnInit(): void {
		this.modelId = this.data.assetId;
		this.asset = this.data.assetClass;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.isEditAvailable(),
			active: () => true,
			type: DialogButtonType.ACTION
		});

		this.buttons.push({
			name: 'saveAsset',
			icon: 'floppy',
			show: () => this.isEditAvailable(),
			type: DialogButtonType.ACTION
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.isDeleteAvailable(),
			type: DialogButtonType.ACTION,
			action: this.onDeleteAsset.bind(this)
		});

		this.buttons.push({
			name: 'cancelEdit',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onDismiss.bind(this)
		});
	}

	ngAfterViewInit() {
		this.prepareMetadata().then( (metadata: any) => {
			Observable.zip(
				this.http.get(`../ws/asset/editTemplate/${this.modelId}`, {responseType: 'text'}),
				this.http.get(`../ws/asset/editModel/${this.modelId}`),
				this.http.get(`../ws/asset/assetForDependencyGroup?assetId=${this.modelId}`))
				.subscribe((response: any) => {
					let template = response[0];
					let model = response[1];
					const templateTitleData = response[2];

					this.setTitle(this.getModalTitle(templateTitleData.data));

					switch (this.asset) {
						case 'APPLICATION':
							this.registerAndCreate(ApplicationEditComponent(template, model, metadata, this), this.view).subscribe(componentRef => this.prepareButtonsReference(componentRef));
							break;
						case 'DATABASE':
							this.registerAndCreate(DatabaseEditComponent(template, model, metadata, this), this.view).subscribe(componentRef => this.prepareButtonsReference(componentRef));
							break;
						case 'DEVICE':
							this.registerAndCreate(DeviceEditComponent(template, model, metadata, this), this.view).subscribe(componentRef => this.prepareButtonsReference(componentRef));
							break;
						case 'STORAGE':
							this.registerAndCreate(StorageEditComponent(template, model, metadata, this), this.view).subscribe(componentRef => this.prepareButtonsReference(componentRef));
							break;
					}
				});
		}, (error) => {
			console.error('Error: ');
			console.error(error);
		});
	}

	/**
	 * Prepare the Dialog to map the buttons, with this all buttons are the same for every Dialog
	 * @param componentRef
	 */
	private prepareButtonsReference(componentRef: any): void {
		// @ts-ignore
		const lastComponent = <AssetCommonEdit>componentRef.instance;
		this.changeButton('saveAsset', {
			action: lastComponent.submitForm,
			disabled: () => (!(lastComponent.isDirty() && lastComponent.form.valid) || !lastComponent.isDependenciesValidForm),
		});
		this.changeButton('cancelEdit', {
			action: lastComponent.onCancelEdit
		});
	}

	/**
	 * This is used to prepare/build common metadata/information share among the Asset components and send it to be
	 * available.
	 * @returns {Promise<any>}
	 */
	private prepareMetadata(): Promise<any> {
		let metadata: any = {};
		let promises = [];
		promises.push(this.getAssetTags(metadata));
		promises.push(this.getTagList(metadata));
		return new Promise(function (resolve, reject) {
			// Result with all modification
			Promise.all(promises).then(function () {
				resolve(metadata);
			});
		});
	}

	private getAssetTags(metadata): Promise<any> {
		let promise = new Promise((resolve, reject) => {
			// Check for tags related to the asset.
			this.tagService.getAssetTags(this.modelId).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					metadata.assetTags = result.data;
				} else {
					metadata.assetTags = [];
					this.handleError(result.errors ? result.errors[0] : 'Error on tags by asset id call');
				}
				resolve(metadata);
			}, error => {
				resolve(metadata);
				this.handleError(error);
			});
		});
		return promise;
	}

	private getTagList(metadata): Promise<any> {
		let promise = new Promise((resolve, reject) => {
			// Check for tags related to the asset.
			this.tagService.getTags().subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					metadata.tagList = result.data;
				} else {
					metadata.tagList = [];
					this.handleError(result.errors ? result.errors[0] : 'Error on get tag list call');
				}
				resolve(metadata);
			}, error => {
				resolve(metadata);
				this.handleError(error);
			});
		});
		return promise;
	}

	private handleError(error: string): void {
		console.log(error);
	}

	/**
	 * Allows to delete the application assets
	 */
	private onDeleteAsset() {
		this.dialogService.confirm(
			'Confirmation Required',
			'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel'
		).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
				this.assetExplorerService.deleteAssets([this.modelId.toString()]).subscribe(res => {
					if (res) {
						this.notifierService.broadcast({
							name: 'reloadCurrentAssetList'
						});
						this.onCancelClose();
					}
				}, (error) => console.log(error));
			}
		});
	}

	private isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	private isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetEdit);
	}

	private getModalTitle(titleData: any): string {
		let htmlModalTitle = '<div class="modal-title-container">';
		const assetChar = this.asset.charAt(0).toUpperCase();
		htmlModalTitle += `<div class="badge modal-badge">${assetChar}</div>`;
		if (titleData.name !== null) {
			htmlModalTitle += `<h4 class="modal-title">${titleData.name}</h4>`;
		}
		if (titleData.moveBundle !== null) {
			htmlModalTitle += `<div class="modal-subtitle">${titleData.moveBundle}</div>`;
		}
		if (titleData.depGroup !== null && titleData.depGroup > 0) {
			htmlModalTitle += `<a href="${encodeURI('../moveBundle/dependencyConsole/map/' + titleData.depGroup + '?assetName=' + titleData.name)}"><div class="badge modal-subbadge">${titleData.depGroup}</div></a>`;
		}
		htmlModalTitle += `</div>`;
		if (titleData.description !== null) {
			htmlModalTitle += `<div class="modal-description">${titleData.description}</div>`;
		}
		return htmlModalTitle;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		const cancelEdit = this.buttons.find((button: DialogButtonModel) => button.name === 'cancelEdit');
		cancelEdit.action();
	}
}
