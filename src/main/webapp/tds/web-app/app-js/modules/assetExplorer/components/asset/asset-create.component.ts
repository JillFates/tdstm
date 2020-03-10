// Angular
import {HttpClient} from '@angular/common/http';
import {
	Component,
	AfterViewInit,
	ViewChild,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	Inject, OnInit
} from '@angular/core';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Permission} from '../../../../shared/model/permission.model';
// Component
import {DynamicComponent} from '../../../../shared/components/dynamic.component';
import {DatabaseCreateComponent} from '../database/database-create.component';
import {StorageCreateComponent} from '../storage/storage-create.component';
import {DeviceCreateComponent} from '../device/device-create.component';
import {ApplicationCreateComponent} from '../application/application-create.component';
// Service
import {TagService} from '../../../assetTags/service/tag.service';
import {PermissionService} from '../../../../shared/services/permission.service';
// Other
import {Observable} from 'rxjs';
import {DialogButtonType} from 'tds-component-library';
import {AssetCommonEdit} from './asset-common-edit';

@Component({
	selector: `tds-asset-all-create`,
	template: `<div #view></div>`
})
export class AssetCreateComponent extends DynamicComponent implements OnInit, AfterViewInit {

	@ViewChild('view', {read: ViewContainerRef, static: true}) view: ViewContainerRef;

	public asset;

	constructor(
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpClient,
		private tagService: TagService,
		private permissionService: PermissionService) {
		super(inj, comp, mod);
	}

	ngOnInit(): void {
		this.asset = this.data.assetClass;

		this.buttons.push({
			name: 'saveAsset',
			icon: 'floppy',
			show: () => this.isEditAvailable(),
			type: DialogButtonType.ACTION,
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
				this.http.get(`../ws/asset/createTemplate/${this.asset}`, {responseType: 'text'}),
				this.http.get(`../ws/asset/defaultCreateModel/${this.asset}`))
				.subscribe((response: any) => {
					let template = response[0];
					let model = response[1];

					if (!model.moveBundleList && model.dependencyMap && model.dependencyMap.moveBundleList) {
						model.moveBundleList = model.dependencyMap.moveBundleList;
					}
					setTimeout( () => {
						switch (this.asset) {
							case 'APPLICATION':
								this.registerAndCreate(ApplicationCreateComponent(template, model, metadata, this), this.view).subscribe(componentRef => this.prepareButtonsReference(componentRef));
								break;
							case 'DATABASE':
								this.registerAndCreate(DatabaseCreateComponent(template, model, metadata, this), this.view).subscribe();
								break;
							case 'DEVICE':
								this.registerAndCreate(DeviceCreateComponent(template, model, metadata, this), this.view).subscribe();
								break;
							case 'STORAGE':
								this.registerAndCreate(StorageCreateComponent(template, model, metadata, this), this.view).subscribe();
								break;

						}
					}, 700);
				}, (error) => {
					console.error('Error: ');
					console.error(error);
				});
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
			disabled: () => (!lastComponent.isDependenciesValidForm),
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
		promises.push(this.getTagList(metadata));
		return new Promise(function (resolve, reject) {
			// Result with all modification
			Promise.all(promises).then(function () {
				resolve(metadata);
			});
		});
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

	private isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetEdit);
	}

	private handleError(error: string): void {
		console.log(error);
	}

}
