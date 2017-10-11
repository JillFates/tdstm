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

@Component({
	selector: `asset-database-show`,
	template: `<div #view></div>`
})
export class DatabaseShowComponent extends DynamicComponent implements AfterViewInit {
	constructor(
		inj: Injector,
		comp: Compiler,
		mod: NgModuleRef<any>,
		private http: HttpInterceptor,
		@Inject('ID') private modelId: number) {
		super(inj, comp, mod);
	}
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;

	ngAfterViewInit() {
		this.http.get(`../ws/asset/showTemplate/${this.modelId}`).subscribe(res => {
			let template = res.text();

			@Component({
				selector: `dynamic-show`,
				template: template
			}) class DatabaseShowComponentImplementation {

				model: any = {
					name: 'Database'
				};
				onClick() {
					console.log(this.model);
				}
			}

			this.registerAndCreate(DatabaseShowComponentImplementation, this.view);
		});
	}

}