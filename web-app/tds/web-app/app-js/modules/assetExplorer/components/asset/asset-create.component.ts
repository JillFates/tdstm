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

import {Observable} from 'rxjs/Observable';
import {HttpInterceptor} from '../../../../shared/providers/http-interceptor.provider';
import {DynamicComponent} from '../../../../shared/components/dynamic.component';

import {DatabaseCreateComponent} from '../database/database-create.component';
import {StorageCreateComponent} from '../storage/storage-create.component';
import {DeviceCreateComponent} from '../device/device-create.component';
import {ApplicationCreateComponent} from '../application/application-create.component';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TagService} from '../../../assetTags/service/tag.service';
import {TagModel} from '../../../assetTags/model/tag.model';

@Component({
	selector: `tds-asset-all-create`,
	template: `<div #view></div>`
})
export class AssetCreateComponent extends DynamicComponent implements AfterViewInit {

	@ViewChild('view', {read: ViewContainerRef}) view: ViewContainerRef;

	constructor(
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpInterceptor,
		private tagService: TagService,
		@Inject('ASSET') private asset: 'APPLICATION' | 'DATABASE' | 'DEVICE' | 'STORAGE') {
		super(inj, comp, mod);
	}

	ngAfterViewInit() {
		this.prepareMetadata().then( (metadata: any) => {
			this.http.get(`../ws/asset/createTemplate/${this.asset}`)
				.subscribe(res => {
					let template = res.text();

					switch (this.asset) {
						case 'APPLICATION':
							this.registerAndCreate(ApplicationCreateComponent(template, metadata), this.view);
							break;
						case 'DATABASE':
							this.registerAndCreate(DatabaseCreateComponent(template, metadata), this.view);
							break;
						case 'DEVICE':
							this.registerAndCreate(DeviceCreateComponent(template, metadata), this.view);
							break;
						case 'STORAGE':
							this.registerAndCreate(StorageCreateComponent(template, metadata), this.view);
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

	private handleError(error: string): void {
		console.log(error);
	}

}