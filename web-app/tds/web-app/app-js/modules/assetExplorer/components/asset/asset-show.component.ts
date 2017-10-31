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
		@Inject('ID') private modelId: number,
		@Inject('ASSET') private asset: 'APPLICATION' | 'DATABASE' | 'DEVICE' | 'STORAGE') {
		super(inj, comp, mod);
	}
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;

	ngAfterViewInit() {
		this.http.get(`../ws/asset/showTemplate/${this.modelId}`).subscribe(res => {
			let template = res.text();
			switch (this.asset) {
				case 'APPLICATION':
					this.registerAndCreate(ApplicationShowComponent(template), this.view);
					break;
				case 'DATABASE':
					this.registerAndCreate(DatabaseShowComponent(template), this.view);
					break;
				case 'DEVICE':
					this.registerAndCreate(DeviceShowComponent(template), this.view);
					break;
				case 'STORAGE':
					this.registerAndCreate(StorageShowComponent(template), this.view);
					break;
			}
		});
	}

}