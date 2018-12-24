// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {LicenseAdminRouteModule} from './license-admin-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {LicenseListComponent} from './components/list/license-list.component';
import {RequestLicenseComponent} from './components/request/request-license.component';
import {LicenseViewEditComponent} from './components/view-edit/license-view-edit.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {LicensesResolveService} from './resolve/licenses-resolve.service';
// Services
import {LicenseAdminService} from './service/license-admin.service';

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
		LicenseAdminRouteModule
	],
	declarations: [
		LicenseListComponent,
		RequestLicenseComponent,
		LicenseViewEditComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		LicensesResolveService,
		// Service
		LicenseAdminService
	],
	exports: [
		LicenseListComponent,
		RequestLicenseComponent,
		LicenseViewEditComponent
	],
	entryComponents: [
		RequestLicenseComponent,
		LicenseViewEditComponent
	]
})

export class LicenseAdminModule {
}