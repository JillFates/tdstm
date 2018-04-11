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
import { getStorageEditComponent } from  '../storage/storage-edit.component';

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
					case 'DATABASE':
						this.registerAndCreate(DatabaseEditComponent(template, model), this.view);
						break;
					case 'DEVICE':
						break;
					case 'STORAGE':
						const component: any =  getStorageEditComponent(template, model);
						this.registerAndCreate(component, this.view);
						break;

				}
			});
	}

}