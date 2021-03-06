// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {LicenseManagerRouteModule} from './license-manager-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {LicenseListComponent} from './components/list/license-list.component';
import {RequestImportComponent} from './components/requestImport/request-import.component';
import {LicenseDetailComponent} from './components/detail/license-detail.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {LicensesResolveService} from './resolve/licenses-resolve.service';
// Services
import {LicenseManagerService} from './service/license-manager.service';

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
		IntlModule,
		// Route
		LicenseManagerRouteModule
	],
	declarations: [
		LicenseListComponent,
		RequestImportComponent,
		LicenseDetailComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		LicensesResolveService,
		// Service
		LicenseManagerService
	],
	exports: [
		LicenseListComponent,
		RequestImportComponent,
		LicenseDetailComponent
	],
	entryComponents: [
		RequestImportComponent,
		LicenseDetailComponent
	]
})

export class LicenseManagerModule {
}