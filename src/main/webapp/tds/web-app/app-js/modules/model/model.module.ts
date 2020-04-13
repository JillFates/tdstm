// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {ModelRouteModule} from './model-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {ModelListComponent} from './components/list/model-list.component';
import {ModelViewEditComponent} from './view-edit/model-view-edit.component';
// Resolves
// Services
import {ModelService} from './service/model.service';
import {ManufacturerService} from '../manufacturer/service/manufacturer.service';

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
		ModelRouteModule
	],
	declarations: [
		ModelListComponent,
		ModelViewEditComponent
	],
	providers: [
		// Service
		ModelService,
		ManufacturerService,
	],
	exports: [
		ModelListComponent,
	],
	entryComponents: [
		ModelViewEditComponent
	]
})

export class ModelModule {
}