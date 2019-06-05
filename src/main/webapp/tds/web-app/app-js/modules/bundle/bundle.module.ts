// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {BundleListComponent} from './components/list/bundle-list.component';
import {BundleService} from './service/bundle.service';
import {BundleResolveService} from './resolve/bundle-resolve.service';
import {BundleRouteModule} from './bundle-routing.states';
import {BundleShowComponent} from './components/show/bundle-show.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		InputsModule,
		DateInputsModule,
		// Route
		BundleRouteModule
	],
	declarations: [
		BundleListComponent,
		BundleShowComponent
	],
	providers: [
		// Resolve
		BundleResolveService,
		ModuleResolveService,
		// Services
		BundleService
	],
	exports: [
		BundleListComponent,
		BundleShowComponent
	],
	entryComponents: [
		BundleListComponent,
		BundleShowComponent
	]
})

export class BundleModule {
}