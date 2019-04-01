// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {ProviderRouteModule} from './provider-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {ProviderListComponent} from './components/list/provider-list.component';
import {ProviderViewEditComponent} from './components/view-edit/provider-view-edit.component';
import {ProviderAssociatedComponent} from './components/provider-associated/provider-associated.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {ProvidersResolveService} from './resolve/providers-resolve.service';
// Services
import {ProviderService} from './service/provider.service';

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
		ProviderRouteModule
	],
	declarations: [
		ProviderListComponent,
		ProviderViewEditComponent,
		ProviderAssociatedComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		ProvidersResolveService,
		// Service
		ProviderService
	],
	exports: [
		ProviderListComponent,
		ProviderViewEditComponent,
		ProviderAssociatedComponent
	],
	entryComponents: [
		ProviderViewEditComponent,
		ProviderAssociatedComponent
	]
})

export class ProviderModule {
}