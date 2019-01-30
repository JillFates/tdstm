import {
	Component,
	AfterViewInit,
	ViewChild,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	Inject
} from '@angular/core';

import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../../shared/providers/http-interceptor.provider';
import {DynamicComponent} from '../../../../shared/components/dynamic.component';

import {DatabaseEditComponent} from '../database/database-edit.component';
import {StorageEditComponent} from '../storage/storage-edit.component';
import {DeviceEditComponent} from '../device/device-edit.component';
import {ApplicationEditComponent} from '../application/application-edit.component';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TagService} from '../../../assetTags/service/tag.service';

@Component({
	selector: `tds-asset-all-edit`,
	template: `<div #view></div>`
})
export class AssetEditComponent extends DynamicComponent implements AfterViewInit {

	@ViewChild('view', {read: ViewContainerRef}) view: ViewContainerRef;

	constructor(
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpInterceptor,
		private tagService: TagService,
		@Inject('ID') private modelId: number,
		@Inject('ASSET') private asset: 'APPLICATION' | 'DATABASE' | 'DEVICE' | 'STORAGE') {
		super(inj, comp, mod);
	}

	ngAfterViewInit() {
		this.prepareMetadata().then( (metadata: any) => {
			Observable.zip(
				this.http.get(`../ws/asset/editTemplate/${this.modelId}`),
				this.http.get(`../ws/asset/editModel/${this.modelId}`))
				.subscribe(res => {
					let template = res[0].text();
					let model = res[1].json();

					switch (this.asset) {
						case 'APPLICATION':
							this.registerAndCreate(ApplicationEditComponent(template, model, metadata), this.view);
							break;
						case 'DATABASE':
							this.registerAndCreate(DatabaseEditComponent(template, model, metadata), this.view);
							break;
						case 'DEVICE':
							this.registerAndCreate(DeviceEditComponent(template, model, metadata), this.view);
							break;
						case 'STORAGE':
							this.registerAndCreate(StorageEditComponent(template, model, metadata), this.view);
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

}