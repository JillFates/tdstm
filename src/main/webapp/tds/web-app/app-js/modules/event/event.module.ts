import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {EventListComponent} from './components/list/event-list.component';
import {EventService} from './service/event.service';
import {EventRouteModule} from './event-routing.states';
import {EventsResolveService} from './resolve/events-resolve.service';
import {EventCreateComponent} from './components/create/event-create.component';
import {EventViewEditComponent} from './components/view-edit/event-view-edit.component';

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
		EventRouteModule
	],
	declarations: [
		EventListComponent,
		EventCreateComponent,
		EventViewEditComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		EventsResolveService,
		// Service
		EventService
	],
	exports: [
		EventListComponent,
		EventCreateComponent,
		EventViewEditComponent
	],
	entryComponents: [
		EventCreateComponent,
		EventViewEditComponent
	]
})

export class EventModule {

}
