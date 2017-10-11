import {
	Component,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	NgModule
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
	selector: `empty-component`,
	template: `<div #view></div>`
})
export class DynamicComponent {

	constructor(private inj: Injector, private comp: Compiler, private mod: NgModuleRef<any>) { }

	registerAndCreate(compClass: any, view: ViewContainerRef) {
		const tmpModule = NgModule({
			imports: [
				CommonModule,
				FormsModule
			],
			declarations: [compClass]
		})(class { });

		this.comp.compileModuleAndAllComponentsAsync(tmpModule).then((factories) => {
			const f = factories.componentFactories[0];
			const cmpRef = f.create(this.inj, [], null, this.mod);
			view.insert(cmpRef.hostView);
		});

	}

}