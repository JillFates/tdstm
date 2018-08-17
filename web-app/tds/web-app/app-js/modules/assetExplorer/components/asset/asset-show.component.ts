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

import { HttpInterceptor } from '../../../../shared/providers/http-interceptor.provider';
import { DynamicComponent } from '../../../../shared/components/dynamic.component';

import { DatabaseShowComponent } from '../database/database-show.component';
import { ApplicationShowComponent } from '../application/application-show.component';
import { DeviceShowComponent } from '../device/device-show.component';
import { StorageShowComponent } from '../storage/storage-show.component';

import { AssetExplorerModule } from '../../asset-explorer.module';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';

@Component({
	selector: `asset-database-show`,
	template: `<div #view></div>`
})
export class AssetShowComponent extends DynamicComponent implements AfterViewInit {

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
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;

	ngAfterViewInit() {
		this.prepareMetadata().then( (metadata: any) => {
			this.http.get(`../ws/asset/showTemplate/${this.modelId}`).subscribe(res => {
				let template = res.text();
				const additionalImports = [AssetExplorerModule];
				switch (this.asset) {
					case 'APPLICATION':
						this.registerAndCreate(ApplicationShowComponent(template, this.modelId, metadata), this.view, additionalImports);
						break;
					case 'DATABASE':
						this.registerAndCreate(DatabaseShowComponent(template, this.modelId, metadata), this.view, additionalImports);
						break;
					case 'DEVICE':
						this.registerAndCreate(DeviceShowComponent(template, this.modelId, metadata), this.view, additionalImports);
						break;
					case 'STORAGE':
						this.registerAndCreate(StorageShowComponent(template, this.modelId, metadata), this.view, additionalImports);
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

}