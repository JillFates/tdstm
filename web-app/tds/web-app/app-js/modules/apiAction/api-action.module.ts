// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {APIActionRouteModule} from './api-action-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {APIActionListComponent} from './components/list/api-action-list.component';
import {APIActionViewEditComponent} from './components/view-edit/api-action-view-edit.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {APIActionResolveService} from './resolve/api-action-resolve.service';
// Services
import {APIActionService} from './service/api-action.service';

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
		APIActionRouteModule
	],
	declarations: [
		APIActionListComponent,
		APIActionViewEditComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		APIActionResolveService,
		// Service
		APIActionService
	],
	exports: [],
	entryComponents: [
		APIActionViewEditComponent
	]
})

export class APIActionModule {
}