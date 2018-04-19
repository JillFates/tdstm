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

import { Observable } from 'rxjs/Observable';
import { HttpInterceptor } from '../../../../shared/providers/http-interceptor.provider';
import { DynamicComponent } from '../../../../shared/components/dynamic.component';

import { DatabaseEditComponent } from '../database/database-edit.component';
import { StorageEditComponent } from  '../storage/storage-edit.component';
import {DeviceEditComponent} from '../device/device-edit.component';
import { ApplicationEditComponent } from '../application/application-edit.component';

@Component({
	selector: `asset-database-edit`,
	template: `<div #view></div>`
})
export class AssetEditComponent extends DynamicComponent implements AfterViewInit {

	@ViewChild('view', {read: ViewContainerRef}) view: ViewContainerRef;

	constructor(
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpInterceptor,
		@Inject('ID') private modelId: number,
		@Inject('ASSET') private asset: 'APPLICATION' | 'DATABASE' | 'DEVICE' | 'STORAGE') {
		super(inj, comp, mod);
	}

	ngAfterViewInit() {
		Observable.zip(
			this.http.get(`../ws/asset/editTemplate/${this.modelId}`),
			this.http.get(`../ws/asset/editModel/${this.modelId}`))
			.subscribe(res => {
				let template = res[0].text();
				let model = res[1].json();

				switch (this.asset) {
					case 'APPLICATION':
						this.registerAndCreate(ApplicationEditComponent(template, model), this.view);
						break;
					case 'DATABASE':
						this.registerAndCreate(DatabaseEditComponent(template, model), this.view);
						break;
					case 'DEVICE':
						this.registerAndCreate(DeviceEditComponent(template, model), this.view);
						break;
					case 'STORAGE':
						this.registerAndCreate(StorageEditComponent(template, model), this.view);
						break;

				}
			});
	}

}