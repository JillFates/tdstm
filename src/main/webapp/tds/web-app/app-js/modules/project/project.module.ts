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
import {ProjectListComponent} from './components/list/project-list.component';
import {ProjectService} from './service/project.service';
import {ProjectResolveService} from './resolve/project-resolve.service';
import {ProjectRouteModule} from './project-routing.states';
import {ProjectCreateComponent} from './components/create/project-create.component';
import {ProjectViewEditComponent} from './components/view-edit/project-view-edit.component';

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
		ProjectRouteModule
	],
	declarations: [
		ProjectListComponent,
		ProjectViewEditComponent,
		ProjectCreateComponent
	],
	providers: [
		// Resolve
		ProjectResolveService,
		ModuleResolveService,
		// Services
		ProjectService
	],
	exports: [
		ProjectListComponent,
		ProjectViewEditComponent,
		ProjectCreateComponent
	],
	entryComponents: [
		ProjectListComponent,
		ProjectViewEditComponent,
		ProjectCreateComponent
	]
})

export class ProjectModule {
}