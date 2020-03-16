// Angular
import {
	Component,
	ViewContainerRef,
	Injector,
	Compiler,
	NgModuleRef,
	NgModule
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Module
import {SharedModule} from '../shared.module';
// Model
import {Dialog, TdsComponentLibraryModule} from 'tds-component-library';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {GridModule} from '@progress/kendo-angular-grid';
import {TabStripModule} from '@progress/kendo-angular-layout';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {ClarityModule} from '@clr/angular';
import {Observable} from 'rxjs';

@Component({
	selector: `empty-component`,
	template: `<div #view></div>`
})
export class DynamicComponent extends Dialog {

	constructor(
		private inj: Injector,
		private comp: Compiler,
		private mod: NgModuleRef<any>
	) {
		super();
	}

	registerAndCreate(compClass: any, view: ViewContainerRef, imports = []): Observable<any> {
		return new Observable((observer: any) => {
			const tmpModule = NgModule({
				imports: [
					CommonModule,
					SharedModule,
					FormsModule,
					// Import Kendo Modules
					TabStripModule,
					DropDownsModule,
					IntlModule,
					DateInputsModule,
					GridModule,
					InputsModule,
					ClarityModule,
					TdsComponentLibraryModule,
				].concat(imports),
				declarations: [
					compClass
				]
			})(class {
			});

			this.comp.compileModuleAndAllComponentsAsync(tmpModule).then((factories) => {
				// When injecting dependencies the number of elements injected increase and the new component is at the end
				// TODO: Keep and eye of this behavior or add a name to the component class so we can always find the real element to insert
				const f = factories.componentFactories[factories.componentFactories.length - 1];
				const cmpRef = f.create(this.inj, [], null, this.mod);
				view.insert(cmpRef.hostView);

				// Return the new created component
				return observer.next(cmpRef);
			});
		});
	}

	/**
	 * Added listener to last button to move the focus
	 * back to the begin of the form.
	 **/
	protected onFocusOutOfCancel(): void {
		let all = document.getElementsByClassName('modal-content tds-angular-component-content')[0];
		let focusable = all.querySelectorAll('input, select, textarea, [tabindex]:not([tabindex="-1"])');
		let firstFocusable = <HTMLElement>focusable[0];
		let lastFocusable = focusable[focusable.length - 1];
		lastFocusable.addEventListener('focusout', () => {
			firstFocusable.focus();
		});
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		super.onCancelClose();
	}

	/**
	 * On double click
	 */
	public onDoubleClick(event: MouseEvent): void {
		super.onDoubleClick(event);
	}
}
