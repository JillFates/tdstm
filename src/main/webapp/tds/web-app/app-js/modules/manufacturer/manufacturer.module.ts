// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Shared
import { SharedModule } from '../../shared/shared.module';
// Route Module
import {ManufacturerRouteModule} from './manufacturer-routing.states';
// Kendo
import { GridModule } from '@progress/kendo-angular-grid';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
import { InputsModule } from '@progress/kendo-angular-inputs';
import { DateInputsModule } from '@progress/kendo-angular-dateinputs';
// Components
import {ManufacturerViewEditComponent} from './components/view-edit/manufacturer-view-edit.component';
import {ManufacturerListComponent} from './components/list/manufacturer-list.component';
// Resolves
import { ModuleResolveService } from '../../shared/resolves/module.resolve.service';
// Services
import {ManufacturerService} from './service/manufacturer.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownListModule,
		GridModule,
		InputsModule,
		DateInputsModule,
		// Route
		ManufacturerRouteModule,
	],
	declarations: [
		ManufacturerListComponent,
		ManufacturerViewEditComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// Service
		ManufacturerService,
	],
	exports: [
		ManufacturerListComponent,
		ManufacturerViewEditComponent
	],
	entryComponents: [
		ManufacturerViewEditComponent
	],
})
export class ManufacturerModule {}
