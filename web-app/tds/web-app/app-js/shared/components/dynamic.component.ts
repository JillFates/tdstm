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
import { DropDownsModule } from '@progress/kendo-angular-dropdowns';
import { IntlModule } from '@progress/kendo-angular-intl';
import { DateInputsModule } from '@progress/kendo-angular-dateinputs';

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
				FormsModule,
				// Import Kendo Modules
				DropDownsModule,
				IntlModule,
				DateInputsModule
			],
			declarations: [compClass]
		})(class { });

		this.comp.compileModuleAndAllComponentsAsync(tmpModule).then((factories) => {
			// When injecting dependencies the number of elements injected increase and the new component is at the end
			// TODO: Keep and eye of this behavior or add a name to the component class so we can always find the real element to insert
			const f = factories.componentFactories[factories.componentFactories.length - 1];
			const cmpRef = f.create(this.inj, [], null, this.mod);
			view.insert(cmpRef.hostView);
		});

	}

}