// Angular
import {
	Component,
	AfterViewInit,
	ViewChild,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	OnInit,
	Input, ComponentFactoryResolver
} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Module
import {AssetExplorerModule} from '../../asset-explorer.module';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Component
import {DynamicComponent} from '../../../../shared/components/dynamic.component';
import {DatabaseShowComponent} from '../database/database-show.component';
import {ApplicationShowComponent} from '../application/application-show.component';
import {DeviceShowComponent} from '../device/device-show.component';
import {StorageShowComponent} from '../storage/storage-show.component';
import {AssetEditComponent} from './asset-edit.component';
// Service
import {TagService} from '../../../assetTags/service/tag.service';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {DOMAIN} from '../../../../shared/model/constants';
import {Observable} from 'rxjs';

@Component({
	selector: `tds-asset-all-show`,
	template: `<div #view></div>`
})
export class AssetShowComponent extends DynamicComponent implements OnInit, AfterViewInit {
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
			type: DialogButtonType.ACTION,
			action: this.showAssetEditView.bind(this, this.data.assetId, this.asset)
		});

		this.buttons.push({
			name: 'cloneAsset',
			icon: 'copy',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onCloneAsset.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.isDeleteAvailable(),
			type: DialogButtonType.ACTION,
			action: this.onDeleteAsset.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onDismiss.bind(this)
		});
	}

	ngAfterViewInit() {
		this.prepareMetadata().then( (metadata: any) => {
			Observable.zip(
				this.http.get(`../ws/asset/showTemplate/${this.modelId}`, {responseType: 'text'}),
				this.http.get(`../ws/asset/assetForDependencyGroup?assetId=${this.modelId}`))
				.subscribe((response: any) => {
					let template = response[0];
					const templateTitleData = response[1];

					this.setTitle(this.getModalTitle(templateTitleData.data));

					const additionalImports = [AssetExplorerModule];
					switch (this.asset) {
						case 'APPLICATION':
							this.registerAndCreate(ApplicationShowComponent(template, this.modelId, metadata, this), this.view, additionalImports).subscribe();
							break;
						case 'DATABASE':
							this.registerAndCreate(DatabaseShowComponent(template, this.modelId, metadata, this), this.view, additionalImports).subscribe();
							break;
						case 'DEVICE':
							this.registerAndCreate(DeviceShowComponent(template, this.modelId, metadata, this), this.view, additionalImports).subscribe();
							break;
						case 'STORAGE':
							this.registerAndCreate(StorageShowComponent(template, this.modelId, metadata, this), this.view, additionalImports).subscribe();
							break;
					}
			});
		});
	}

	/**
	 * This is used to prepare/build common metadata/information share among the Asset components and send it to be
	 * available.
	 * @returns {Promise<any>}
	 */
	private prepareMetadata(): Promise<any> {
		let metadata: any = {};
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

	private handleError(error: string): void {
		console.log(error);
	}

	/**
	 * Open same Model in Edit Mode
	 */
	private showAssetEditView(assetId: any, assetClass: any): void {
		// Close View and Open Edit
		this.onCancelClose();

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetEditComponent,
			data: {
				assetId: assetId,
				assetClass: assetClass
			},
			modalConfiguration: {
				title: 'Asset',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	/**
	 * Allows to clone an application asset
	 */
	onCloneAsset(): void {

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCloneComponent,
			data: {
				cloneModalModel: {
					assetId: this.modelId,
					assetType: this.asset,
				}
			},
			modalConfiguration: {
				title: 'Clone Asset',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((data: any) => {
			if (data.clonedAsset && data.showEditView) {
				this.showAssetEditView(data.assetId, DOMAIN.APPLICATION);
			} else if (!data.clonedAsset && data.showView) {
				this.showAssetDetailView(DOMAIN.APPLICATION, data.assetId);
			}
		});

	}

	protected showAssetDetailView(assetClass: string, id: number) {
		// Close View and Open Edit
		this.onCancelClose();

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetShowComponent,
			data: {
				assetId: id,
				assetClass: assetClass
			},
			modalConfiguration: {
				title: 'Asset',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	/**
	 * Allows to delete the application assets
	 */
	private onDeleteAsset() {
		this.dialogService.confirm(
			'Confirmation Required',
			'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel'
		).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				this.assetExplorerService.deleteAssets([this.modelId.toString()]).subscribe( res => {
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
		htmlModalTitle += `<div class="badge modal-badge">A</div>`;
		if (titleData.name !== null) {
			htmlModalTitle += `<h4 class="modal-title">${titleData.name}</h4>`;
		}
		if (titleData.moveBundle !== null) {
			htmlModalTitle += `<div class="modal-subtitle">${titleData.moveBundle}</div>`;
		}
		if (titleData.depGroup !== null) {
			htmlModalTitle += `<a href="../moveBundle/dependencyConsole/map/${titleData.depGroup}?assetName=${titleData.name}"><div class="badge modal-subbadge">${titleData.depGroup}</div></a>`;
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
		super.onCancelClose();
	}
}
